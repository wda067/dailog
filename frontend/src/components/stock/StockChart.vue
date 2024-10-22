<template>
  <div
    style="width: 100%; height: 250px; position: relative;">
    <StockChartOptions
      :selectedInterval="selectedInterval"
      :selectedChartType="selectedChartType"
      @interval-change="updateInterval"
      @chart-type-change="updateChartType"
    />
    <canvas ref="stockChartRef" style="margin-top: 5px;"></canvas>
  </div>
</template>

<script lang="ts" setup>
import { nextTick, onMounted, ref, watch } from 'vue';
import axios from 'axios';
import 'chart.js';
import 'chartjs-adapter-date-fns';
import 'chartjs-chart-financial';
import 'chartjs-plugin-crosshair';
import { CandlestickController, CandlestickElement } from 'chartjs-chart-financial';
import { Chart, plugins, registerables } from 'chart.js';
import { useRouter } from 'vue-router';
import StockChartOptions from '@/components/stock/StockChartOptions.vue';
import dayjs from 'dayjs';

declare module 'chart.js' {
  interface FinancialDataPoint {
    x: number;
    o: number;
    h: number;
    l: number;
    c: number;
    chg?: number;
    vol?: number;
    y?: number;
  }
}

Chart.register(...registerables, plugins, CandlestickElement, CandlestickController);

interface StockPrice {
  date: string;    //일자 (YYYYMMDD)
  open: string;    //시가
  close: string;   //종가
  high: string;    //고가
  low: string;     //저가
  rate: string;    //등락율
  volume: string;  //거래량
}
const stockPrices = ref<StockPrice[]>([]);

const stockChartRef = ref<HTMLCanvasElement | null>(null);
const selectedChartType = ref<number | null>(0);
const selectedInterval = ref<number | null>(null);
const router = useRouter();
const props = defineProps({
  ticker: {
    type: String,
    required: true
  }
});

const fetchStockPrice = async () => {
  try {
    if (selectedInterval.value !== null) {
      await fetchStockByMinutes();
    } else if (selectedChartType.value !== null) {
      await fetchStockHistory();
    } else {
      console.warn('조회할 주가 기간이나 분 단위가 선택되지 않았습니다.');
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response?.status === 404) {
        await router.push({ name: 'NotFound', params: { catchAll: '404' } });
      }
      console.log('error: ', error);
    }
  }
};

const updateInterval = (interval: number | null) => {
  selectedInterval.value = interval;
  selectedChartType.value = null;
};

const updateChartType = (type: number) => {
  selectedChartType.value = type;
  selectedInterval.value = null;
};

const fetchStockHistory = async () => {
  try {
    const response = await axios.get(`/api/stock/${props.ticker}/price/history`, {
      params: {
        dateType: selectedChartType.value  //0: 일봉, 1: 주봉, 2: 월봉
      }
    });
    stockPrices.value = response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response?.status === 404) {
        await router.push({ name: 'NotFound', params: { catchAll: '404' } });
      }
      console.log('error: ', error);
    }
  }
};

const fetchStockByMinutes = async () => {
  try {
    const response = await axios.get(`/api/stock/${props.ticker}/price/minutes`, {
      params: {
        intervalMinutes: selectedInterval.value
      }
    });
    stockPrices.value = response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response?.status === 404) {
        await router.push({ name: 'NotFound', params: { catchAll: '404' } });
      }
      console.log('error: ', error);
    }
  }
};

