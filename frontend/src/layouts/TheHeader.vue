<template>
  <header>
    <nav class="navbar navbar-expand-sm navbar-dark bg-primary">
      <div class="container-fluid">
        <a class="navbar-brand" @click.prevent="goPage('HomeView')">Dailog</a>
        <button
          aria-controls="navbarSupportedContent"
          aria-expanded="false"
          aria-label="Toggle navigation"
          class="navbar-toggler"
          data-bs-target="#navbarSupportedContent"
          data-bs-toggle="collapse"
          type="button"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
        <div id="navbarSupportedContent" class="collapse navbar-collapse">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <a class="nav-link" @click.prevent="goPage('PostList')"
                >게시글 목록</a
              >
            </li>
            <li v-if="user.role === 'ADMIN'" class="nav-item">
              <a class="nav-link" @click.prevent="goPage('AdminMemberList')"
                >회원 목록</a
              >
            </li>
          </ul>

          <ul class="navbar-nav ms-auto">
            <li v-if="loggedIn" class="nav-item">
              <span class="navbar-text">
                <a
                  class="nav-link d-inline p-0 profile-link"
                  @click.prevent="showProfile(user.id)"
                >
                  {{ user.nickname }} </a
                >님 안녕하세요!
              </span>
            </li>
            <li v-if="!loggedIn" class="nav-item">
              <a class="nav-link" @click.prevent="goPage('LoginView')"
                >로그인</a
              >
            </li>
            <li v-if="!loggedIn" class="nav-item">
              <a class="nav-link" @click.prevent="goPage('JoinView')"
                >계정 만들기</a
              >
            </li>
            <li v-if="loggedIn" class="nav-item">
              <a class="nav-link" @click.prevent="logout">로그아웃</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </header>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router';
import { computed, onMounted } from 'vue';
import { useAlert } from '@/composables/useAlert';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const { vSuccess } = useAlert();

const authStore = useAuthStore();
const loggedIn = computed(() => authStore.isLoggedIn);
const user = computed(() => authStore.user);
const goPage = (pageName: string) => {
  router.push({ name: pageName });
};

const showProfile = (id: string) => {
  router.push({ name: 'MemberDetail', params: { id } });
};

const logout = async () => {
  try {
    await axios.post('/api/auth/logout');
    authStore.cleanToken();
    router.replace({ name: 'HomeView' });
    vSuccess('로그아웃 되었습니다.');
  } catch (error) {
    console.error('로그아웃 실패:', error);
  }
};

onMounted(() => {
  authStore.initStore();
});
</script>

<style scoped>
.navbar-text {
  color: white;
  display: inline;
}

.nav-link {
  display: inline;
}

.profile-link {
  border-bottom: 1px solid transparent;
  transition: border-bottom 0.2s ease-in-out;
}

.profile-link:hover {
  border-bottom: 1px solid white;
}
</style>
