<template>
  <div class="mb-1">
    <strong class="me-2 fs-3" :style="getStyle(rate)">${{ last }}</strong>
    <strong
      :style="[getStyle(rate), { backgroundColor: '#f8f9fa', borderRadius: '10px', padding: '2px' }]">
      {{ formatRate(diff) }} {{ formatRate(rate) }}%
    </strong>
  </div>
</template>

<script lang="ts" setup>
const props = defineProps<{
  last: string;
  diff: string;
  rate: string;
}>();

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
</script>

<style scoped></style>
