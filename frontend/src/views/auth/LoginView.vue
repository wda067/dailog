<template>
  <div class="login-container">
    <h2>로그인</h2>
    <hr class="my-4" />

    <form class="form-container" @submit.prevent="login">
      <div class="mb-3">
        <input
          v-model="form.email"
          class="form-control"
          placeholder="이메일"
          type="text"
        />
      </div>
      <div class="position-relative mb-3">
        <input
          v-model="form.password"
          :type="showPassword ? 'text' : 'password'"
          class="form-control"
          placeholder="비밀번호"
        />
        <div class="icon-container" @click="togglePassword">
          <i :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
        </div>
      </div>
      <div class="d-grid mx-auto">
        <button class="btn btn-primary mb-2">로그인</button>
      </div>
      <div class="sns-login">
        <span>SNS LOGIN</span>
      </div>
      <div class="flex-row">
        <a href="/oauth2/authorization/naver">
          <img alt="네이버 로그인" src="/images/btnG_아이콘사각.png" />
        </a>
        <a href="/oauth2/authorization/google">
          <img alt="구글 로그인" src="/images/web_neutral_sq_na@4x.png" />
        </a>
        <a href="/oauth2/authorization/kakao">
          <img alt="카카오 로그인" src="/images/kakao.png" />
        </a>
      </div>
    </form>
  </div>
</template>

<script lang="ts" setup>
import axios from 'axios';
import { ref } from 'vue';
import { useAlert } from '@/composables/useAlert';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const { vAlert, vSuccess } = useAlert();
const router = useRouter();
useRoute();
const authStore = useAuthStore();
const { setIsLoggedIn, getUserInfo } = authStore;

const form = ref({
  email: '',
  password: '',
});

const showPassword = ref(false);

const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

const login = async () => {
  try {
    const response = await axios.post('/api/auth/login', {
      email: form.value.email,
      password: form.value.password,
    });
    if (response.status === 200) {
      const accessToken = response.headers['access'];
      window.localStorage.setItem('access', accessToken);
      getUserInfo();
      setIsLoggedIn(true);
      vSuccess('로그인 되었습니다.');
      router.replace({ name: 'HomeView' });
    }
    //await profileStore.fetchProfile();
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      const { validation } = response?.data || {};

      if (!response) {
        vAlert('응답을 받을 수 없습니다.');
      } else if (validation === undefined) {
        vAlert(response.statusText);
      } else if (validation?.email) {
        vAlert(validation.email);
      } else if (validation?.password) {
        vAlert(validation.password);
      } else {
        vAlert(response.data?.message || '로그인 중 오류가 발생하였습니다.');
      }
    } else {
      vAlert('로그인 중 오류가 발생하였습니다.');
    }
  }
};
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
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
  gap: 20px;
  justify-content: center; /* 중앙 정렬 추가 */
}

.flex-row img {
  width: 60px;
  cursor: pointer;
}

.sns-login {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20px 0;
  width: 100%;
}

.sns-login::before,
.sns-login::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #ccc;
  margin: 0 10px;
}

.sns-login span {
  flex-shrink: 0;
  color: #888;
}
</style>
