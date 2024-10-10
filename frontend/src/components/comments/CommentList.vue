<template>
  <ul style="padding-left: 0">
    <li v-for="item in items" :key="item.id" class="card" style="border: none">
      <!--부모 댓글-->
      <div v-if="item.isParent">
        <!--수정 중인 댓글-->
        <div v-if="isEditingComment(item.id)" class="mt-2">
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
                <div class="row g-2 mb-2">
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
            v-else-if="
              (item.memberId == props.memberId && props.memberId != '') ||
              isAdmin
            "
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
                <div class="row g-2 mb-2">
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
          <div class="card-body py-1">
            <h6 class="card-title">
              <strong v-if="item.anonymousName == null">
                {{ getCommenterName(item) }}
              </strong>
              <strong v-else>
                {{ getCommenterName(item) }} ({{ item.ipAddress }})
              </strong>
            </h6>
            <p class="card-text mb-1">{{ item.content }}</p>
            <p class="card-text">
              <small class="text-muted"
                >{{ item.createdAt }}
                <a
                  class="text-muted"
                  @click.prevent="toggleReply(item.id)"
                  style="text-decoration: none"
                >
                  답글쓰기</a
                >
              </small>
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
                    (item.memberId == props.memberId && props.memberId != '') ||
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

        <!--대댓글 작성-->
        <div
          v-if="replyStatus[item.id]"
          class="mt-2"
          style="padding-left: 20px"
        >
          <CommentForm
            v-model:content="newComment.content"
            v-model:name="newComment.anonymousName"
            v-model:password="newComment.password"
            :isLoggedIn="isLoggedIn"
            @submit.prevent="replyComment(item.id)"
          >
            <template #actions>
              <div class="row g-2 mb-2">
                <div class="col-auto">
                  <button
                    class="btn btn-outline-dark"
                    @click.prevent="toggleReply(item.id)"
                  >
                    취소
                  </button>
                </div>
                <div class="col-auto">
                  <button class="btn btn-primary">등록</button>
                </div>
              </div>
            </template>
          </CommentForm>
          <div class="card-divider"></div>
        </div>
      </div>

      <!--대댓글-->
      <div v-else style="padding-left: 20px">
        <!--수정 중인 댓글-->
        <div v-if="isEditingComment(item.id)" class="mt-2">
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
                <div class="row g-2 mb-2">
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
            v-else-if="
              (item.memberId == props.memberId && props.memberId != '') ||
              isAdmin
            "
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
                <div class="row g-2 mb-2">
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
          <div class="card-body py-1">
            <h6 class="card-title">
              <strong v-if="item.anonymousName == null">
                {{ getCommenterName(item) }}
              </strong>
              <strong v-else>
                {{ getCommenterName(item) }} ({{ item.ipAddress }})
              </strong>
            </h6>
            <p class="card-text mb-1">{{ item.content }}</p>
            <p class="card-text">
              <small class="text-muted">{{ item.createdAt }} </small>
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
                    (item.memberId == props.memberId && props.memberId != '') ||
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
      </div>

      <!--대댓글-->
      <!--      <div v-if="replyStatus[item.id]">-->
      <!--        <CommentForm-->
      <!--          v-model:content="newComment.content"-->
      <!--          v-model:name="newComment.anonymousName"-->
      <!--          v-model:password="newComment.password"-->
      <!--          :isLoggedIn="isLoggedIn"-->
      <!--          @submit.prevent="replyComment(item.id)"-->
      <!--        >-->
      <!--          <template #actions>-->
      <!--            <div class="form-row justify-content-end">-->
      <!--              <button class="btn btn-primary">등록</button>-->
      <!--            </div>-->
      <!--          </template>-->
      <!--        </CommentForm>-->
      <!--        <div class="card-divider"></div>-->
      <!--      </div>-->

      <!--      &lt;!&ndash; 자식 댓글 리스트 &ndash;&gt;-->
      <!--      <ul-->
      <!--        v-if="item.childComments && item.childComments.length > 0"-->
      <!--        style="padding-left: 20px"-->
      <!--      >-->
      <!--        <ChildCommentList-->
      <!--          :items="item.childComments"-->
      <!--          :memberId="memberId"-->
      <!--          @edit:comment="saveEditWithParams"-->
      <!--          @delete:comment="confirmDeleteWithParams"-->
      <!--        />-->
      <!--      </ul>-->
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
import { computed, reactive, ref } from 'vue';
import CommentForm from '@/components/comments/CommentForm.vue';
import { useAlert } from '@/composables/useAlert';
import { useAuthStore } from '@/stores/auth';