let mouseX = -1;
let mouseY = -1;
// 주가 차트를 그리는 함수
const renderPriceChart = async () => {
  await nextTick(); // DOM이 준비된 후 차트를 그리기 위해 nextTick 사용
  if (stockChartRef.value) {

    const ctx = stockChartRef.value.getContext('2d');
    if (ctx) {
      const existingChart = Chart.getChart(ctx);
      if (existingChart) {
        existingChart.destroy();
      }

      // 바 차트를 위한 데이터 배열
      const chart = new Chart(ctx, {
        type: 'candlestick',
        data: {
          labels: stockPrices.value.map(price => price.date).reverse(),
          datasets: [
            {
              data: stockPrices.value.map(price => ({
                x: price.date,
                o: parseFloat(price.open),   // 시가
                h: parseFloat(price.high),   // 고가
                l: parseFloat(price.low),    // 저가
                c: parseFloat(price.close),  // 종가
                chg: parseFloat(price.rate), // 등락율
                y: parseInt(price.volume)    // 거래량
              })).reverse(),
              yAxisID: 'y',  // 가격 축 설정
              barThickness: 2,
              order: 1,
              label: 'Candlestick'
            },
            {
              type: 'bar',
              data: stockPrices.value.map(price => ({
                x: price.date,
                y: parseInt(price.volume)  // 거래량은 y 값으로 설정해야 함
              })).reverse(),
              order: 2,
              barThickness: 2,
              label: 'Volume',
              yAxisID: 'volume', // 거래량 전용 축 설정
              backgroundColor: stockPrices.value.map(price =>
                price.close > price.open ? 'rgba(0, 191, 191, 0.5)' : 'rgba(255, 182, 193, 0.5)'
              ).reverse()
            }
          ]
        },
        options: {
          layout: {
            autoPadding: true
          },
          hover: {
            intersect: false
          },
          maintainAspectRatio: false,
          responsive: true,
          scales: {
            x: {
              type: 'category',
              reverse: false, //최신 날짜가 우측에 오도록 설정
              grid: {
                display: true,
                drawTicks: true,
                offset: false,
              },
              offset: true,
              // afterTickToLabelConversion: function(scaleInstance) {
              //   const ticks = scaleInstance.ticks;
              //   scaleInstance.ticks = ticks.map(tick => {
              //     const tickValue = tick.label;
              //
              //     // tickValue가 존재하고 문자열인 경우 처리
              //     if (typeof tickValue === 'string') {
              //       if (tickValue.length === 8) {
              //         // YYYYMMDD 형식
              //         const year = tickValue.substring(0, 4);
              //         const month = tickValue.substring(4, 6);
              //         const day = tickValue.substring(6, 8);
              //         return {
              //           ...tick,
              //           label: `${year}/${month}/${day}` // YYYY/MM/DD로 변환
              //         };
              //       } else if (tickValue.length === 14 && selectedChartType.value === null) {
              //         // YYYYMMDDHHmmss 형식
              //         const year = tickValue.substring(0, 4);
              //         const month = tickValue.substring(4, 6);
              //         const day = tickValue.substring(6, 8);
              //         const hour = tickValue.substring(8, 10);
              //         const minute = tickValue.substring(10, 12);
              //         // 월/일 시간:분 형식으로 변환
              //         return {
              //           ...tick,
              //           label: `${month}/${day} ${hour}:${minute}`
              //         };
              //       }
              //     }
              //
              //     // tickValue가 유효하지 않을 경우 기본 값을 반환
              //     return tick;
              //   });
              // },

              ticks: {
                autoSkip: false,
                font: {
                  size: 10,
                },
                callback: function(value, index, values) {
                  if ((index + 1) % 20 !== 0 || index === 0 || index === 99) {
                    return;
                  }

                  const reversedIndex = stockPrices.value.length - 1 - index;
                  const date = stockPrices.value[reversedIndex]?.date || '';
                  if (selectedChartType.value === null) {
                    return formatDateTime(date);
                  } else if (selectedInterval.value === null) {
                    return date.slice(0, 4) + '/' + date.slice(4, 6) + '/' + date.slice(6, 8);
                  }
                }
              }
            },
            y: {  //가격 축
              title: {
                display: false,
                text: 'Price'
              },
              beginAtZero: false,
              // grid: {
              //   display: true,
              //   drawTicks: true,
              //   tickLength: 4,
              // },
            },
            volume: { // 거래량 전용 축
              display: true,
              type: 'linear',
              position: 'right',
              beginAtZero: true,
              ticks: {
                display: false,
              },
              grid: {
                drawTicks: false,
                drawOnChartArea: false // 가격과 겹치는 것을 방지
              },
              afterDataLimits: (axis) => {
                axis.max = axis.max * 6; // 거래량 차트의 높이를 상대적으로 줄임
              }
            }
          },
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              enabled: false, // 기본 툴팁 비활성화
              intersect: false,
              external: function(context) {
                if (context.tooltip.opacity === 0 || context.tooltip.dataPoints[0].dataset.label !== 'Candlestick') {
                  return; // 캔들스틱 차트가 아닐 때는 툴팁을 표시하지 않음
                }
                let tooltipEl = document.getElementById('chartjs-tooltip');

                // 툴팁 엘리먼트가 없다면 생성
                if (!tooltipEl) {
                  tooltipEl = document.createElement('div');
                  tooltipEl.id = 'chartjs-tooltip';
                  tooltipEl.style.backgroundColor = 'rgba(0, 0, 0, 0.7)';
                  tooltipEl.style.borderRadius = '8px';
                  tooltipEl.style.color = 'white';
                  tooltipEl.style.opacity = String(1);
                  tooltipEl.style.pointerEvents = 'none';
                  tooltipEl.style.position = 'absolute';
                  tooltipEl.style.transform = 'translate(-50%, 0)';
                  tooltipEl.style.transition = 'opacity 0.2s ease, left 0.2s ease, top 0.2s ease';
                  // tooltipEl.style.padding = '10px';

                  document.body.appendChild(tooltipEl);
                }

                const tooltipModel = context.tooltip;

                // 툴팁 숨김 상태 처리
                if (tooltipModel.opacity === 0) {
                  tooltipEl.style.opacity = String(0);
                  return;
                }
                tooltipEl.style.opacity = String(1);

                // 툴팁의 텍스트 및 데이터 설정
                const dataIndex = tooltipModel.dataPoints[0].dataIndex;
                const data = tooltipModel.dataPoints[0].dataset.data[dataIndex];

                if (data != null && typeof data === 'object' && 'x' in data) {
                  if (selectedInterval.value === null) {
                    tooltipEl.innerHTML = `
                          <div style="background-color: #333; color: #fff; padding: 10px; border-radius: 10px; position: relative; font-size: 12px;">
                            <span>${formatDate(String(data.x))}</span><br>
                            <div style="display: flex; justify-content: space-between;">
                                <span>Open</span>
                                <span style="text-align: right; margin-left: 12px;">${data.o}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>High</span>
                                <span style="text-align: right; margin-left: 12px;">${data.h}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>Low</span>
                                <span style="text-align: right; margin-left: 12px;">${data.l}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>Close</span>
                                <span style="text-align: right; margin-left: 12px;">${data.c}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>Chg%</span>
                                <span style="text-align: right; margin-left: 12px;">${data.chg}%</span>
                            </div>
                             <div style="display: flex; justify-content: space-between;">
                                <span>Vol.</span>
                                <span style="text-align: right; margin-left: 12px;">
                                    ${data.y !== undefined ? formatVolume(data.y) : 'N/A'}
                                </span>
                            </div>
                            <div id="tooltip-arrow" style="width: 0; height: 0; border-left: 10px solid transparent;
                            border-right: 10px solid transparent; border-top: 10px solid #333;
                            position: absolute;"></div>
                          </div>`;
                  } else if (selectedChartType.value === null) {
                    tooltipEl.innerHTML = `
                          <div style="background-color: #333; color: #fff; padding: 10px; border-radius: 10px; position: relative; font-size: 12px;">
                            <span>${formatDateTime(String(data.x))}</span><br>
                            <div style="display: flex; justify-content: space-between;">
                                <span>Open</span>
                                <span style="text-align: right; margin-left: 12px;">${data.o}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>High</span>
                                <span style="text-align: right; margin-left: 12px;">${data.h}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>Low</span>
                                <span style="text-align: right; margin-left: 12px;">${data.l}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>Close</span>
                                <span style="text-align: right; margin-left: 12px;">${data.c}</span>
                            </div>
                            <div id="tooltip-arrow" style="width: 0; height: 0; border-left: 10px solid transparent;
                            border-right: 10px solid transparent; border-top: 10px solid #333;
                            position: absolute;"></div>
                          </div>`;
                  }

                  const yScale = context.chart.scales['y'];

                  // 시가와 종가의 y좌표 계산
                  const openY = yScale.getPixelForValue(data.o);
                  const closeY = yScale.getPixelForValue(data.c);

                  // 툴팁의 위치 설정
                  const chartRect = context.chart.canvas.getBoundingClientRect();
                  const tooltipWidth = tooltipEl.offsetWidth;
                  const tooltipHeight = tooltipEl.offsetHeight;

                  // 뾰족한 부분 동적 조정 (기본적으로 아래쪽에)
                  const arrowEl = document.getElementById('tooltip-arrow');
                  if (!(arrowEl instanceof HTMLElement)) {
                    return;
                  }
                  let tooltipX;
                  //툴팁의 X, Y 좌표 계산 (뾰족한 부분을 기준으로 설정)
                  if (tooltipModel.caretX <= chartRect.width / 2) {
                    tooltipX = tooltipModel.caretX + chartRect.left + tooltipWidth / 2 + 10;
                    arrowEl.style.left = '-15px';
                    arrowEl.style.transform = 'rotate(90deg)';
                  } else {
                    tooltipX = tooltipModel.caretX + chartRect.left - tooltipWidth / 2 - 10;
                    arrowEl.style.left = tooltipWidth - 5 + 'px';
                    arrowEl.style.transform = 'rotate(270deg)';
                  }

                  // const tooltipY = chartRect.top + tooltipModel.dataPoints[0].element.y - tooltipHeight / 2;
                  let tooltipY;
                  tooltipEl.style.height = '100px';
                  if (selectedInterval.value === null) {
                    tooltipY = (openY + closeY) / 2 - 0.78 * tooltipHeight + chartRect.top;
                    // tooltipY = (openY + closeY) / 2 + 1.3 * tooltipHeight;
                  } else {
                    tooltipY = (openY + closeY) / 2 - 0.6 * tooltipHeight + chartRect.top;
                    // tooltipY = (openY + closeY) / 2 + 1.5 * tooltipHeight;
                  }
                  arrowEl.style.top = '50%';

                  tooltipEl.style.position = 'absolute';
                  tooltipEl.style.left = `${tooltipX}px`;
                  tooltipEl.style.top = `${tooltipY}px`;
                  tooltipEl.style.pointerEvents = 'none';
                }
              }
            }
          }
        },
        plugins: [{
          id: 'crosshairLines',
          afterDraw(chart) {
            const ctx = chart.ctx;
            const xScale = chart.scales.x;
            const yScale = chart.scales.y;

            // 마우스가 차트 안에 있을 때만 그리기
            if (mouseX >= xScale.left && mouseX <= xScale.right &&
              mouseY >= yScale.top && mouseY <= yScale.bottom) {

              // 가로, 세로 선을 그리기
              ctx.save();
              ctx.setLineDash([5, 3]);  //[길이, 간격]
              ctx.beginPath();
              ctx.moveTo(xScale.left, mouseY);
              ctx.lineTo(xScale.right, mouseY);
              ctx.moveTo(mouseX, yScale.top);
              ctx.lineTo(mouseX, yScale.bottom);
              ctx.lineWidth = 1;
              ctx.strokeStyle = 'rgb(13,110,253)';
              ctx.stroke();

              // 마우스 위치에 해당하는 y축 값(주가)을 계산
              const priceAtMouseY = yScale?.getValueForPixel ? yScale.getValueForPixel(mouseY)?.toFixed(2) : 'N/A';
              const textX = xScale.left + 8;

              // y축 왼쪽 끝에 주가 표시
              ctx.font = '12px Arial';
              ctx.fillStyle = 'black';
              ctx.textAlign = 'right';
              if (priceAtMouseY !== undefined) {
                const textWidth = ctx.measureText(priceAtMouseY).width;
                const textHeight = 14; // 텍스트 높이를 약간 크게 설정

                const radius = 6; // 코너의 둥글기 설정

                // 사각형의 좌표
                const rectX = textX - 2;
                const rectY = mouseY - textHeight / 2 - 2;
                const rectWidth = textWidth + 12;
                const rectHeight = textHeight + 4;

                // 둥근 사각형을 그리기
                ctx.beginPath();
                ctx.moveTo(rectX + radius, rectY);
                ctx.lineTo(rectX + rectWidth - radius, rectY);
                ctx.arcTo(rectX + rectWidth, rectY, rectX + rectWidth, rectY + radius, radius);
                ctx.lineTo(rectX + rectWidth, rectY + rectHeight - radius);
                ctx.arcTo(rectX + rectWidth, rectY + rectHeight, rectX + rectWidth - radius, rectY + rectHeight, radius);
                ctx.lineTo(rectX + radius, rectY + rectHeight);
                ctx.arcTo(rectX, rectY + rectHeight, rectX, rectY + rectHeight - radius, radius);
                ctx.lineTo(rectX, rectY + radius);
                ctx.arcTo(rectX, rectY, rectX + radius, rectY, radius);
                ctx.closePath();

                // 사각형 배경을 채우기
                ctx.fillStyle = 'rgb(13, 110, 253)';
                ctx.fill();

                // 텍스트 그리기 (텍스트의 중앙이 사각형 중앙에 오도록 배치)
                ctx.fillStyle = 'white';
                ctx.textAlign = 'left';
                ctx.textBaseline = 'middle';
                ctx.fillText(priceAtMouseY, rectX + 6, mouseY);
              }
              ctx.restore();
            }
          }
        }]
      });

      stockChartRef.value.addEventListener('mousemove', (event) => {
        if (stockChartRef.value != null) {
          const rect = stockChartRef.value.getBoundingClientRect();
          mouseX = event.clientX - rect.left;
          mouseY = event.clientY - rect.top;
          // chart.update('none'); // 차트 업데이트 (재렌더링 없이)
        }
      });

      // 마우스가 차트 밖으로 나갔을 때 좌표 초기화
      stockChartRef.value.addEventListener('mouseout', () => {
        mouseX = -1;
        mouseY = -1;
        // chart.update('none');

        const tooltipEl = document.getElementById('chartjs-tooltip');
        if (tooltipEl) {
          tooltipEl.style.opacity = String(0);
        }
      });

    }
  }
};

