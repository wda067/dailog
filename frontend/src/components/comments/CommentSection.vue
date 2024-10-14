<template>
  <div>
    <CommentList
      :items="comments"
      :memberId="memberId"
      :addedCommentId="addedCommentId"
      @edit:comment="editComment"
      @delete:comment="deleteComment"
      @reply:comment="replyComment"
    />

    <AppPagination
      v-if="props.commentCount > params.size"
      :current-page="params.page"
      :page-count="pageCount"
      @page="page => (params.page = page)"
    />

    <CommentForm
      v-model:content="newComment.content"
      v-model:name="newComment.anonymousName"
      v-model:password="newComment.password"
      :isLoggedIn="isLoggedIn"
      @submit.prevent="addComment"
    >
      <template #actions>
        <div class="form-row justify-content-end">
          <button class="btn btn-primary">등록</button>
        </div>
      </template>
    </CommentForm>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import axios from 'axios';
import CommentList from '@/components/comments/CommentList.vue';
import CommentForm from '@/components/comments/CommentForm.vue';
import { useAlert } from '@/composables/useAlert';
import { useAuthStore } from '@/stores/auth';
import axiosInstance from '@/composables/useApi';
import AppPagination from '@/components/app/AppPagination.vue';

const { vAlert, vSuccess } = useAlert();
const emits = defineEmits(['comment:changed']);
const totalCount = ref(0);
const params = ref({
  page: 1,
  size: 15,
});
const pageCount = computed(() =>
  Math.ceil(totalCount.value / params.value.size),
);

interface Comment {
  id: string;
  memberId: string;
  nickname: string;
  anonymousName: string;
  password: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  ipAddress: string;
  childComments: Comment[];
  parentStatus: boolean;
}

interface NewComment {
  anonymousName: string;
  password: string;
  content: string;
}

const props = defineProps({
  postId: {
    type: String,
    required: true,
  },
  commentCount: {
    type: Number,
    required: true,
  },
});

const comments = ref<Comment[]>([]);

const newComment = ref<NewComment>({
  anonymousName: '',
  password: '',
  content: '',
});

const authStore = useAuthStore();
const isLoggedIn = computed(() => authStore.isLoggedIn);
const isAdmin = computed(() => authStore.user?.role === 'ADMIN');
const memberId = computed(() =>
  authStore.user?.id ? String(authStore.user?.id) : '',
);
const addedCommentId = ref('');

const fetchComment = async () => {
  try {
    const { data } = await axios.get(`/api/posts/${props.postId}/comments`, {
      params: params.value,
    });
    comments.value = data.items;
    totalCount.value = data.totalCount;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response) {
        vAlert(response.data.message);
      } else {
        vAlert(error.message);
      }
    }
  }
};

watch(
  () => params.value.page,
  () => {
    fetchComment();
  },
);

watch(() => props.postId, fetchComment);
onMounted(() => {
  fetchComment();
});

const addComment = async () => {
  if (isLoggedIn.value) {
    await addCommentByMember();
  } else {
    await addCommentByAnonymous();
  }

  const totalPages = Math.ceil(totalCount.value / params.value.size);
  if (params.value.page < totalPages) {
    params.value.page = totalPages;
  }
  emits('comment:changed');
};

const addCommentByMember = async () => {
  try {
    const { data } = await axiosInstance.post(
      `/api/posts/${props.postId}/comments/member`,
      {
        content: newComment.value.content,
      },
    );
    addedCommentId.value = data.id;
    await fetchComment();
    initForm();
    vSuccess('댓글이 등록되었습니다.');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      const { validation } = response?.data || {};

      if (!response) {
        vAlert('응답을 받을 수 없습니다.');
      }
      if (validation.content) {
        vAlert(validation?.content);
      } else {
        vAlert(response?.data.message);
      }
    } else {
      vAlert('댓글 작성 중 오류가 발생했습니다.');
    }
  }
};

const addCommentByAnonymous = async () => {
  try {
    const { data } = await axios.post(
      `/api/posts/${props.postId}/comments/anonymous`,
      {
        anonymousName: newComment.value.anonymousName,
        password: newComment.value.password,
        content: newComment.value.content,
      },
    );
    addedCommentId.value = data.id;
    await fetchComment();
    initForm();
    vSuccess('댓글이 등록되었습니다.');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      const { validation } = response?.data || {};

      if (!response) {
        vAlert('응답을 받을 수 없습니다.');
      }
      if (validation.anonymousName) {
        vAlert(validation?.anonymousName);
      }
      if (validation.password) {
        vAlert(validation?.password);
      }
      if (validation.content) {
        vAlert(validation?.content);
      }
      if (!(!(validation.title || validation.content) || validation.content)) {
        vAlert(response?.data.message);
      }
    } else {
      vAlert('댓글 작성 중 오류가 발생했습니다.');
    }
  }
};

