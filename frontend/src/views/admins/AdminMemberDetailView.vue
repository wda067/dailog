<template>
  <div class="col-sm-6 mx-auto">
    <h2 class="my-4">회원 정보 (관리자용)</h2>

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

    <div v-if="true" class="flex-row">
      <label class="form-label fixed-width">별명</label>
      <label class="form-label fixed-width">{{ profile.nickname }}</label>
    </div>
    <hr class="thin-hr" />

    <div class="flex-row">
      <label class="form-label fixed-width">가입 날짜</label>
      <label class="form-label fixed-width">{{ profile.createdAt }}</label>
    </div>
    <hr class="thin-hr" />

    <div class="flex-row">
      <label class="form-label fixed-width">비밀번호</label>
      <div class="flex-grow-1 d-flex align-content-center">
        <div class="position-relative flex-grow-1 me-2">
          <input
            id="newPassword"
            v-model="initPassword"
            :type="showPassword ? 'text' : 'password'"
            class="form-control"
          />
          <div class="icon-container" @click="togglePassword">
            <i :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
          </div>
        </div>
        <a class="btn btn-outline-dark" @click.prevent="resetPassword"
          >비밀번호 초기화</a
        >
      </div>
    </div>
    <hr class="thin-hr" />

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
        회원 삭제
      </button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useAlert } from '@/composables/useAlert';
import axiosInstance from '@/composables/useApi';

const { vAlert, vSuccess } = useAlert();
const router = useRouter();

const showPassword = ref(false);

const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

const initPassword = ref('');

const props = defineProps<{ id: string | string[] }>();

interface Member {
  name: string;
  email: string;
  nickname: string;
  createdAt: string;
  role: string;
}

const profile = ref<Member>({
  name: '',
  email: '',
  nickname: '',
  createdAt: '',
  role: '',
});

const fetchProfile = async (id: string | string[]) => {
  const memberId = Array.isArray(id) ? id[0] : id;
  try {
    const { data } = await axiosInstance.get(`/api/admin/members/${memberId}`);
    profile.value.email = data.email;
    profile.value.name = data.name;
    profile.value.nickname = data.nickname;
    profile.value.createdAt = data.createdAt;
    profile.value.role = data.role;
  } catch (error) {
    console.log('error: ', error);
  }
};

const resetPassword = async () => {
  const memberId = Array.isArray(props.id) ? props.id[0] : props.id;
  try {
    await axios.patch(
      `/api/admin/members/${memberId}`,
      {
        password: initPassword.value,
      },
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('access')}`,
        },
      },
    );
    vSuccess(`비밀번호가 ${initPassword.value}로 초기화되었습니다.`);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const errorMessage = error.response?.data.validation.password;
      if (errorMessage) {
        vAlert(errorMessage);
      } else {
        vAlert(error.response?.data.message);
      }
    }
  }
};

const confirmDeleteAccount = () => {
  if (!confirm('계정을 삭제하시겠습니까?')) {
    return;
  }
  deleteAccount();
};

const deleteAccount = async () => {
  const memberId = Array.isArray(props.id) ? props.id[0] : props.id;
  try {
    await axios.post(
      `/api/admin/${memberId}/delete`,
      {},
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('access')}`,
        },
      },
    );
    vSuccess('계정이 삭제되었습니다.');
    router.replace({ name: 'AdminMemberList' });
  } catch (error) {
    if (axios.isAxiosError(error)) {
      vAlert(error.response?.data.message);
    }
  }
};

onMounted(() => fetchProfile(props.id as string | string[]));
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
