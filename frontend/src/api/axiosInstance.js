// axiosInstance.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

// Create Axios instance
const axiosInstance = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Token refresh state
let isRefreshing = false;
let failedQueue = [];

// Process queued requests
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

// Endpoints that don't require authentication
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

// Request interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    const requestUrl = config.url || '';
    const shouldSkipToken = NO_TOKEN_ENDPOINTS.some(endpoint => 
      requestUrl.includes(endpoint)
    );
    
    // Add token to request if needed
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
    // Remove any existing interceptors to prevent duplicates
    axiosInstance.interceptors.response.eject(0);
    axiosInstance.interceptors.request.eject(0);

    // Request interceptor to add auth token to requests
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

    // Response interceptor to handle token refresh
    const interceptor = axiosInstance.interceptors.response.use(
        (response) => response,
        async (error) => {
            const originalRequest = error.config;
            
            // If error is not 401 or it's a refresh token request, reject
            if (error.response?.status !== 401 || 
                originalRequest._retry ||
                NO_TOKEN_ENDPOINTS.some(endpoint => originalRequest.url.includes(endpoint))) {
                return Promise.reject(error);
            }

            // Mark this request as a retry
            originalRequest._retry = true;

            // If already refreshing, add to queue
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
                    // Update the original request with the new token
                    originalRequest.headers.Authorization = `Bearer ${newToken}`;
                    
                    // Process any queued requests with the new token
                    processQueue(null, newToken);
                    
                    // Retry the original request
                    return axiosInstance(originalRequest);
                }
                
                // If no new token, logout
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
