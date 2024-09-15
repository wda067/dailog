<template>
  <form @submit.prevent="onSearch">
    <div class="row align-items-center">
      <div class="col-auto" style="padding-right: 1px">
        <select
          v-model="localSearchParams.searchParams.searchDateType"
          class="form-select"
        >
          <option selected value="">전체 기간</option>
          <option value="1w">최근 1주일</option>
          <option value="1m">최근 1개월</option>
          <option value="6m">최근 6개월</option>
          <option value="1y">최근 1년</option>
        </select>
      </div>
      <div class="col-auto" style="padding-right: 1px">
        <select
          v-model="localSearchParams.searchParams.searchType"
          class="form-select"
        >
          <option
            v-for="option in searchTypeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.text }}
          </option>
        </select>
      </div>
      <div class="col" style="padding-right: 1px">
        <input
          v-model="localSearchParams.searchParams.searchQuery"
          class="form-control"
          placeholder="검색어를 입력하세요."
          type="text"
        />
      </div>
      <div class="col-auto">
        <button class="btn btn-primary" style="float: right" type="submit">
          <i class="bi bi-search"></i>
        </button>
      </div>
    </div>
  </form>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';

const props = defineProps({
  searchParams: {
    type: Object,
    required: true,
  },
  type: { type: String },
});

const localSearchParams = ref({ ...props });

const emit = defineEmits(['search', 'changeSize']);
const onSearch = () => {
  emit('search');
};
const searchTypeOptions = computed(() => {
  if (localSearchParams.value.type === 'post') {
    return [
      { value: 'titleOrContent', text: '제목 또는 내용' },
      { value: 'title', text: '제목' },
      { value: 'content', text: '내용' },
    ];
  } else if (localSearchParams.value.type === 'member') {
    return [
      { value: 'email', text: '이메일' },
      { value: 'name', text: '이름' },
      { value: 'role', text: '등급' },
    ];
  } else {
    return [];
  }
});
</script>

<style scoped>
@media (min-width: 1200px) {
  .row {
    padding-left: 16rem; /* 화면 너비가 1200px 이상일 때 */
    padding-right: 16rem;
  }
}
</style>
