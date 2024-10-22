<template>
  <div class="chart-container">
    <div>
      <h3 class="ticker-title">SPY</h3>
      <StockChart ticker="SPY" />
    </div>
    <div>
      <h3 class="ticker-title">QQQ</h3>
      <StockChart ticker="QQQ" />
    </div>
    <div>
      <h3 class="ticker-title">DIA</h3>
      <StockChart ticker="DIA" />
    </div>
  </div>
</template>

<script setup lang="ts">
import StockChart from '@/components/stock/StockChart.vue'; // Chart 컴포넌트 임포트
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
  font-size: 1.2rem; /* 티커 제목 크기 */
  margin-bottom: 10px; /* 티커와 차트 사이 간격 */
}
</style>