const replyComment = async ({
  parentId,
  content,
  password,
  anonymousName,
}: {
  parentId: string;
  content: string;
  anonymousName: string;
  password: string;
}) => {
  const parentCommentIndex = comments.value.findIndex(
    comment => comment.id === parentId,
  );

  let prevChildCommentCount = 0;
  for (let i = parentCommentIndex + 1; i < comments.value.length; i++) {
    if (comments.value[i].parentStatus) break;
    prevChildCommentCount++;
  }

  if (isLoggedIn.value) {
    await replyCommentByMember(parentId, content);
  } else {
    await replyCommentByAnonymous(parentId, content, anonymousName, password);
  }

  let nextChildCommentCount = 0;
  for (let i = parentCommentIndex + 1; i < comments.value.length; i++) {
    if (comments.value[i].parentStatus) break;
    nextChildCommentCount++;
  }

  const prevTotalCount = parentCommentIndex + prevChildCommentCount;
  const nextTotalCount = parentCommentIndex + nextChildCommentCount;
  if (
    prevTotalCount == nextTotalCount &&
    nextTotalCount + 1 == params.value.size
  ) {
    params.value.page++;
  }
  emits('comment:changed');
};

const replyCommentByMember = async (parentId: string, content: string) => {
  try {
    const { data } = await axiosInstance.post(
      `/api/posts/${props.postId}/comments/member`,
      {
        parentId: parentId,
        content: content,
      },
    );
    addedCommentId.value = data.id;
    await fetchComment();
    vSuccess('댓글이 등록되었습니다.');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      const { validation } = response?.data || {};

      if (!response) {
        vAlert('응답을 받을 수 없습니다.');
      }
      if (validation.content) {
        vAlert(validation?.content);
      } else {
        vAlert(response?.data.message);
      }
    } else {
      vAlert('댓글 작성 중 오류가 발생했습니다.');
    }
  }
};

const replyCommentByAnonymous = async (
  parentId: string,
  content: string,
  anonymousName: string,
  password: string,
) => {
  try {
    const { data } = await axios.post(
      `/api/posts/${props.postId}/comments/anonymous`,
      {
        parentId: parentId,
        content: content,
        anonymousName: anonymousName,
        password: password,
      },
    );
    addedCommentId.value = data.id;
    await fetchComment();
    vSuccess('댓글이 등록되었습니다.');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      const { validation } = response?.data || {};

      if (!response) {
        vAlert('응답을 받을 수 없습니다.');
      }
      if (validation.anonymousName) {
        vAlert(validation?.anonymousName);
      }
      if (validation.password) {
        vAlert(validation?.password);
      }
      if (validation.content) {
        vAlert(validation?.content);
      }
      if (!(!(validation.title || validation.content) || validation.content)) {
        vAlert(response?.data.message);
      }
    } else {
      vAlert('댓글 작성 중 오류가 발생했습니다.');
    }
  }
};

const editComment = async ({
  id,
  password,
  content,
  isAnonymousComment,
}: {
  id: string;
  password: string;
  content: string;
  isAnonymousComment: boolean;
}) => {
  if (isLoggedIn.value && !isAnonymousComment) {
    await editCommentByMember(id, content);
  } else {
    await editCommentByAnonymous(id, password, content);
  }
  await fetchComment();
};

const editCommentByMember = async (id: string, content: string) => {
  try {
    const { data } = await axiosInstance.patch(`/api/comments/${id}/member`, {
      content: content,
    });
    addedCommentId.value = data.id;
    await fetchComment();
    vSuccess('댓글이 수정되었습니다.');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response) {
        vAlert(response.data.message);
      }
    }
  }
};

const editCommentByAnonymous = async (
  id: string,
  password: string,
  content: string,
) => {
  try {
    const { data } = await axios.patch(`/api/comments/${id}/anonymous`, {
      password: password,
      content: content,
    });
    addedCommentId.value = data.id;
    await fetchComment();
    vSuccess('댓글이 수정되었습니다.');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response) {
        vAlert(response.data.message);
      }
    }
  }
};

const deleteComment = async ({
  id,
  password,
  isAnonymousComment,
}: {
  id: string;
  password: string;
  isAnonymousComment: boolean;
}) => {
  if (isAdmin.value) {
    await deleteCommentByAdmin(id);
  } else if (isLoggedIn.value && !isAnonymousComment) {
    await deleteMemberComment(id);
  } else if (isAnonymousComment) {
    await deleteAnonymousComment(id, password);
  }
  emits('comment:changed');
};

const deleteCommentByAdmin = async (commentId: string) => {
  try {
    await axiosInstance
      .post(`/api/admin/comments/${commentId}/delete`)
      .then(fetchComment);
    vSuccess('댓글이 삭제되었습니다.');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response) {
        vAlert(response.data.message);
      } else {
        vAlert(error.message);
      }
    }
  }
};
const deleteMemberComment = async (commentId: string) => {
  try {
    await axiosInstance
      .post(`/api/comments/${commentId}/delete/member`)
      .then(fetchComment);
    vSuccess('댓글이 삭제되었습니다.');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response) {
        vAlert(response.data.message);
      } else {
        vAlert(error.message);
      }
    }
  }
};

const deleteAnonymousComment = async (commentId: string, password: string) => {
  try {
    await axios
      .post(`/api/comments/${commentId}/delete/anonymous`, {
        password: password,
      })
      .then(fetchComment);
    vSuccess('댓글이 삭제되었습니다.');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response) {
        vAlert(response.data.message);
      } else {
        vAlert(error.message);
      }
    }
  }
};

const initForm = () => {
  newComment.value.anonymousName = '';
  newComment.value.password = '';
  newComment.value.content = '';
};
</script>

<style scoped></style>
