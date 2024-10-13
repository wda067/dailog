<template>
  <div>
    <h2>{{ post.title }}</h2>
    <p class="fw-bold m-0">{{ post.nickname }}</p>
    <p class="m-0">
      <small class="text-muted">
        {{ post.createdAt }} 조회 {{ post.views }}
      </small>
    </p>
    <hr class="mt-1 mb-1" />
    <p style="white-space: normal">{{ post.content }}</p>

    <div class="d-flex justify-content-between align-items-center">
      <div class="d-flex align-items-center">
        <!--좋아요 버튼-->
        <a class="mb-0 me-1 text-reset text-decoration-none" @click.prevent="toggleLike">
          <span v-if="isLiked"><i class="bi bi-suit-heart-fill"></i></span>
          <span v-else><i class="bi bi-suit-heart"></i></span>
        </a>
        <!--좋아요누른 회원 목록-->
        <a class="mb-0 text-reset text-decoration-none" @click.prevent="toggleLikeMembers">
          좋아요<strong class="ms-1">{{ post.likes }}</strong>
        </a>
        <a class="mb-0 ms-3 text-reset text-decoration-none" @click.prevent="toggleComments">
          <i class="me-1 bi bi-chat-text"></i>댓글<strong class="ms-1">{{ post.commentCount }}</strong>
        </a>
      </div>

      <div>
        <PostButtons
          :hasAuthorized="hasAuthorized"
          :postId="props.id"
          :isAdmin="isAdmin"
          @remove="remove"
          @move-to-prev="moveToPrev"
          @move-to-next="moveToNext"
          @move-to-list="moveToList"
          @move-to-edit="moveToEdit"
          @remove:admin="removeByAdmin"
        />
      </div>
    </div>
    <hr class="mt-1 mb-4" />

    <LikeMembers
      v-if="isLikeMembersVisible"
      :members="members"
      :current-page="params.page"
      :page-count="pageCount"
      @page="page => (params.page = page)"
    />

    <CommentSection
      v-if="!isLikeMembersVisible"
      :postId="props.id"
      :commentCount="post.commentCount"
      @comment:changed="fetchPost"
    />
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
import axiosInstance from '@/composables/useApi';
import LikeMembers from '@/components/likes/LikeMembers.vue';

const { vAlert, vSuccess } = useAlert();

const router = useRouter();
const authStore = useAuthStore();
const profile = computed(() => authStore.user);
const currentUserId = computed(() => profile.value?.id);
const hasAuthorized = computed(
  () => post.value.memberId === currentUserId.value
);
const isAdmin = computed(() => authStore.user.role === 'ADMIN');
const isLiked = ref(false);
const isLoggedIn = computed(() => authStore.isLoggedIn);

//좋아요 목록
const params = ref({
  page: 1,
  size: 16,
});

const props = defineProps({
  id: {
    type: String,
    required: true
  }
});

const members = ref(
  [] as Array<{
    nickname: string;
    likedAt: string;
  }>,
);

const isLikeMembersVisible = ref(false);
const toggleLikeMembers = async () => {
  isLikeMembersVisible.value = !isLikeMembersVisible.value;
  if (isLikeMembersVisible.value) {
    await fetchLikeMembers();
  }
};
const toggleComments = () => {
  isLikeMembersVisible.value = false;
};

const totalCount = ref(0);
const pageCount = computed(() =>
  Math.ceil(totalCount.value / params.value.size),
);

const fetchLikeMembers = async () => {
  try {
    const { data } = await axiosInstance.get(`/api/posts/${props.id}/likes/members`, {
      params: params.value
    });
    members.value = data.items;
    totalCount.value = data.totalCount;
  } catch (error) {
    console.error('Failed to fetch like members:', error);
  }
};

watch(
  [() => params.value.page, () => isLiked.value, () => post.value.likes],
  () => {
    fetchLikeMembers();
  },
);

//게시글 조회
interface Post {
  title: string;
  content: string;
  createdAt: string;
  memberId: string;
  nickname: string;
  views: number;
  commentCount: number;
  likes: number;
}

const post = ref<Post>({
  content: '',
  createdAt: '',
  memberId: '',
  title: '',
  nickname: '',
  views: 0,
  commentCount: 0,
  likes: 0
});

const fetchPost = async () => {
  try {
    const { data } = await axiosInstance.get(`/api/posts/${props.id}`);
    setPost(data);
    isLikeMembersVisible.value = false;
    await fetchLikes();
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
                   commentCount,
                 }: Post) => {
  post.value.title = title;
  post.value.content = content;
  post.value.createdAt = createdAt;
  post.value.memberId = memberId;
  post.value.nickname = nickname;
  post.value.views = views;
  post.value.commentCount = commentCount;
};

const fetchLikes = async () => {
  try {
    const { data } = await axios.get(`/api/posts/${props.id}/likes`);
    post.value.likes = data.likes;
    if (isLoggedIn.value) {
      const { data } = await axiosInstance.get(`/api/posts/${props.id}/likes/status`);
      isLiked.value = data.likedStatus;
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response?.status === 409) {
      } else {
        console.log(response);
      }
    }
  }
};

watch([() => props.id], fetchPost);
// watch([() => post.value.likes, () => isLiked.value], fetchLikes);

const toggleLike = async () => {
  try {
    if (!isLiked.value) {
      await axiosInstance.post(`/api/posts/${props.id}/likes`);
    } else {
      await axiosInstance.delete(`/api/posts/${props.id}/likes`);
    }
    await fetchLikes();
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response?.status === 401) {
        const result = confirm('이 기능은 로그인이 필요합니다. 로그인하시겠습니까?');
        if (result) {
          await router.push({ name: 'LoginView' });
        }
      }
    }
  }
}

//Button
const remove = async () => {
  try {
    if (!confirm('게시글을 삭제하시겠습니까?')) {
      return;
    }
    await axios.delete(`/api/posts/${props.id}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('access')}`
      }
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

const removeByAdmin = async () => {
  try {
    if (!confirm('게시글을 삭제하시겠습니까?')) {
      return;
    }
    await axios.delete(`/api/admin/posts/${props.id}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('access')}`
      }
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
    await router.replace({ name: 'PostDetail', params: { id: data.id } });
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
    name: 'PostList'
  });

const moveToEdit = () =>
  router.push({
    name: 'PostEdit',
    params: { id: props.id }
  });

onMounted(fetchPost);
</script>

<style scoped></style>
