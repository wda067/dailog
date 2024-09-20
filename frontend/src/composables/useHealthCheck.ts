import axios from 'axios';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {useAuthStore} from "@/stores/auth";

export function useHealthCheck() {
  const isServerAlive = ref(true);
  const wasServerDown = ref(false);
  const router = useRouter();
  const authStore = useAuthStore();

  //서버 상태를 확인하는 함수
  async function checkServerStatus() {
    try {
      const response = await axios.get('/api/health-check');
      if (response.status !== 200) {
        return;
      }

      isServerAlive.value = true;

      if (wasServerDown.value) {
        wasServerDown.value = false;
        window.location.reload();
        await router.replace({name: 'HomeView'});
      }
    } catch (error) {
      console.error('Server connection failed:', error);
      isServerAlive.value = false;
      wasServerDown.value = true;
      authStore.cleanToken();
    }
  }

  //서버 상태를 주기적으로 체크하는 함수
  function startServerMonitor(interval = 5000) {
    setInterval(async () => {
      await checkServerStatus();
      if (!isServerAlive.value) {
        localStorage.clear();
        alert('서버와의 연결이 끊겼습니다.');
      }
    }, interval);
  }

  return {
    isServerAlive,
    startServerMonitor,
  };
}
