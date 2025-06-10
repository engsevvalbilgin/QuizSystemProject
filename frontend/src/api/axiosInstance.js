import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const axiosInstance = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  const queue = [...failedQueue];
  failedQueue = [];
  queue.forEach(({ resolve, reject, unsubscribe }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token);
    }
  });
};

const NO_TOKEN_ENDPOINTS = [
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/register/teacher',
  '/api/teachers/register',
  '/api/auth/forgot-password',
  '/api/auth/reset-password',
  '/api/auth/verify-email',
  '/api/users/password-reset/request',
  '/api/users/password-reset/complete',
  '/api/auth/refresh-token'
];

axiosInstance.interceptors.request.use(
  (config) => {
    const requestUrl = config.url || '';
    const shouldSkipToken = NO_TOKEN_ENDPOINTS.some(endpoint => 
      requestUrl.includes(endpoint)
    );
    
    if (!shouldSkipToken) {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const setupAxiosInterceptors = (refreshTokenFn, logoutFn) => {
    axiosInstance.interceptors.response.eject(0);
    axiosInstance.interceptors.request.eject(0);

    axiosInstance.interceptors.request.use(
        (config) => {
            const requestUrl = config.url || '';
            const shouldSkipToken = NO_TOKEN_ENDPOINTS.some(endpoint => 
                requestUrl.includes(endpoint)
            );
            
            if (!shouldSkipToken) {
                const token = localStorage.getItem('token');
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
            }
            
            return config;
        },
        (error) => {
            return Promise.reject(error);
        }
    );

    const interceptor = axiosInstance.interceptors.response.use(
        (response) => response,
        async (error) => {
            const originalRequest = error.config;
            
            if (error.response?.status !== 401 || 
                originalRequest._retry ||
                NO_TOKEN_ENDPOINTS.some(endpoint => originalRequest.url.includes(endpoint))) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;

            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    const unsubscribe = () => {
                        const token = localStorage.getItem('token');
                        if (token) {
                            originalRequest.headers.Authorization = `Bearer ${token}`;
                            resolve(axiosInstance(originalRequest));
                        } else {
                            reject(error);
                        }
                    };
                    failedQueue.push({ resolve: unsubscribe, unsubscribe });
                });
            }

            isRefreshing = true;
            
            try {
                const newToken = await refreshTokenFn();
                if (newToken) {
                    originalRequest.headers.Authorization = `Bearer ${newToken}`;
                    processQueue(null, newToken);
                    
                    return axiosInstance(originalRequest);
                }
                
                throw new Error('No new token received');
            } catch (refreshError) {
                console.error('Token refresh failed:', refreshError);
                processQueue(refreshError, null);
                if (logoutFn) {
                    logoutFn();
                }
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }
    );

    return interceptor;
  };

export default axiosInstance;
