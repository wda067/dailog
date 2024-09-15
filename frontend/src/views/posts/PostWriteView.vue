<template>
  <div>
    <h2>게시글 쓰기</h2>
    <hr class="my-4" />
    <PostForm
      v-model:title="form.title"
      v-model:content="form.content"
      @submit.prevent="write"
    >
      <template #actions>
        <button class="btn btn-outline-dark" type="button" @click="goListPage">
          목록
        </button>
        <button class="btn btn-primary">저장</button>
      </template>
    </PostForm>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import PostForm from '@/components/posts/PostForm.vue';
import { useAlert } from '@/composables/useAlert';
import axiosInstance from '@/composables/useApi';

const { vAlert, vSuccess } = useAlert();

const form = ref({
  title: '',
  content: '',
});

const router = useRouter();

const write = async () => {
  try {
    await axiosInstance.post('/api/posts', {
      title: form.value.title,
      content: form.value.content,
    });
    router
      .replace({ name: 'PostList' })
      .then(() => vSuccess('저장이 완료되었습니다.'));
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      const { validation } = response?.data || {};

      if (!response) {
        vAlert('응답을 받을 수 없습니다.');
      }
      if (validation.title) {
        vAlert(validation?.title);
      }
      if (validation.content) {
        vAlert(validation?.content);
      }
      if (!(validation.title || validation.content)) {
        vAlert(response?.data.message);
      }
    } else {
      vAlert('게시글 작성 중 오류가 발생했습니다.');
    }
  }
};

const goListPage = () => {
  router.push({ name: 'PostList' });
};
</script>

<style scoped></style>
