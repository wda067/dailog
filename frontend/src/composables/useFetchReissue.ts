import axios from 'axios';
import { useCookies } from 'vue3-cookies';

//access token 만료, 쿠키에 refresh token 존재할 때
//access token 만료 -> 백엔드에서 401 응답 -> 프론트에서 재발급 요청
const fetchReissue = async () => {
  try {
    const response = await axios.post(
      '/api/auth/reissue',
      {},
      {
        withCredentials: true,
      },
    );

    if (response.status === 200) {
      const accessToken = response.headers['access'] || '';
      window.localStorage.setItem('access', accessToken);
      return true;
    } else {
      window.localStorage.removeItem('access');
      useCookies().cookies.set('refresh', '', 0);
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      console.log(error.response?.data.message);
    }
  }
  return false;
};

export default fetchReissue;
