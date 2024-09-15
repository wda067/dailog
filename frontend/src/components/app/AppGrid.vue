<template>
  <div class="row g-3">
    <div v-for="(item, index) in items" :key="index" :class="colClass">
      <slot :index="index" :item="item"></slot>
    </div>
  </div>
</template>

<script lang="ts" setup>
defineProps({
  items: {
    type: Array,
    required: true,
  },
  colClass: {
    type: String,
    default: 'col-4',
  },
});
</script>

<style scoped></style>
