<template>
  <div class="chart-options" style="display: flex; align-items: center;">
    <select
      class="btn-select me-1"
      :class="{'active': selectedInterval !== null}"
      @change="handleIntervalChange"
      :value="selectedInterval"
      aria-label="Default select example">
      <option value="" disabled selected>Time Interval</option>
      <option value="1">1 min</option>
      <option value="3">3 mins</option>
      <option value="5">5 mins</option>
      <option value="10">10 mins</option>
      <option value="30">30 mins</option>
      <option value="60">1 hour</option>
      <option value="120">2 hours</option>
      <option value="240">4 hours</option>
    </select>

    <button
      class="btn-option ms-1"
      @click="emitChartTypeChange(0)"
      :class="{'active': selectedChartType === 0}">
      Daily
    </button>
    <button
      class="btn-option ms-1"
      @click="emitChartTypeChange(1)"
      :class="{'active': selectedChartType === 1}">
      Weekly
    </button>
    <button
      class="btn-option ms-1"
      @click="emitChartTypeChange(2)"
      :class="{'active': selectedChartType === 2}">
      Monthly
    </button>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  selectedInterval: {
    type: null,
    default: null
  },
  selectedChartType: {
    type: null,
    required: true
  }
});

const emit = defineEmits(['interval-change', 'chart-type-change']);

const handleIntervalChange = (event: Event) => {
  const interval = (event.target as HTMLSelectElement).value;
  emit('interval-change', interval);
};

const emitChartTypeChange = (type: number) => {
  emit('chart-type-change', type);
};
</script>

<style scoped>
/* 기본 버튼 및 select 스타일 */
.btn-select, .btn-option {
  background-color: white;
  border: none;
  color: black;
  cursor: pointer;
  font-size: 14px;
  min-width: 60px; /* 버튼 크기를 맞추기 위해 최소 너비 설정 */
  text-align: center;
  height: 25px; /* 높이 설정 */
  border-radius: 6px; /* 모서리를 둥글게 설정 */
  outline: none;
}

/* hover 상태 스타일 */
.btn-select:hover, .btn-option:hover {
  background-color: lightgrey;
  color: black;
  border-radius: 6px; /* hover 상태에서도 둥근 모서리 유지 */
}

/* 선택된 버튼에 대한 스타일 */
.btn-select.active, .btn-option.active {
  background-color: var(--bs-primary);
  color: white;
  border-radius: 6px; /* 선택된 버튼에도 둥근 모서리 적용 */
}

/* option 요소의 스타일을 지정 */
option {
  background-color: white; /* option 요소 배경색을 흰색으로 */
  color: black; /* 글자색을 검정으로 고정 */
}

/* 선택된 버튼의 hover 상태 유지 */
.btn-select.active:hover, .btn-option.active:hover {
  background-color: var(--bs-primary);
  color: white;
  cursor: default;
  border-radius: 6px; /* 선택된 버튼에도 둥근 모서리 적용 */
}
</style>
