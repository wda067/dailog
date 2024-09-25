import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';

export async function useOAuth2Redirect() {
  const router = useRouter();
  const route = useRoute();
  const authStore = useAuthStore();
  const { setIsLoggedIn, getUserInfo } = authStore;

  const OAuthJWTHeaderFetch = async () => {
    const queryParams = new URLSearchParams(route.query as any);
    try {
      const response = await axios.post(
        '/api/oauth2-jwt-header',
        {},
        {
          withCredentials: true,
        },
      );

      if (response.status === 200) {
        const accessToken = response.headers['access'] || '';
        window.localStorage.setItem('access', accessToken);

        const name = queryParams.get('name') || '';
        const nickname = queryParams.get('nickname') || '';
        const role = queryParams.get('role') || '';
        localStorage.setItem('name', name);
        localStorage.setItem('nickname', nickname);
        localStorage.setItem('role', role);

        await getUserInfo();
        setIsLoggedIn(true);
      } else {
        alert('접근할 수 없는 페이지입니다.');
      }
      await router.replace({ name: 'HomeView' });
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.error('Error fetching profile:', error.response?.statusText);
      }
    }
  };

  await OAuthJWTHeaderFetch();
}
