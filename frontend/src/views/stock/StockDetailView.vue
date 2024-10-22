<template>
  <div v-if="stockDetail && stockInformation"
       style="width: 50%; height: 250px; position: relative;">
    <h2 style="font-weight: bolder">{{ props.ticker }}</h2>
    <div class="text-muted" style="color: #555;">{{ stockInformation.name }} ·
      <span v-if="stockDetail.sector !== ''">
         {{ stockDetail.sector }}
      </span>
      <span v-else>ETF</span>
    </div>
    <div class="mb-1">
      <strong class="me-2" :style="getStyle(stockDetail.rate)">${{ stockDetail.last }}</strong>
      <strong
        :style="[getStyle(stockDetail.rate), { backgroundColor: '#f8f9fa', borderRadius: '10px', padding: '2px' }]">
        {{ formatRate(stockDetail.rate) }}%
      </strong>
    </div>
    <StockChart :ticker="props.ticker"/>
    <br>
    <div class="detail-container">
      <div class="detail-item">
        <span class="label">시가총액</span>
        <span class="value">{{ stockDetail.marketCap }}</span>
        <div class="divider"></div>
        <span class="label">상장주식수</span>
        <span class="value">{{ stockDetail?.totalShares }}</span>
      </div>
      <div class="detail-item">
        <span class="label">52주 최고</span>
        <span class="value">${{ stockDetail.high52Weeks }} ({{ formatDate(stockDetail.high52WeeksDate) }})</span>
        <div class="divider"></div>
        <span class="label">52주 최저</span>
        <span class="value">${{ stockDetail.low52Weeks }} ({{ formatDate(stockDetail.low52WeeksDate) }})</span>
      </div>
      <div class="detail-item">
        <span class="label">PER</span>
        <span class="value">{{ stockDetail.per }}</span>
        <div class="divider"></div>
        <span class="label">PBR</span>
        <span class="value">{{ stockDetail.pbr }}</span>
      </div>
      <div class="detail-item">
        <span class="label">EPS</span>
        <span class="value">{{ stockDetail.eps }}</span>
        <div class="divider"></div>
        <span class="label">BPS</span>
        <span class="value">{{ stockDetail.bps }}</span>
      </div>
    </div>
  </div>

</template>

<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import StockChart from '@/components/stock/StockChart.vue';

//주식 데이터 타입 정의
const stockDetail = ref<{
  open: string;
  high: string;
  low: string;
  last: string;
  base: string;
  marketCap: string;
  high52Weeks: string;
  high52WeeksDate: string;
  low52Weeks: string;
  low52WeeksDate: string;
  per: string;
  pbr: string;
  eps: string;
  bps: string;
  totalShares: string;
  sector: string;
  rate: string;
}>();

const stockInformation = ref<{
  country: string;
  market: string;
  name: string;
}>();

const props = defineProps<{ ticker: string }>();
const router = useRouter();

const fetchStockDetail = async () => {
  try {
    const { data } = await axios.get(`/api/stock/${props.ticker}`);
    stockDetail.value = data;
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

const fetchStockInformation = async () => {
  try {
    const { data } = await axios.get(`/api/stock/${props.ticker}/information`);
    stockInformation.value = data;
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

const formatDate = (dateString: string): string => {
  const formattedDate = `${dateString.slice(0, 4)}-${dateString.slice(4, 6)}-${dateString.slice(6, 8)}`;
  const date = new Date(formattedDate);
  const dayOfWeekNumber = date.getDay();
  const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  return `${dateString.slice(0, 4)}/${dateString.slice(4, 6)}/${dateString.slice(6, 8)}` + ' ' + daysOfWeek[dayOfWeekNumber];
};

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

const formatRate = (rate: string | undefined) => {
  if (rate === undefined) return '';
  const rateNum = parseFloat(rate);

  if (rateNum > 0) {
    return `+${rateNum.toFixed(2)}`;
  } else {
    return rateNum.toFixed(2);
  }
};

watch(() => props.ticker, async () => {
  await fetchStockDetail();
  await fetchStockInformation();
});

onMounted(async () => {
  await fetchStockDetail();
  await fetchStockInformation();
});
</script>

<style scoped>
.detail-container {
  display: flex;
  flex-direction: column;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #e9ecef;
}

.detail-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: bold;
  color: #495057;
  text-align: left; /* 왼쪽 정렬 */
  flex: 1; /* 남은 공간을 차지하여 균형 맞춤 */
}

.value {
  color: #6c757d;
  text-align: right; /* 오른쪽 정렬 */
  flex: 1; /* 남은 공간을 차지하여 균형 맞춤 */
}

.divider {
  height: 20px; /* 선의 높이 */
  width: 1px; /* 선의 두께 */
  background-color: #e9ecef; /* 선의 색상 */
  margin: 0 20px; /* 선과 항목 사이의 여백 */
}
</style>
