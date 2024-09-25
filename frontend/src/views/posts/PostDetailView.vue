<template>
  <div>
    <h1>{{ post.title }}</h1>
    <p class="fw-bold m-0">{{ post.nickname }}</p>
    <p class="text-muted mb-1">{{ post.createdAt }} 조회 {{ post.views }}</p>
    <hr class="mt-1" />
    <p style="white-space: pre-line">{{ post.content }}</p>
    <hr class="mt-4 mb-2" />

    <PostButtons
      :hasAuthorized="hasAuthorized"
      :postId="props.id"
      @remove="remove"
      @move-to-prev="moveToPrev"
      @move-to-next="moveToNext"
      @move-to-list="moveToList"
      @move-to-edit="moveToEdit"
    />

    <CommentSection :postId="props.id" />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useAlert } from '@/composables/useAlert';
import PostButtons from '@/components/posts/PostButtons.vue';
import CommentSection from '@/components/comments/CommentSection.vue';
import { useAuthStore } from '@/stores/auth';

const { vAlert, vSuccess } = useAlert();

const router = useRouter();
const authStore = useAuthStore();
const profile = computed(() => authStore.user);
const currentUserId = computed(() => profile.value?.id);
const hasAuthorized = computed(
  () => post.value.memberId === currentUserId.value,
);

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
});

interface Post {
  title: string;
  content: string;
  createdAt: string;
  memberId: string;
  nickname: string;
  views: number;
}

const post = ref<Post>({
  content: '',
  createdAt: '',
  memberId: '',
  title: '',
  nickname: '',
  views: 0,
});

const fetchPost = async () => {
  try {
    const { data } = await axios.get(`/api/posts/${props.id}`);
    setPost(data);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response?.status === 404) {
        await router.push({ name: 'NotFound', params: { catchAll: '404' } });
      }
      console.log('error: ', error);
    }
  }
};

const setPost = ({
  title,
  content,
  createdAt,
  memberId,
  nickname,
  views,
}: Post) => {
  post.value.title = title;
  post.value.content = content;
  post.value.createdAt = createdAt;
  post.value.memberId = memberId;
  post.value.nickname = nickname;
  post.value.views = views;
};
watch(() => props.id, fetchPost);

//Button
const remove = async () => {
  try {
    if (!confirm('게시글을 삭제하시겠습니까?')) {
      return;
    }
    await axios.delete(`/api/posts/${props.id}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('access')}`,
      },
    });
    router
      .push({ name: 'PostList' })
      .then(() => vSuccess('게시글이 삭제되었습니다.'));
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const data = error.response?.data;
      if (data) {
        vAlert(data.message);
      } else {
        vAlert('게시글을 삭제하는데 실패했습니다.');
      }
    }
  }
};

const moveToPrev = async () => {
  try {
    const { data } = await axios.get(`/api/posts/${props.id}/prev`);
    router.replace({ name: 'PostDetail', params: { id: data.id } });
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 404) {
        vAlert('이전 글이 없습니다.');
      } else {
        console.log('error: ', error);
        vAlert('이전 글로 이동하는 데 실패했습니다.');
      }
    }
  }
};

const moveToNext = async () => {
  try {
    const { data } = await axios.get(`/api/posts/${props.id}/next`);
    await router.replace({ name: 'PostDetail', params: { id: data.id } });
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 404) {
        vAlert('다음 글이 없습니다.');
      } else {
        console.log('error: ', error);
        vAlert('다음 글로 이동하는 데 실패했습니다.');
      }
    }
  }
};

const moveToList = () =>
  router.push({
    name: 'PostList',
  });

const moveToEdit = () =>
  router.push({
    name: 'PostEdit',
    params: { id: props.id },
  });

onMounted(fetchPost);
</script>

<style scoped></style>
