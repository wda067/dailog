<template>
  <ul style="padding-left: 0">
    <li v-for="item in items" :key="item.id" class="card" style="border: none">
      <!--수정 중인 댓글-->
      <div v-if="isEditingComment(item.id)" class="mt-3">
        <!--익명 댓글은 누구나 수정 가능-->
        <div v-if="item.anonymousName != null">
          <CommentForm
            v-model:content="item.content"
            v-model:name="item.anonymousName"
            :isLoggedIn="false"
            :password="editPassword"
            :readonly="true"
            @update:password="setPassword"
            @submit.prevent="startEdit(item)"
          >
            <template #actions>
              <div class="row g-2 mb-3">
                <div class="col-auto">
                  <button
                    class="btn btn-outline-dark"
                    @click.prevent="cancelEdit"
                  >
                    취소
                  </button>
                </div>
                <div class="col-auto">
                  <button class="btn btn-primary">수정</button>
                </div>
              </div>
            </template>
          </CommentForm>
        </div>

        <!--본인 댓글이거나 관리자-->
        <div
          v-else-if="(item.memberId == memberId && memberId != '') || isAdmin"
        >
          <CommentForm
            v-model:content="item.content"
            :isLoggedIn="true"
            :name="getCommenterName(item)"
            :readonly="true"
            @update:password="setPassword"
            @submit.prevent="startEdit(item)"
          >
            <template #actions>
              <div class="row g-2 mb-3">
                <div class="col-auto">
                  <button class="btn btn-outline-dark" @click="cancelEdit">
                    취소
                  </button>
                </div>
                <div class="col-auto">
                  <button class="btn btn-primary">수정</button>
                </div>
              </div>
            </template>
          </CommentForm>
        </div>

        <div v-else>
          {{ alertNotAuthorized() }}
        </div>
      </div>

      <!--수정 중이 아닌 댓글-->
      <div v-else class="d-flex justify-content-between">
        <div class="card-body">
          <h6 class="card-title">
            <strong>
              {{ getCommenterName(item) }}
            </strong>
          </h6>
          <p class="card-text mb-1">{{ item.content }}</p>
          <p class="card-text">
            <small class="text-muted">{{ item.createdAt }}</small>
          </p>
        </div>

        <div class="dropdown mt-1">
          <button
            aria-expanded="false"
            class="btn dropdown-toggle"
            data-bs-toggle="dropdown"
            type="button"
          />
          <ul class="dropdown-menu">
            <li>
              <a
                class="dropdown-item"
                href="#"
                @click.prevent="editComment(item)"
                >수정</a
              >
            </li>
            <li>
              <div
                v-if="
                  (item.memberId == memberId && memberId != '') ||
                  isAdmin ||
                  item.anonymousName != null
                "
              >
                <a
                  class="dropdown-item"
                  data-bs-target="#deleteModal"
                  data-bs-toggle="modal"
                  href="#"
                  @click.prevent="setSelectedComment(item)"
                  >삭제</a
                >
              </div>
              <div v-else>
                <a
                  class="dropdown-item"
                  href="#"
                  @click.prevent="alertNotAuthorized"
                  >삭제</a
                >
              </div>
            </li>
          </ul>
        </div>
      </div>
      <div class="card-divider"></div>
    </li>
  </ul>

  <!-- Delete Modal -->
  <div
    id="deleteModal"
    aria-hidden="true"
    aria-labelledby="deleteModalLabel"
    class="modal fade"
    tabindex="-1"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h1 id="deleteModalLabel" class="modal-title fs-5">댓글 삭제</h1>
          <button
            aria-label="Close"
            class="btn-close"
            data-bs-dismiss="modal"
            type="button"
          ></button>
        </div>

        <div v-if="isAnonymousComment && !isAdmin" class="modal-body">
          비밀번호를 입력해 주세요.
          <input
            v-model="deletePassword"
            class="form-control mt-1"
            type="password"
          />
        </div>
        <div class="modal-footer">
          <button
            class="btn btn-secondary"
            data-bs-dismiss="modal"
            type="button"
          >
            취소
          </button>
          <button
            class="btn btn-primary"
            data-bs-dismiss="modal"
            type="button"
            @click.prevent="confirmDeleteComment"
          >
            확인
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import CommentForm from '@/components/comments/CommentForm.vue';
import { useAlert } from '@/composables/useAlert';
import { useAuthStore } from '@/stores/auth';

const { vAlert } = useAlert();
const authStore = useAuthStore();
const isAdmin = computed(() => authStore.user?.role === 'ADMIN');

interface Comment {
  id: string;
  memberId: string;
  nickname: string;
  anonymousName: string;
  password: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

defineProps<{
  items: Comment[];
  memberId: string;
}>();

const temp = (a: string, b: string) => {
  console.log(a);
  console.log(b);
  console.log(a === b);
  console.log(b != '');
};

const emits = defineEmits(['delete:comment', 'edit:comment']);
const selectedCommentId = ref('');
const deletePassword = ref('');
const editingCommentId = ref('');
const editContent = ref('');
const editPassword = ref('');
const isAnonymousComment = ref(false);

const getCommenterName = (item: Comment) => {
  return item.memberId ? item.nickname : item.anonymousName;
};

const setIsAnonymous = (comment: Comment) => {
  isAnonymousComment.value = comment.anonymousName != null;
};

const setSelectedComment = (comment: Comment) => {
  selectedCommentId.value = comment.id;
  //setIsAnonymous(comment);
};

const editComment = (comment: Comment) => {
  editingCommentId.value = comment.id;
  editPassword.value = '';
  setIsAnonymous(comment);
};

const isEditingComment = (commentId: string) => {
  return editingCommentId.value === commentId;
};

const startEdit = (comment: Comment) => {
  editContent.value = comment.content;
  saveEdit(comment.id);
};

const setPassword = (password: string) => {
  editPassword.value = password;
};

const cancelEdit = () => {
  editingCommentId.value = '';
};

const saveEdit = (commentId: string) => {
  emits('edit:comment', {
    id: commentId,
    password: editPassword.value,
    content: editContent.value,
    isAnonymousComment: isAnonymousComment.value,
  });
  cancelEdit();
};

const confirmDeleteComment = () => {
  emits(
    'delete:comment',
    selectedCommentId.value,
    deletePassword.value,
    isAnonymousComment.value,
  );
  deletePassword.value = '';
};

const alertNotAuthorized = () => {
  vAlert('수정/삭제 권한이 없습니다.');
  cancelEdit();
};
</script>

<style scoped>
.card-divider {
  border-bottom: 1px solid #dee2e6;
  margin-top: 0;
  margin-bottom: 0;
}

.card-body {
  padding-left: 0;
}

.dropdown-menu {
  padding: 0;
  min-width: auto;
}

.dropdown-item {
  display: inline-block;
  width: auto;
  padding: 0.25rem 0.5rem;
}
</style>
