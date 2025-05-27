import React, { useState } from 'react';
import axiosInstance from '../api/axiosInstance';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; // AuthContext'ten useAuth hook'unu import ediyoruz

function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth(); // useAuth hook'unu kullanarak login fonksiyonunu alıyoruz

  // --- State Tanımları ---
  // Kullanıcı adı/email input değeri için state
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  // Şifre input değeri için state
  const [password, setPassword] = useState('');
  // Şifre görünür olup olmadığını kontrol eden state
  const [showPassword, setShowPassword] = useState(false);
  // Hata mesajlarını göstermek için state
  const [error, setError] = useState('');
  // Loading durumu için state (isteğin gönderilip gönderilmediğini takip etmek için)
  const [isLoading, setIsLoading] = useState(false);

  // --- Input Değişikliklerini Yöneten Fonksiyonlar ---
  // Kullanıcı adı/email inputu değiştiğinde çalışır
  const handleUsernameOrEmailChange = (event) => {
    setUsernameOrEmail(event.target.value); // State'i inputun yeni değeriyle güncelle
  };

  // Şifre inputu değiştiğinde çalışır
  const handlePasswordChange = (event) => {
    setPassword(event.target.value); // State'i inputun yeni değeriyle güncelle
  };

  // --- Form Gönderme İşlemi ---
  // Form gönderildiğinde (submit edildiğinde) çalışacak asenkron fonksiyon
  const handleSubmit = async (event) => {
    event.preventDefault(); // Tarayıcının formu göndermesini ve sayfayı yeniden yüklemesini engelle

    setError(''); // Yeni bir denemede eski hataları temizle
    setIsLoading(true); // Yükleniyor durumunu başlat (düğmeyi devre dışı bırak vs.)

    console.log('Login Denemesi:', { usernameOrEmail, password });

    try {
      // axiosInstance kullanarak login isteği gönder
      const response = await axiosInstance.post('/auth/login', {
        usernameOrEmail: usernameOrEmail,
        password: password
      });

      console.log('Login başarılı:', response.data);

      // Backend'den gelen yanıttan gerekli verileri al
      const { userId, username, roles, token, refreshToken } = response.data;
      
      if (!token) {
        throw new Error('Invalid response from server: Missing token');
      }

      // Token'ları ve kullanıcı bilgilerini localStorage'a kaydetme işlemini
      // artık AuthContext'teki login fonksiyonu yapacak
      console.log('Token ve kullanıcı bilgileri kaydedildi');
      
      // Kullanıcı bilgilerini oluştur ve kaydet
      const userDetails = { 
        id: userId, 
        username, 
        roles,
        // Diğer gerekli kullanıcı bilgileri
      };
      
      localStorage.setItem('user', JSON.stringify(userDetails));
      console.log('Kullanıcı bilgileri kaydedildi:', userDetails);

      // AuthContext'teki login fonksiyonunu kullanarak state'i güncelle
      // refreshToken'ı da iletiyoruz
      login(token, refreshToken, userDetails);

      // Rol tabanlı yönlendirme
      if (roles.includes('ROLE_ADMIN')) {
        console.log("Yönlendiriliyor: ADMIN -> /admin");
        navigate('/admin');
      }
      else if (roles.includes('ROLE_TEACHER')) {
        console.log("Yönlendiriliyor: TEACHER -> /teacher");
        navigate('/teacher');
      }
      else if (roles.includes('ROLE_STUDENT')) {
        console.log("Yönlendiriliyor: STUDENT -> /student");
        navigate('/student');
      } else {
        console.warn("Beklenmeyen rol. Anasayfaya yönlendiriliyor.");
        navigate('/');
      }

    } catch (error) {
      console.error('Login hatası:', error);
      
      // Hata mesajını ayarla
      let errorMessage = 'Giriş başarısız. Lütfen bilgilerinizi kontrol edin.';
      
      if (error.response) {
        // Sunucudan hata yanıtı geldiyse
        const { status, data } = error.response;
        console.error('Hata detayları:', { status, data });
        
        if (status === 401) {
          errorMessage = 'Geçersiz kullanıcı adı veya şifre.';
        } else if (status >= 500) {
          errorMessage = 'Sunucu hatası. Lütfen daha sonra tekrar deneyin.';
        } else if (data && data.message) {
          errorMessage = data.message;
        }
      } else if (error.request) {
        // İstek gönderildi ama yanıt alınamadı
        console.error('Sunucudan yanıt alınamadı:', error.request);
        errorMessage = 'Sunucuya bağlanılamıyor. Lütfen internet bağlantınızı kontrol edin.';
      } else {
        // İstek oluşturulurken hata oluştu
        console.error('İstek oluşturulurken hata:', error.message);
        errorMessage = 'İstek gönderilirken bir hata oluştu.';
      }
      
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  // --- Komponentin Render Ettiği JSX ---
  return (
        <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px', border: '1px solid #ddd', borderRadius: '8px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}> {/* Basit stil eklendi */}
            <h2>Giriş Yap</h2>

            {/* Hata Mesajını Gösterme Alanı */}
            {/* error state'i boş değilse (yani bir hata oluştuysa) bu paragrafı göster */}
            {error && <p style={{ color: 'red', marginBottom: '15px' }}>{error}</p>} {/* Hata mesajına margin eklendi */}

            {/* Login Formu */}
            {/* Form submit edildiğinde handleSubmit fonksiyonu çalışacak */}
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '15px' }}> {/* Kullanıcı Adı / Email input alanı için kapsayıcı */}
                    <label htmlFor="usernameOrEmail" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Kullanıcı Adı / Email:</label> {/* Label stili eklendi */}
                    {/* htmlFor ve id aynı olmalı, erişilebilirlik için */}
                    <input
                        type="text" // Metin input alanı
                id="usernameOrEmail" // Label ile eşleşen ID
                name="usernameOrEmail" // Form verisi için isim (state ismiyle aynı olması iyi bir pratik)
                value={usernameOrEmail} // Inputun güncel değeri state'ten alınır (Controlled Component)
                onChange={handleUsernameOrEmailChange} // Input değeri değiştikçe handleUsernameOrEmailChange çalışır ve state'i günceller
                required // Alanın doldurulması zorunlu
                disabled={isLoading} // İstek gönderilirken inputu devre dışı bırak (kullanıcının tekrar tıklamasını engellemek için)
                style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }} 
            />
        </div>


        <div style={{ marginBottom: '10px', position: 'relative' }}> {/* Şifre input alanı için kapsayıcı */}
            <label htmlFor="password" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Şifre:</label> {/* Label stili eklendi */}
            <div style={{ display: 'flex', alignItems: 'center' }}>
                <input
                    type={showPassword ? 'text' : 'password'} // Şifre görünür olup olmadığına göre type'ı değiştir
                    id="password"
                    name="password"
                    value={password}
                    onChange={handlePasswordChange}
                    required
                    disabled={isLoading}
                    style={{
                        width: '100%',
                        padding: '10px',
                        border: '1px solid #ddd',
                        borderRadius: '4px',
                        boxSizing: 'border-box',
                        marginRight: '30px' // Eye icon için yer aç
                    }}
                />
                {/* Eye icon */}
                <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    style={{
                        position: 'absolute',
                        right: 10,
                        top: 50,
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        padding: '0',
                        width: '20px',
                        height: '20px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#666'
                    }}
                >
                    <svg
                        width="20"
                        height="20"
                        viewBox="0 0 24 24"
                        fill={showPassword ? '#007bff' : '#666'}
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path d="M12 15c1.659 0 3-1.341 3-3s-1.341-3-3-3-3 1.341-3 3 1.341 3 3 3zm0-6c2.761 0 5 2.239 5 5s-2.239 5-5 5-5-2.239-5-5 2.239-5 5-5zM12 13c-2.761 0-5-2.239-5-5s2.239-5 5-5 5 2.239 5 5-2.239 5-5 5zm-1-8h2v2h-2V5zm0 4h2v6h-2V9z"/>
                    </svg>
                </button>
            </div>
        </div>
        
        {/* Şifremi Unuttum Linki */}
        <div style={{ marginBottom: '20px', textAlign: 'right' }}>
            <a 
                href="/password-reset" 
                style={{ 
                    color: '#007bff', 
                    textDecoration: 'none', 
                    fontSize: '0.9em' 
                }}
                onClick={(e) => {
                    e.preventDefault();
                    navigate('/password-reset');
                }}
            >
                Şifremi Unuttum
            </a>
        </div>

            {/* Formu gönderme düğmesi */}
            {/* type="submit" formu gönderir */}
            {/* disabled={isLoading} true ise düğmeyi devre dışı bırakır */}
            <button type="submit" disabled={isLoading} style={{ width: '100%', padding: '10px', backgroundColor: '#5cb85c', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '1em', transition: 'background-color 0.3s ease' }}> {/* Düğme stili eklendi */}
                {/* İstek gönderilirken düğme metnini değiştir */}
                {isLoading ? 'Giriş Yapılıyor...' : 'Giriş Yap'}
            </button>
        </form>
    </div>
);
}

export default LoginPage; // Bu komponenti diğer dosyalarda (App.jsx gibi) kullanabilmek için dışarıya aktar
