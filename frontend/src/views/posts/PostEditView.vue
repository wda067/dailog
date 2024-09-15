<template>
  <div>
    <h2>게시글 수정</h2>
    <hr class="my-4" />
    <PostForm
      v-model:content="form.content"
      v-model:title="form.title"
      @submit.prevent="edit"
    >
      <template #actions>
        <button
          class="btn btn-outline-danger"
          type="button"
          @click="moveToPost"
        >
          취소
        </button>
        <button class="btn btn-primary">수정</button>
      </template>
    </PostForm>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from 'vue-router';
import PostForm from '@/components/posts/PostForm.vue';
import { useAlert } from '@/composables/useAlert';

const { vAlert, vSuccess } = useAlert();

const route = useRoute();
const router = useRouter();
const id = route.params.id;

const form = ref({
  title: '',
  content: '',
});

const fetchPost = async () => {
  try {
    const { data } = await axios.get(`/api/posts/${id}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('access')}`,
      },
    });
    setForm(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      vAlert(error.message);
    } else {
      vAlert('게시글을 불러오는 중 오류가 발생했습니다.');
    }
  }
};

const setForm = ({ title, content }: { title: string; content: string }) => {
  form.value.title = title;
  form.value.content = content;
};

fetchPost();

const edit = async () => {
  try {
    await axios.patch(
      `/api/posts/${id}`,
      { ...form.value },
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('access')}`,
        },
      },
    );
    router
      .replace({ name: 'PostDetail', params: { id } })
      .then(() => vSuccess('수정이 완료되었습니다.'));
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
      vAlert('게시글 수정 중 오류가 발생했습니다.');
    }
  }
};

const moveToPost = () => router.push({ name: 'PostDetail', params: { id } });
</script>

<style scoped></style>
