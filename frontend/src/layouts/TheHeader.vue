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
          <ul class="navbar-nav me-auto d-flex align-items-center">
            <li class="nav-item">
              <a class="nav-link" @click.prevent="goPage('PostList')"
                >커뮤니티</a
              >
            </li>
            <li v-if="user.role === 'ADMIN'" class="nav-item">
              <a class="nav-link" @click.prevent="goPage('AdminMemberList')"
                >회원 목록</a
              >
            </li>
            <li class="nav-item d-flex align-items-center ms-2">
              <form class="d-flex position-relative" @submit.prevent>
                <input
                  class="form-control me-2"
                  placeholder="주식 티커로 검색"
                  type="text"
                  @input="updateSearchTicker($event)"
                  style="width: 250px; height: 40px"
                />
<!--                <button class="btn btn-primary" type="submit">-->
<!--                  <i class="bi bi-search"></i>-->
<!--                </button>-->
                <button
                  type="button"
                  class="btn btn-sm"
                  @click="clearSearch"
                  style="position: absolute; right: 15px; height: 40px; top: 50%; transform: translateY(-50%); background: none; border: none; font-size: 1.5rem; padding: 0;"
                >
                  &times;
                </button>
              </form>
              <ul v-if="suggestions.length != 0" class="autocomplete-list">
                <li
                  v-for="(suggestion, index) in suggestions"
                  :key="index"
                  @click="selectStock(suggestion)"
                >
                  <span class="stock-name">{{ truncateName(suggestion.name) }}</span><br>
                  <span class="stock-ticker">{{ suggestion.ticker }} {{ suggestion.exchange }}</span>
                </li>
              </ul>
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
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
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
    await router.replace({ name: 'HomeView' });
    vSuccess('로그아웃 되었습니다.');
  } catch (error) {
    console.error('로그아웃 실패:', error);
  }
};

const searchTicker = ref('');
const suggestions = ref<{
  ticker: string;
  exchange: string;
  name: string;
}[]>([])

const updateSearchTicker = (event: Event) => {
  searchTicker.value = (event.target as HTMLInputElement).value;
  fetchStocks();  // 입력 값에 따라 즉시 검색을 수행
};

const fetchStocks = async () => {
  if (searchTicker.value.length < 1) {
    suggestions.value = [];
    return;
  }
  try {
    const response = await axios.get(`/api/stock/search`, {
      params: { query: searchTicker.value }
    });
    suggestions.value = response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const response = error.response;
      if (response?.status === 404) {
        suggestions.value = [{ name: '검색 결과가 없습니다.', ticker: '', exchange: '' }];
        return;
      }
    }
      console.error('자동완성 데이터를 가져오는 중 오류 발생:', error);
  }
};

const selectStock = (stock: { ticker: string; name: string; }) => {
  searchTicker.value = stock.ticker;
  suggestions.value = [];
  searchTicker.value = '';
  router.push({ name: 'StockDetail', params: { ticker: stock.ticker } });
};

const clearSearch = () => {
  searchTicker.value = '';
  suggestions.value = [];
};

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  const formElement = document.querySelector('.position-relative');
  if (formElement && !formElement.contains(target)) {
    suggestions.value = []; //외부 클릭 시 suggestions 비우기
  }
};

const truncateName = (name: string) => {
  const maxLength = 20;
  return name.length > maxLength ? name.slice(0, maxLength).trimEnd() + '...' : name;
};

onMounted(() => {
  authStore.initStore();
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
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

.autocomplete-list {
  position: absolute;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 4px; /* 모서리를 둥글게 */
  width: 250px; /* 검색창 크기에 맞게 조정 */
  z-index: 1000;
  top: 100%; /* form 하단에 리스트가 뜨도록 설정 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* 그림자 효과 추가 */
  padding: 0;
}

.autocomplete-list li {
  padding: 10px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.autocomplete-list li:hover {
  background-color: #f0f0f0;
}

.autocomplete-list .stock-name {
  font-size: 16px; /* 원하는 크기로 설정 */
  font-weight: bold;
}

.autocomplete-list .stock-ticker {
  font-size: 13px; /* 원하는 크기로 설정 */
  color: gray; /* 회색 설정 */
}
</style>
