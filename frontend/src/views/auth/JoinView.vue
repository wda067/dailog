<template>
  <div class="col-5 mx-auto">
    <h2>계정 생성</h2>
    <hr class="my-4" />

    <form class="form-container" @submit.prevent="join">
      <div class="mb-3">
        <input
          v-model="form.email"
          class="form-control"
          placeholder="이메일"
          type="text"
        />
      </div>
      <div class="position-relative">
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
      <small class="mb-0">
        * 비밀번호는 8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해 주세요.
      </small>
      <div class="mb-3">
        <input
          v-model="form.name"
          class="form-control"
          placeholder="이름"
          type="text"
        />
      </div>
      <div class="mb-3">
        <input
          v-model="form.nickname"
          class="form-control"
          placeholder="별명"
          type="text"
        />
      </div>
      <div class="d-grid mx-auto">
        <button class="btn btn-primary">계정 생성</button>
      </div>
    </form>
  </div>
</template>

<script lang="ts" setup>
import axios from 'axios';
import { ref } from 'vue';
import { useAlert } from '@/composables/useAlert';
import { useRouter } from 'vue-router';

const { vAlert, vSuccess } = useAlert();
const router = useRouter();

const form = ref({
  email: '',
  password: '',
  name: '',
  nickname: '',
});

const showPassword = ref(false);

const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

const join = async () => {
  try {
    await axios.post('/api/auth/join', {
      email: form.value.email,
      password: form.value.password,
      name: form.value.name,
      nickname: form.value.nickname,
    });
    vSuccess('계정이 생성되었습니다.');
    router.replace({ name: 'HomeView' });
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
      } else if (validation?.name) {
        vAlert(validation.name);
      } else if (validation?.nickname) {
        vAlert(validation.nickname);
      } else {
        vAlert(response.data?.message || '계정 생성 중 오류가 발생하였습니다.');
      }
    } else {
      vAlert('계정 생성 중 오류가 발생하였습니다.');
    }
  }
};
</script>

<style scoped>
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
</style>
