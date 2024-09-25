<template>
  <div class="col-sm-6 mx-auto">
    <h2 class="my-4">회원 정보</h2>

    <hr class="thin-hr" />
    <div class="flex-row">
      <label class="form-label fixed-width">이메일</label>
      <label class="form-label fixed-width w-auto">{{ profile.email }}</label>
    </div>
    <hr class="thin-hr" />

    <div class="flex-row">
      <label class="form-label fixed-width">이름</label>
      <label class="form-label fixed-width">{{ profile.name }}</label>
    </div>
    <hr class="thin-hr" />

    <Transition name="slide">
      <div v-if="true" class="flex-row">
        <label class="form-label fixed-width">별명</label>
        <label class="form-label fixed-width">{{ profile.nickname }}</label>
        <a
          class="btn btn-outline-dark ms-auto"
          @click.prevent="openModal('nickname')"
          >별명 변경</a
        >
      </div>
    </Transition>
    <hr class="thin-hr" />

    <div class="flex-row">
      <label class="form-label fixed-width">가입 날짜</label>
      <label class="form-label fixed-width">{{ profile.createdAt }}</label>
    </div>
    <hr class="thin-hr" />

    <div v-if="!profile.oAuth2Login">
      <Transition name="slide">
        <div class="flex-row">
          <label class="form-label fixed-width">비밀번호</label>
          <a class="btn btn-outline-dark" @click.prevent="openModal('password')"
            >비밀번호 변경</a
          >
        </div>
      </Transition>
      <hr class="thin-hr" />
    </div>

    <div class="flex-row">
      <label class="form-label fixed-width">등급</label>
      <label class="form-label fixed-width">{{ profile.role }}</label>
    </div>
    <hr class="thin-hr" />

    <div class="d-flex justify-content-end">
      <button
        class="btn btn-outline-danger"
        @click.prevent="confirmDeleteAccount"
      >
        회원 탈퇴
      </button>
    </div>
  </div>

  <!--Modal-->
  <Transition name="slide">
    <div v-if="showModal" class="modal-mask" @click.self="closeModal">
      <div class="modal-wrapper">
        <div class="modal-container">
          <div class="modal-header">
            <h4 class="modal-title">{{ modalTitle }}</h4>
            <button
              class="btn-close"
              type="button"
              @click="closeModal"
            ></button>
          </div>
          <hr class="mt-2" />
          <div v-if="isChangePassword || isDeleteAccount" class="modal-body">
            <label class="form-label" for="currentPassword"
              >현재 비밀번호</label
            >
            <div class="position-relative mb-1">
              <input
                id="currentPassword"
                v-model="currentPassword"
                :type="showPassword ? 'text' : 'password'"
                class="form-control"
              />
              <div class="icon-container" @click="togglePassword">
                <i :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
              </div>
            </div>
            <div v-if="!isChangePassword">
              <small style="color: GrayText">
                * 비밀번호는 8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해
                주세요.
              </small>
            </div>
            <div v-if="currentError" class="text-danger mb-1">
              {{ currentError }}
            </div>

            <div v-if="isChangePassword">
              <label class="form-label" for="newPassword"
                >새로운 비밀번호</label
              >
              <div class="position-relative mb-1">
                <input
                  id="newPassword"
                  v-model="newPassword"
                  :type="showPassword ? 'text' : 'password'"
                  class="form-control"
                />
                <div class="icon-container" @click="togglePassword">
                  <i
                    :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"
                  ></i>
                </div>
              </div>
              <div class="mb-1">
                <small style="color: GrayText">
                  * 비밀번호는 8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해
                  주세요.
                </small>
                <div v-if="newError" class="text-danger">{{ newError }}</div>
              </div>

              <label class="form-label" for="confirmNewPassword"
                >새로운 비밀번호 확인</label
              >
              <div class="position-relative mb-1">
                <input
                  id="confirmNewPassword"
                  v-model="confirmPassword"
                  :type="showPassword ? 'text' : 'password'"
                  class="form-control"
                />
                <div class="icon-container" @click="togglePassword">
                  <i
                    :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"
                  ></i>
                </div>
              </div>
              <div v-if="confirmError" class="text-danger">
                {{ confirmError }}
              </div>
            </div>
            <div v-if="errorMessage" class="text-danger">
              {{ errorMessage }}
            </div>
          </div>

          <div v-else-if="isChangeNickname" class="modal-body">
            <label class="form-label" for="newNickname">새로운 별명</label>
            <div class="position-relative mb-1">
              <input
                id="newNickname"
                v-model="newNickname"
                class="form-control"
                type="text"
              />
            </div>
            <div v-if="nicknameError" class="text-danger mb-1">
              {{ nicknameError }}
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" type="button" @click="closeModal">
              취소
            </button>
            <button
              class="btn btn-primary"
              type="button"
              @click.prevent="
                isChangePassword
                  ? changePassword()
                  : isChangeNickname
                    ? changeNickname()
                    : deleteAccount()
              "
            >
              확인
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';
import { useAlert } from '@/composables/useAlert';
import { onBeforeRouteUpdate, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import axiosInstance from '@/composables/useApi';

const { vAlert, vSuccess } = useAlert();
const router = useRouter();
const authStore = useAuthStore();

const showPassword = ref(false);

const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

const showModal = ref(false);
const modalTitle = ref('비밀번호 변경');
const isChangePassword = ref(true);
const isChangeNickname = ref(false);
const isDeleteAccount = ref(false);

const openModal = (type: 'password' | 'delete' | 'nickname') => {
  if (type === 'password') {
    modalTitle.value = '비밀번호 변경';
    isChangePassword.value = true;
    isChangeNickname.value = false;
  } else if (type === 'nickname') {
    modalTitle.value = '별명 변경';
    isChangePassword.value = false;
    isChangeNickname.value = true;
  } else {
    modalTitle.value = '회원 탈퇴';
    isChangePassword.value = false;
    isChangeNickname.value = false;
  }
  showModal.value = true;
};

const closeModal = () => {
  resetForm();
  resetError();
  showModal.value = false;
};

const props = defineProps<{ id: string | string[] }>();

interface Member {
  name: string;
  email: string;
  nickname: string;
  createdAt: string;
  role: string;
  oAuth2Login: boolean;
}

const profile = ref<Member>({
  name: '',
  email: '',
  nickname: '',
  createdAt: '',
  role: '',
  oAuth2Login: false,
});

const currentPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const currentError = ref('');
const newError = ref('');
const confirmError = ref('');
const errorMessage = ref('');
const newNickname = ref('');
const nicknameError = ref('');

const fetchProfile = async (id: string | string[]) => {
  const memberId = Array.isArray(id) ? id[0] : id;
  try {
    const { data } = await axiosInstance.get(`/api/members/${memberId}`);
    profile.value.email = data.email;
    profile.value.name = data.name;
    profile.value.nickname = data.nickname;
    profile.value.createdAt = data.createdAt;
    profile.value.role = data.role;
    profile.value.oAuth2Login = data.oauth2Login;
  } catch (error) {
    console.log('error: ', error);
  }
};

const resetError = () => {
  currentError.value = '';
  newError.value = '';
  confirmError.value = '';
  errorMessage.value = '';
  nicknameError.value = '';
};

const resetForm = () => {
  currentPassword.value = '';
  newPassword.value = '';
  confirmPassword.value = '';
  newNickname.value = '';
};

const changePassword = async () => {
  const memberId = Array.isArray(props.id) ? props.id[0] : props.id;
  try {
    await axiosInstance.patch(`/api/members/${memberId}/password`, {
      currentPassword: currentPassword.value,
      newPassword: newPassword.value,
      confirmPassword: confirmPassword.value,
    });
    await fetchProfile(memberId);
    vSuccess('비밀번호가 성공적으로 변경되었습니다.');
    closeModal();
  } catch (error) {
    resetError();
    if (axios.isAxiosError(error)) {
      if (Object.keys(error.response?.data.validation).length === 0) {
        errorMessage.value = error.response?.data.message;
      } else {
        if (error.response?.data.validation.currentPassword !== null) {
          currentError.value = error.response?.data.validation.currentPassword;
        }
        if (error.response?.data.validation.confirmPassword !== null) {
          newError.value = error.response?.data.validation.newPassword;
        }
        if (error.response?.data.validation.newPassword !== null) {
          confirmError.value = error.response?.data.validation.confirmPassword;
        }
      }
    }
  }
};

const changeNickname = async () => {
  const memberId = Array.isArray(props.id) ? props.id[0] : props.id;
  try {
    await axiosInstance.patch(`/api/members/${memberId}/nickname`, {
      newNickname: newNickname.value,
    });
    await fetchProfile(memberId);
    await authStore.getUserInfo();
    vSuccess('별명이 성공적으로 변경되었습니다.');
    closeModal();
  } catch (error) {
    resetError();
    if (axios.isAxiosError(error)) {
      const validation = error.response?.data.validation;
      if (
        validation &&
        Object.keys(validation).length > 0 &&
        validation.newNickname
      ) {
        nicknameError.value = validation.newNickname;
      } else {
        nicknameError.value = error.response?.data.message;
      }
    }
  }
};

const confirmDeleteAccount = () => {
  if (!confirm('계정을 삭제하시겠습니까?')) {
    return;
  }
  console.log(profile.value.oAuth2Login);
  if (profile.value.oAuth2Login) {
    deleteOAuth2Account();
    isDeleteAccount.value = true;
    return;
  }
  openModal('delete');
  isDeleteAccount.value = true;
};

const deleteOAuth2Account = async () => {
  try {
    const memberId = Array.isArray(props.id) ? props.id[0] : props.id;
    await axiosInstance.post(`/api/auth/${memberId}/leave-oauth2`);
    authStore.cleanToken();
    router
      .replace({ name: 'HomeView' })
      .then(() => vSuccess('계정이 삭제되었습니다.'));
  } catch (error) {
    resetError();
    if (axios.isAxiosError(error)) {
      console.log(error.message);
      vAlert('계정을 삭제하는데 실패했습니다.');
    }
  }
};

const deleteAccount = async () => {
  try {
    const memberId = Array.isArray(props.id) ? props.id[0] : props.id;
    await axiosInstance.post(`/api/auth/${memberId}/leave`, {
      password: currentPassword.value,
    });
    closeModal();
    authStore.cleanToken();
    router
      .replace({ name: 'HomeView' })
      .then(() => vSuccess('계정이 삭제되었습니다.'));
  } catch (error) {
    resetError();
    if (axios.isAxiosError(error)) {
      const data = error.response?.data;
      if (data) {
        if (data.validation && data.validation.password) {
          currentError.value = data.validation.password;
        } else {
          errorMessage.value = data.message;
        }
      } else {
        vAlert('계정을 삭제하는데 실패했습니다.');
      }
    }
  }
};

onMounted(() => fetchProfile(props.id as string | string[]));

onBeforeRouteUpdate((to, from, next) => {
  fetchProfile(to.params.id as string | string[]);
  next();
});
</script>

<style scoped>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-wrapper {
  width: 100%;
  max-width: 600px;
}

.modal-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  transition:
    transform 0.3s ease-out,
    opacity 0.3s ease-out;
}

.slide-enter-active,
.slide-leave-active {
  transition:
    transform 0.3s ease-out,
    opacity 0.3s ease-out;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  margin: 0;
}

.modal-body {
  margin-top: 10px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.modal-footer .btn + .btn {
  margin-left: 10px;
}

.position-relative {
  display: flex;
  align-items: center;
}

.icon-container {
  position: absolute;
  right: 15px;
  display: flex;
  align-items: center;
  height: 100%;
  cursor: pointer;
}

.flex-row {
  display: flex;
  align-items: center;
}

.fixed-width {
  margin: 0;
  height: 40px;
  width: 105px; /* 원하는 너비 설정 */
  display: flex;
  align-items: center;
  white-space: nowrap;
}

.thin-hr {
  border: 0;
  height: 1px; /* 선의 기본 높이 설정 */
  background-color: #000000; /* 선의 색상 설정 */
  transform: scaleY(0.5); /* 선의 두께를 0.5로 줄임 */
  margin-top: 8px; /* 상하 여백 설정 */
  margin-bottom: 8px;
}

.btn {
  white-space: nowrap;
}
</style>
