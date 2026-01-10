import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

const api = axios.create({
  baseURL: `${import.meta.env.VITE_BACKEND_URL}/api/`,
})

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if ((error.response.status === 403 || error.response.status === 401) && !originalRequest._retry) {

      if (originalRequest.url.includes('auth/refresh')) {
        const authStore = useAuthStore();
        authStore.logout();
        router.push('/login');
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise(function (resolve, reject) {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const authStore = useAuthStore();

      try {
        const newToken = await authStore.refreshAccessToken();

        processQueue(null, newToken);
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return api(originalRequest);

      } catch (err) {
        processQueue(err, null);
        authStore.logout();
        router.push('/login');
        return Promise.reject(err);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;
