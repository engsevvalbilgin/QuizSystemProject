// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\pages\VerifyEmailPage.jsx
import React, { useEffect, useState, useRef } from 'react'; // useRef hook'unu import et
import { useLocation, Link } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function VerifyEmailPage() {
  const location = useLocation();

  const [verificationStatus, setVerificationStatus] = useState('loading');
  const [message, setMessage] = useState('');

  // İsteğin zaten yapılıp yapılmadığını takip etmek için useRef kullanıyoruz
  // useRef değeri render'lar arasında kalıcıdır ve güncellenmesi render'ı tetiklemez
  const hasAttempted = useRef(false); // <-- useRef eklendi, başlangıç değeri false

  useEffect(() => {
    // Eğer istek zaten yapıldıysa tekrar yapma
    if (hasAttempted.current) { // <-- useRef değeri .current ile okunur
        console.log("VerifyEmailPage: Doğrulama isteği zaten yapılmış, tekrar denenmiyor.");
        return;
    }

    const params = new URLSearchParams(location.search);
    const token = params.get('token');

    console.log("VerifyEmailPage: URL'den token alındı:", token);

    if (token) {
      const verifyToken = async () => {
        hasAttempted.current = true; // <-- İstek başlatılıyor, durumu useRef'e kaydet

        try {
          console.log("VerifyEmailPage: Backend'e doğrulama isteği gönderiliyor...");
          const response = await axiosInstance.get(`/auth/verify-email?token=${token}`);

          console.log("VerifyEmailPage: Doğrulama başarılı:", response.data);
          setVerificationStatus('success');
          setMessage(response.data || "Hesabınız başarıyla etkinleştirildi!");

        } catch (err) {
          console.error("VerifyEmailPage: Doğrulama hatası:", err);
          setVerificationStatus('error');
          if (err.response) {
             if (err.response.data && err.response.data.message) {
                 setMessage(err.response.data.message);
             } else {
                 setMessage(`Doğrulama başarısız. Status: ${err.response.status}`);
             }
          } else if (err.request) {
             setMessage("Doğrulama başarısız. Sunucuya ulaşılamadı.");
          } else {
             setMessage("Beklenmeyen bir hata oluştu.");
          }
        }
      };

      verifyToken(); // Token bulunduysa doğrulama işlemini başlat

    } else {
      // URL'de token bulunamadı
      console.error("VerifyEmailPage: URL'de doğrulama token'ı bulunamadı.");
      setVerificationStatus('error');
      setMessage("Doğrulama linki geçersiz veya eksik.");
      hasAttempted.current = true; // <-- Token yoksa da denendi sayılır
    }

    // useEffect'in temizleme fonksiyonu (component unmount edildiğinde çalışır)
    // Bu senaryoda API çağrısını iptal etmek gibi şeyler yapılabilir, şimdilik gerek yok.
    // return () => {
    //     // Cleanup logic here if needed
    // };

  }, [location.search]); // location.search değiştiğinde effect'i tekrar çalıştır, hasAttempted useRef olduğu için dependency array'e eklenmez


  return (
    <div style={{ maxWidth: '500px', margin: '50px auto', textAlign: 'center', padding: '20px', border: '1px solid #ddd', borderRadius: '8px' }}>
      <h2>Email Doğrulama</h2>

      {verificationStatus === 'loading' && (
        <p>Hesabınız doğrulanıyor...</p>
      )}

      {verificationStatus === 'success' && (
        <div style={{ color: 'green' }}>
          <p>{message}</p>
          <Link to="/login">Giriş Yapmak İçin Tıklayın</Link>
        </div>
      )}

      {verificationStatus === 'error' && (
        <div style={{ color: 'red' }}>
          <p>{message}</p>
           <Link to="/login">Giriş Sayfasına Geri Dön</Link>
        </div>
      )}
    </div>
  );
}

export default VerifyEmailPage;
