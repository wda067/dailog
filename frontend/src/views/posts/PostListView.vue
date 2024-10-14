<template>
  <div>
    <h2>게시글 목록</h2>
    <hr class="my-4" />

    <AppLoading v-if="loading" />

    <AppError v-else-if="error" :message="getErrorMessage()" />

    <template v-else>
      <PostTable
        :items="posts"
        @postClick="moveToDetail"
        @sortByLikes="toggleSort"
      />
      <div
        class="d-grid gap-2 d-md-flex justify-content-md-end align-items-center"
      >
        <AppSelect
          :options="selectOptions"
          :size="params.size"
          @changeSize="updateSize"
        />
        <button class="btn btn-primary me-md-2" @click="moveToWrite">
          글 쓰기
        </button>
      </div>
      <AppPagination
        :current-page="params.page"
        :page-count="pageCount"
        @page="page => (params.page = page)"
      />
      <AppFilter :searchParams="params" :type="'post'" @search="updateParams" />
    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import axios, { AxiosError } from 'axios';
import router from '@/router';
import AppPagination from '@/components/app/AppPagination.vue';
import AppFilter from '@/components/app/AppFilter.vue';
import PostTable from '@/components/posts/PostTable.vue';
import AppLoading from '@/components/app/AppLoading.vue';
import AppError from '@/components/app/AppError.vue';
import AppSelect from '@/components/app/AppSelect.vue';

const error = ref<null | AxiosError>(null);
const loading = ref(false);
const toggleSort = () => {
  params.value.sortByLikes = !params.value.sortByLikes;
  fetchPosts();
};

const posts = ref(
  [] as Array<{
    id: string;
    title: string;
    createdAt: string;
    nickname: string;
    commentCount: number;
    views: number;
    likes: number;
  }>,
);

const totalCount = ref(0);
const params = ref({
  page: 1,
  size: 10,
  sortByLikes: false,
  searchDateType: '',
  searchType: 'titleOrContent',
  searchQuery: '',
});

const pageCount = computed(() =>
  Math.ceil(totalCount.value / params.value.size),
);

const fetchPosts = async () => {
  try {
    loading.value = true;
    const { data } = await axios.get('/api/posts', { params: params.value });
    posts.value = data.items;
    totalCount.value = data.totalCount;
  } catch (e: unknown) {
    if (axios.isAxiosError(e)) {
      error.value = e;
    } else {
      console.error('Failed to get posts:', e);
    }
  } finally {
    loading.value = false;
  }
};

onMounted(fetchPosts);

const updateParams = () => {
  params.value.page = 1;
  fetchPosts();
};

const updateSize = (newSize: number) => {
  params.value.size = newSize;
  updateParams();
};

watch(
  [() => params.value.page, params.value.sortByLikes],
  () => {
    fetchPosts();
  },
);

const moveToWrite = () => {
  router.push({ name: 'PostWrite' });
};

const moveToDetail = (id: string) => {
  router.push({ name: 'PostDetail', params: { id } });
};

const selectOptions = [
  { value: 10, text: '10개씩' },
  { value: 20, text: '20개씩' },
  { value: 50, text: '50개씩' },
];

const getErrorMessage = () => {
  if (error.value?.response) {
    return error.value.response.statusText;
  }
  return 'Unknown error occurred.';
};
</script>

<style scoped></style>
