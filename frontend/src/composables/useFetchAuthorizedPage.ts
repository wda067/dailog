import { useRouter } from 'vue-router';
import axiosInstance from '@/composables/useApi';
import fetchReissue from '@/composables/useFetchReissue';

const router = useRouter();

const fetchAuthorizedPage = async (
  url: string,
): Promise<string | undefined> => {
  try {
    const response = await axiosInstance.post(
      url,
      {},
      {
        withCredentials: true,
      },
    );

    if (response.status === 200) {
      return response.data;
    } else {
      //401 -> 재발급 요청
      //성공 시 다시 권한 페이지 fetch, 실패 시 로그인 페이지로 이동
      const reissueSuccess = await fetchReissue();
      if (reissueSuccess) {
        return await fetchAuthorizedPage(url);
      } else {
        alert('접근 권한이 없습니다.');
        router.push({ name: 'LoginView' });
      }
    }
  } catch (error) {
    console.error('Error fetching authorized page:', error);
  }
};

export default fetchAuthorizedPage;
