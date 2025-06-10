import React, { useEffect, useState, useRef } from 'react'; 
import { useLocation, Link } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function VerifyEmailPage() {
  const location = useLocation();

  const [verificationStatus, setVerificationStatus] = useState('loading');
  const [message, setMessage] = useState('');

  
  const hasAttempted = useRef(false); 

  useEffect(() => {
    
    if (hasAttempted.current) { 
        console.log("VerifyEmailPage: Doğrulama isteği zaten yapılmış, tekrar denenmiyor.");
        return;
    }

    const params = new URLSearchParams(location.search);
    const token = params.get('token');

    console.log("VerifyEmailPage: URL'den token alındı:", token);

    if (token) {
      const verifyToken = async () => {
        hasAttempted.current = true; 

        try {
          console.log("VerifyEmailPage: Backend'e doğrulama isteği gönderiliyor...");
          console.log("VerifyEmailPage: Kullanılan token:", token);
          const response = await axiosInstance.get(`/auth/verify-email?token=${token}`);
          

          console.log("VerifyEmailPage: Doğrulama başarılı:", response.data);
          setVerificationStatus('success');
          setMessage(response.data || "Hesabınız başarıyla etkinleştirildi!");

        } catch (err) {
          console.error("VerifyEmailPage: Doğrulama hatası:", err);
          console.log("Error response:", err.response);
          
          
          if (err.response && err.response.status === 500) {
            console.log("VerifyEmailPage: Sunucu hatası (500) - Muhtemelen hesap zaten etkinleştirilmiş");
            setVerificationStatus('success');
            setMessage("Bu e-posta adresi zaten doğrulanmış veya etkinleştirilmiş. Giriş yapabilirsiniz.");
            return;
          }
          
          
          setVerificationStatus('error');
          if (err.response) {
            if (err.response.data && err.response.data.message) {
                setMessage(err.response.data.message);
            } else if (typeof err.response.data === 'string') {
                setMessage(err.response.data);
            } else {
                setMessage(`Doğrulama başarısız. Status: ${err.response.status}`);
            }
          } else if (err.request) {
             setVerificationStatus('error');
             setMessage("Doğrulama başarısız. Sunucuya ulaşılamadı.");
          } else {
             setVerificationStatus('error');
             setMessage("Beklenmeyen bir hata oluştu.");
          }
        }
      };

      verifyToken(); 

    } else {
      
      console.error("VerifyEmailPage: URL'de doğrulama token'ı bulunamadı.");
      setVerificationStatus('error');
      setMessage("Doğrulama linki geçersiz veya eksik.");
      hasAttempted.current = true; 
    }

    
    

  }, [location.search]); 


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