const { vAlert } = useAlert();
const authStore = useAuthStore();
const isAdmin = computed(() => authStore.user?.role === 'ADMIN');
const isLoggedIn = computed(() => authStore.isLoggedIn);

interface NewComment {
  anonymousName: string;
  password: string;
  content: string;
  parentId: string;
}

const newComment = ref<NewComment>({
  anonymousName: '',
  password: '',
  content: '',
  parentId: '',
});

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
  isParent: boolean;
}

const props = defineProps<{
  items: Comment[];
  memberId: string;
}>();

const emits = defineEmits(['delete:comment', 'edit:comment', 'reply:comment']);

const replyStatus = reactive<{ [key: string]: boolean }>({}); //각 댓글별 상태 관리

// 답글 작성 폼을 토글하는 함수
const toggleReply = (id: string) => {
  if (replyStatus[id]) {
    replyStatus[id] = false;
  } else {
    Object.keys(replyStatus).forEach(key => {
      replyStatus[key] = false; // 다른 폼 닫기
    });
    replyStatus[id] = true; // 해당 폼 열기
    newComment.value = {
      anonymousName: '',
      password: '',
      content: '',
      parentId: id,
    };
  }
};

const selectedCommentId = ref('');
const deletePassword = ref('');
const editingCommentId = ref('');
const editContent = ref('');
const editPassword = ref('');
const isAnonymousComment = ref(false);
const hasReply = ref(false);
const parentId = ref('');

const openReply = (id: string) => {
  hasReply.value = !hasReply.value;
  parentId.value = id;
};

const isAuthor = (item: Comment) => {
  return item.memberId == props.memberId && props.memberId != '';
};

const getCommenterName = (item: Comment) => {
  return item.memberId ? item.nickname : item.anonymousName;
};

const setIsAnonymous = (comment: Comment) => {
  isAnonymousComment.value = comment.anonymousName != null;
};

const setSelectedComment = (comment: Comment) => {
  selectedCommentId.value = comment.id;
  setIsAnonymous(comment);
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

interface CommentParams {
  id: string;
  password: string;
  content: string;
  isAnonymousComment: boolean;
}

const saveEditWithParams = (params: CommentParams) => {
  emits('edit:comment', {
    id: params.id,
    password: params.password,
    content: params.content,
    isAnonymousComment: params.isAnonymousComment,
  });
  cancelEdit();
};

const confirmDeleteComment = () => {
  emits('delete:comment', {
    id: selectedCommentId.value,
    password: deletePassword.value,
    isAnonymousComment: isAnonymousComment.value,
  });
  deletePassword.value = '';
  selectedCommentId.value = '';
};

// interface DeleteCommentParams {
//   id: string;
//   password: string;
//   isAnonymousComment: boolean;
// }

// const confirmDeleteWithParams = (params: DeleteCommentParams) => {
//   console.log('confirmDeleteWithParams');
//   console.log(selectedCommentId.value);
//   console.log(params.id);
//   if (params.id == '') {
//     emits('delete:comment', {
//       id: selectedCommentId.value,
//       password: deletePassword.value,
//       isAnonymousComment: isAnonymousComment.value,
//     });
//   } else {
//     emits('delete:comment', {
//       id: params.id,
//       password: params.password,
//       isAnonymousComment: params.isAnonymousComment,
//     });
//   }
//   deletePassword.value = '';
//   selectedCommentId.value = '';
// };

const replyComment = (parentId: string) => {
  emits('reply:comment', {
    password: newComment.value.password,
    content: newComment.value.content,
    anonymousName: newComment.value.anonymousName,
    parentId: parentId,
  });
  replyStatus[parentId] = false; //답글 작성 후 폼 닫기
};

const alertNotAuthorized = () => {
  vAlert('수정/삭제 권한이 없습니다.');
  cancelEdit();
};
</script>

<style scoped>
.card-divider {
  border-bottom: 1px solid #dee2e6;
  margin-top: 5px;
  margin-bottom: 5px;
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
