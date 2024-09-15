<template>
  <div class="card h-100">
    <div class="card-body">
      <h5 class="card-title">{{ title }}</h5>
      <p class="card-text">
        {{ content }}
      </p>
      <p class="text-muted mb-0">{{ createdAt }}</p>
    </div>
  </div>
</template>

<script lang="ts" setup>
defineProps({
  title: {
    String,
    required: true,
  },
  content: {
    type: String,
  },
  createdAt: {
    type: [String, Date, Number],
    required: true,
  },
});
</script>

<style scoped>
.card-body {
  height: auto;
}

.card-text {
  display: -webkit-box;
  -webkit-line-clamp: 1; /* 원하는 줄 수 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal; /* 줄 바꿈을 허용 */
}
</style>
