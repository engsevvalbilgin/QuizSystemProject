import axios from 'axios';

// Backend API'mizin temel URL'si
const API_BASE_URL = 'http://localhost:8080/api'; // Backend'inizin çalıştığı adres ve API path'i

// Özel bir Axios instance'ı oluştur
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json', // İsteklerin JSON formatında olacağını belirtir
  },
});

// Axios interceptor'larını kurmak için fonksiyon
export const setupAxiosInterceptors = (refreshToken, logout) => {
    // İstek Interceptor'ı: Her istek gönderilmeden önce çalışır
    axiosInstance.interceptors.request.use(
        (config) => {
            // TOKEN EKLENMEMESİ GEREKEN ENDPOINT'LER
            const NO_TOKEN_ENDPOINTS = [
                '/auth/login', 
                '/auth/register',
                '/auth/register/teacher',
                '/teachers/register',
                '/auth/forgot-password',
                '/auth/reset-password',
                '/auth/verify-email',
                '/users/password-reset/request',
                '/users/password-reset/complete'
            ];
            
            // Endpoint kontrolü - tam url yerine, sadece path kontrolü yap
            const shouldSkipToken = NO_TOKEN_ENDPOINTS.includes(config.url);
            
            // Özel log ekle
            console.log(`Axios Request: ${config.method.toUpperCase()} ${config.url} - Token Gerekli: ${!shouldSkipToken}`);
            
            // Eğer token gereken bir endpoint ise ve token mevcutsa, token'i ekle
            if (!shouldSkipToken) {
                const token = localStorage.getItem('token');
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                    console.log(`Axios: PRIVATE ENDPOINT - Token eklendi - ${config.url}`);
                }
            }
            
            // Eğer token gereken bir endpoint değilse veya token yoksa, Authorization header'ını temizle
            if (shouldSkipToken || !localStorage.getItem('token')) {
                if (config.headers.Authorization) {
                    delete config.headers.Authorization;
                }
            }

            return config;
        },
        (error) => {
            console.error('Axios Request Error:', error);
            return Promise.reject(error);
        }
    );

    // Yanıt Interceptor'ı: Her yanıt alındıktan sonra çalışır
    axiosInstance.interceptors.response.use(
        (response) => {
            // Başarılı yanıtlar için yapılacaklar
            console.log(`Axios Interceptor: ${response.status} yanıtı alındı - ${response.config.method.toUpperCase()} ${response.config.url}`);
            return response; // Yanıtı döndür
        },
        async (error) => {
            const originalRequest = error.config;
            
            // Hatalı yanıtlar için yapılacaklar - daha detaylı loglama yapalım
            console.error('Axios Interceptor (Response Error):', error.response || error.message || error);
            
            // Detaylı hata bilgisi
            if (error.response) {
                // Sunucudan dönen yanıt var (4xx veya 5xx)
                console.error(`Detaylı Hata [${error.response.status}]:`, {
                    endpoint: originalRequest.url,
                    method: originalRequest.method.toUpperCase(),
                    statusText: error.response.statusText,
                    data: error.response.data,
                    headers: error.response.headers,
                    hasToken: !!originalRequest.headers['Authorization']
                });
            }

            // 401 Unauthorized hatası alınırsa (token süresi dolmuş olabilir) ve daha önce yenileme denemesi yapılmadıysa
            if (error.response && 
                error.response.status === 401 && 
                !originalRequest._retry && // Önceden denenmediğinden emin ol
                localStorage.getItem('token')) { // Token varsa yenileme dene
                
                originalRequest._retry = true; // Bu isteği yeniden denediğimizi işaretle
                console.log('Axios Interceptor: 401 hatası - Token yenileme deneniyor...');
                
                try {
                    // Token'i refresh et
                    const refreshSuccess = await refreshToken();
                    
                    if (refreshSuccess) {
                        // Yeni token ile orijinal isteği tekrar gönder
                        return axiosInstance(originalRequest);
                    }
                } catch (refreshError) {
                    console.error('Token refresh hatası:', refreshError);
                    // Token refresh başarısız olduğunda logout yap
                    logout();
                    window.location.href = '/login';
                }
            }
            
            // Diğer hatalar için normal hata yönetimi
            return Promise.reject(error);
        }
    );
};

// Oluşturulan instance'ı dışa aktar
export default axiosInstance;