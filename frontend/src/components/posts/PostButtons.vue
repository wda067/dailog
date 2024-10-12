<template>
  <div class="row g-2">
    <div class="col-auto">
      <button class="btn btn-outline-dark" @click="$emit('move-to-prev')">
        이전 글
      </button>
    </div>
    <div class="col-auto">
      <button class="btn btn-outline-dark" @click="$emit('move-to-next')">
        다음 글
      </button>
    </div>
    <div class="col-auto">
      <button class="btn btn-outline-dark" @click="$emit('move-to-list')">
        목록
      </button>
    </div>
    <div v-if="hasAuthorized" class="col-auto">
      <button class="btn btn-outline-dark" @click="$emit('move-to-edit')">
        수정
      </button>
    </div>
    <div v-if="hasAuthorized && !isAdmin" class="col-auto">
      <button class="btn btn-outline-danger" @click="$emit('remove')">
        삭제
      </button>
    </div>
    <div v-if="isAdmin" class="col-auto">
      <button class="btn btn-outline-danger" @click="$emit('remove:admin')">
        삭제(관리자용)
      </button>
    </div>
  </div>
</template>

<script lang="ts" setup>
defineProps({
  postId: {
    type: String,
    required: true,
  },
  hasAuthorized: {
    type: Boolean,
    required: true,
  },
  isAdmin: {
    type: Boolean,
    required: true,
  }
});
</script>

<style scoped></style>
