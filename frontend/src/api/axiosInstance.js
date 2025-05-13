// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\api\axiosInstance.js
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

// İstek Interceptor'ı: Her istek gönderilmeden önce çalışır
axiosInstance.interceptors.request.use(
  (config) => {
    // localStorage'dan JWT token'ı al
    const token = localStorage.getItem('token'); // <-- Token'ı 'token' anahtarıyla alıyoruz

    // Token varsa ve istek login veya register endpoint'ine gitmiyorsa
    // Authorization başlığına token'ı ekle
    // Login ve Register endpoint'leri token gerektirmez, bu yüzden hariç tutulurlar.
    const isLoginOrRegister = config.url.includes('/auth/login') || config.url.includes('/auth/register');

    if (token && !isLoginOrRegister) {
      config.headers['Authorization'] = `Bearer ${token}`;
      console.log(`Axios Interceptor: Token eklendi - ${config.method.toUpperCase()} ${config.url}`);
    } else {
       // Token yoksa veya login/register isteği ise token eklenmez
       if (!token && !isLoginOrRegister) {
           console.log(`Axios Interceptor: Token yok, eklenmedi - ${config.method.toUpperCase()} ${config.url}`);
       } else {
           console.log(`Axios Interceptor: Login/Register isteği, token eklenmedi - ${config.method.toUpperCase()} ${config.url}`);
       }
    }

    return config; // Güncellenmiş istek yapılandırmasını döndür
  },
  (error) => {
    // İstek hatası durumunda yapılacaklar
    console.error('Axios Interceptor (Request Error):', error);
    return Promise.reject(error); // Hatayı yay
  }
);

// Yanıt Interceptor'ı: Her yanıt alındıktan sonra çalışır
axiosInstance.interceptors.response.use(
  (response) => {
    // Başarılı yanıtlar için yapılacaklar
    console.log(`Axios Interceptor: ${response.status} yanıtı alındı - ${response.config.method.toUpperCase()} ${response.config.url}`);
    return response; // Yanıtı döndür
  },
  (error) => {
    // Hatalı yanıtlar için yapılacaklar
    console.error('Axios Interceptor (Response Error):', error.response || error.message || error);

    // Örneğin, 401 Unauthorized hatası alınırsa (token süresi dolmuş olabilir)
    if (error.response && error.response.status === 401) {
      console.log('Axios Interceptor: 401 Unauthorized yanıtı alındı. Muhtemelen token süresi dolmuş veya geçersiz.');
      // TODO: Kullanıcıyı login sayfasına yönlendirme veya token yenileme işlemleri yapılabilir
      // Örnek: window.location.href = '/login'; // Sayfayı login sayfasına yönlendir
    }
     // 403 Forbidden hatası alınırsa (yetkisiz erişim)
     if (error.response && error.response.status === 403) {
         console.log('Axios Interceptor: 403 Forbidden yanıtı alındı. Yetkiniz yok.');
         // TODO: Kullanıcıya yetkisiz erişim mesajı gösterme veya farklı bir sayfaya yönlendirme
     }

    return Promise.reject(error); // Hatayı yay
  }
);

// Oluşturulan instance'ı dışa aktar
export default axiosInstance;
