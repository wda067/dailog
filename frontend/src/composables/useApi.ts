import axios from 'axios';
import fetchReissue from '@/composables/useFetchReissue';

const axiosInstance = axios.create();

axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('access');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  },
);

axiosInstance.interceptors.response.use(
  response => {
    return response;
  },
  async error => {
    const originalRequest = error.config;

    //401 에러 및 이미 한 번 시도했는지 체크
    if (error.response.status === 401 && !originalRequest._retry) {
      console.log('Access token is expired or invalid');
      originalRequest._retry = true;

      //토큰 재발급 요청
      const success = await fetchReissue();
      if (success) {
        const token = localStorage.getItem('access');
        if (token) {
          originalRequest.headers.Authorization = `Bearer ${token}`;
        }
        console.log('Token Reissue');
        //원래 요청 다시 시도
        return axiosInstance(originalRequest);
      }
    }
    return Promise.reject(error);
  },
);

export default axiosInstance;
