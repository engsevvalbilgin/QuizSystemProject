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
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
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
    // Store the interceptor to be able to remove it later
    const interceptor = axiosInstance.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;
        
        // If error is not 401 or it's a refresh token request, reject
        if (error.response?.status !== 401 || 
            NO_TOKEN_ENDPOINTS.some(endpoint => originalRequest.url.includes(endpoint))) {
          return Promise.reject(error);
        }

        // If already refreshing, add to queue
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            const retryPromise = new Promise((retryResolve, retryReject) => {
              failedQueue.push({ resolve: retryResolve, reject: retryReject });
            });
            
            retryPromise.then((token) => {
              if (token) {
                originalRequest.headers.Authorization = `Bearer ${token}`;
                return axiosInstance(originalRequest);
              }
              return Promise.reject(error);
            }).catch(reject);
          });
        }

        isRefreshing = true;
        
        try {
          // Call the refresh token function
          const newToken = await refreshTokenFn();
          
          if (newToken) {
            // Update the original request with the new token
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            
            // Process any queued requests with the new token
            processQueue(null, newToken);
            
            // Retry the original request
            return axiosInstance(originalRequest);
          }

          // If refresh token failed, logout the user
          if (logoutFn) {
            logoutFn();
          }
          processQueue(error, null);
          return Promise.reject(error);
        } catch (refreshError) {
          // If refresh token failed, logout the user
          if (logoutFn) {
            logoutFn();
          }
          processQueue(refreshError, null);
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
          // Clear the queue after refresh
          failedQueue = [];
        }
      }
    );

    // Return the interceptor ID so it can be cleaned up
    return interceptor;
  };

export default axiosInstance;