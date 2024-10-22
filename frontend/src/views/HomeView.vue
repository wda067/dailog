<template>
  <div class="chart-container">
    <div v-for="(detail, ticker) in stockDetails" :key="ticker">
      <h2 class="ticker-title">{{ ticker }}</h2>
      <StockPrice
        :last="detail.last"
        :diff="detail.diff"
        :rate="detail.rate" />
      <StockChart :ticker="ticker" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import StockChart from '@/components/stock/StockChart.vue';
import StockPrice from '@/components/stock/StockPrice.vue';
import axios from 'axios';

// 주식 데이터를 저장할 객체
const stockDetails = ref<Record<string, { last: string; diff: string; rate: string }>>({
  SPY: { last: '', diff: '', rate: '' },
  QQQ: { last: '', diff: '', rate: '' },
  DIA: { last: '', diff: '', rate: '' },
});

// 각 티커에 대한 데이터를 가져오는 함수
const fetchStockDetails = async (ticker: string) => {
  try {
    const { data } = await axios.get(`/api/stock/${ticker}`);
    stockDetails.value[ticker] = {
      last: data.last,
      diff: data.diff,
      rate: data.rate,
    };
  } catch (error) {
    console.error(`Error fetching data for ${ticker}:`, error);
  }
};

// 컴포넌트가 마운트될 때 세 가지 티커에 대해 데이터를 가져옴
onMounted(() => {
  ['SPY', 'QQQ', 'DIA'].forEach((ticker) => {
    fetchStockDetails(ticker);
  });
});
</script>

<style scoped>
.chart-container {
  display: flex;
  justify-content: space-between;
  gap: 20px; /* 차트 간의 간격 */
}

.chart-container > div {
  flex: 1; /* 모든 차트가 동일한 너비로 차지 */
}

.ticker-title {
  text-align: center; /* 티커를 가운데 정렬 */
  font-weight: bolder;
}
</style>