const formatDate = (dateString: string): string => {
  const formattedDate = `${dateString.slice(0, 4)}-${dateString.slice(4, 6)}-${dateString.slice(6, 8)}`;
  const date = new Date(formattedDate);
  const dayOfWeekNumber = date.getDay();
  const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  return `${dateString.slice(0, 4)}/${dateString.slice(4, 6)}/${dateString.slice(6, 8)}` + ' ' + daysOfWeek[dayOfWeekNumber];
};

function formatDateTime(dateString: string): string {
  // 날짜 문자열에서 월, 일, 시간, 분, 초를 추출합니다.
  const month = dateString.substring(4, 6);
  const day = dateString.substring(6, 8);
  const hour = dateString.substring(8, 10);
  const minute = dateString.substring(10, 12);

  // MM/dd hh:mm 형식으로 변환
  return `${month}/${day} ${hour}:${minute}`;
}

function formatVolume(volume: number): string {
  if (volume >= 1_000_000) {
    return (volume / 1_000_000).toFixed(2) + 'M';
  } else if (volume >= 1_000) {
    return (volume / 1_000).toFixed(2) + 'K';
  } else {
    return volume.toString();
  }
}

// 색상 스타일을 반환하는 함수
const getStyle = (rate: string | undefined) => {
  if (rate === undefined) return {};
  const rateNum = parseFloat(rate);

  if (rateNum > 0) {
    return { color: 'green' };
  } else if (rateNum < 0) {
    return { color: 'red' };
  } else {
    return { color: 'black' };
  }
};

// rate 값을 포맷팅하는 함수
const formatRate = (rate: string | undefined) => {
  if (rate === undefined) return '';
  const rateNum = parseFloat(rate);

  // 양수면 앞에 +를 붙임
  if (rateNum > 0) {
    return `+${rateNum.toFixed(2)}`; // 소수점 2자리까지 표시
  } else {
    return rateNum.toFixed(2); // 음수 또는 0은 그대로 반환
  }
};

watch([() => props.ticker,
  () => selectedChartType.value,
  () => selectedInterval.value
], async () => {
  await fetchStockPrice()
    .then(renderPriceChart);
});

// 컴포넌트가 마운트될 때 주식 데이터를 가져오고 차트를 렌더링합니다.
onMounted(async () => {
  await fetchStockPrice();  // 데이터 가져오기
  await renderPriceChart();  // 주가 차트 그리기
});
</script>

<style scoped>
</style>
