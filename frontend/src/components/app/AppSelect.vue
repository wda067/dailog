<template>
  <select :value="size" class="form-select" @change="onSizeChange">
    <option v-for="option in options" :key="option.value" :value="option.value">
      {{ option.text }}
    </option>
  </select>
</template>

<script lang="ts" setup>
import { defineEmits, defineProps } from 'vue';

defineProps({
  size: {
    type: Number,
    required: true,
  },
  options: {
    type: Array as () => Array<{ value: number; text: string }>,
    required: true,
  },
});

const emits = defineEmits(['changeSize']);

function onSizeChange(event: Event) {
  const target = event.target as HTMLSelectElement;
  if (target) {
    emits('changeSize', Number(target.value));
  }
}
</script>

<style scoped>
.form-select {
  display: inline-block;
  width: auto;
}
</style>
