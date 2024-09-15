// stores/auth.js
import { defineStore } from 'pinia';
import axios from 'axios';
import axiosInstance from '@/composables/useApi';

interface User {
  id: string;
  name: string;
  nickname: string;
  role: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: !!localStorage.getItem('access'),
    user: {
      id: JSON.parse(localStorage.getItem('id') || '""'),
      name: JSON.parse(localStorage.getItem('name') || '""'),
      nickname: JSON.parse(localStorage.getItem('nickname') || '""'),
      role: JSON.parse(localStorage.getItem('role') || '""'),
    } as User,
  }),
  actions: {
    setIsLoggedIn(status: boolean) {
      this.isLoggedIn = status;
    },
    async getUserInfo() {
      try {
        const { data } = await axiosInstance.get('/api/member/me');
        this.user = {
          id: data.id,
          name: data.name,
          nickname: data.nickname,
          role: data.role,
        };
        localStorage.setItem('id', JSON.stringify(this.user.id));
        localStorage.setItem('name', JSON.stringify(this.user.name));
        localStorage.setItem('nickname', JSON.stringify(this.user.nickname));
        localStorage.setItem('role', JSON.stringify(this.user.role));
      } catch (error) {
        if (axios.isAxiosError(error)) {
          console.error('Error fetching profile:', error.response?.statusText);
        }
      }
    },
    cleanToken() {
      this.user = { id: '', name: '', nickname: '', role: '' };
      this.isLoggedIn = false;
      localStorage.removeItem('access');
      localStorage.removeItem('id');
      localStorage.removeItem('name');
      localStorage.removeItem('nickname');
      localStorage.removeItem('role');
    },
    initStore() {
      this.isLoggedIn = !!localStorage.getItem('access');
      this.user.id = JSON.parse(localStorage.getItem('id') || '""');
      this.user.name = JSON.parse(localStorage.getItem('name') || '""');
      this.user.nickname = JSON.parse(localStorage.getItem('nickname') || '""');
      this.user.role = JSON.parse(localStorage.getItem('role') || '""');
    },
  },
});
