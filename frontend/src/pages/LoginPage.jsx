// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\pages\LoginPage.jsx
import React, { useState } from 'react'; // useState hook'unu import ediyoruz (input değerleri ve hata/loading durumu için)
// axios yerine axiosInstance'ımızı import ediyoruz
import axiosInstance from '../api/axiosInstance'; // <-- axiosInstance importu
import { useNavigate } from 'react-router-dom'; // useNavigate hook'unu import ediyoruz (sayfa yönlendirmesi için)

function LoginPage() {
  // useNavigate hook'u ile yönlendirme fonksiyonunu alıyoruz
  const navigate = useNavigate();

  // --- State Tanımları ---
  // Kullanıcı adı/email input değeri için state
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  // Şifre input değeri için state
  const [password, setPassword] = useState('');
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

    console.log('Login Denemesi:', { usernameOrEmail, password }); // Konsola loglayalım (gelen veriyi görmek için)

    // *** Backend API Çağrısı ***
    try {
      // axiosInstance kullanarak backend'deki login endpoint'ine POST isteği gönderiyoruz
      // axiosInstance, login endpoint'i olduğu için Authorization başlığı eklemeyecektir, bu doğru davranış
      const response = await axiosInstance.post('/auth/login', { // <-- axios yerine axiosInstance kullanıldı, tam endpoint yerine path kullanıldı (baseURL axiosInstance'ta tanımlı)
        // Backend'in beklediği alan isimleriyle state'teki değerleri gönderiyoruz
        usernameOrEmail: usernameOrEmail,
        password: password
      });

      // İstek başarılıysa (Backend 2xx status kodu döndürdüyse)
      console.log('Login başarılı:', response.data);

      // *** Başarılı Login Sonrası İşlemler ***

      // 1. JWT Token ve Kullanıcı Bilgilerini localStorage'a Saklama
      // Backend'den gelen yanıttan (response.data) token ve diğer kullanıcı bilgilerini al
      // Backend AuthResponseDto'su: { userId, username, roles (List<String>), token }
      // Backend'den gelen 'token' alanını alıyoruz (DTO ile eşleşiyor)
      const { userId, username, roles, token } = response.data; // <-- 'jwt' yerine 'token' kullanıldı

      // JWT Token'ı localStorage'a "token" adıyla kaydet (axiosInstance ile uyumlu)
      localStorage.setItem('token', token); // <-- 'jwt' yerine 'token' kullanıldı

      // Kullanıcıya ait diğer detayları (id, username, roller) da localStorage'a "user" adıyla kaydet (ProtectedRoutes ile uyumlu)
      const userDetails = { id: userId, username, roles }; // userId'yi 'id' olarak eşleştiriyoruz (frontend'de id bekleniyor olabilir)
      localStorage.setItem('user', JSON.stringify(userDetails)); // <-- Anahtar adı 'user' olarak düzeltildi

      console.log('JWT Token ve Kullanıcı Bilgileri localStorage\'a başarıyla kaydedildi.');
      console.log('Kaydedilen kullanıcı bilgileri (localStorage):', localStorage.getItem('user'));


      // 2. Başarılı Login Sonrası Sayfa Yönlendirmesi (Rol Tabanlı)
      // Kullanıcının rollerini localStorage'dan oku (az önce kaydettiğimiz bilgiyi kullanıyoruz)
      const storedUser = JSON.parse(localStorage.getItem('user')); // Kaydedilmiş kullanıcı bilgisini al
      const userRoles = storedUser ? storedUser.roles : []; // Roller listesini al, yoksa boş dizi kullan

      console.log("Login sonrası kullanıcı rolleri:", userRoles);

      // Rollere göre yönlendirme yap (Öncelik sırasına göre)
      // ADMIN -> /admin
      // TEACHER -> /teacher-dashboard (Bu rota henüz tanımlı değil ama mantığı kuruyoruz)
      // STUDENT -> /student-dashboard (Şimdi tanımladık!)
      // Default (rol yoksa veya beklenmedik rol) -> Anasayfa veya başka bir default sayfa

      if (userRoles.includes('ROLE_ADMIN')) {
        console.log("Login sonrası yönlendirme: ADMIN -> /admin");
        navigate('/admin'); // Admin dashboard'a yönlendir
      }
      // TODO: TEACHER rolü eklenince buraya kontrol eklenecek
      // else if (userRoles.includes('ROLE_TEACHER')) {
      //   console.log("Login sonrası yönlendirme: TEACHER -> /teacher-dashboard");
      //   navigate('/teacher-dashboard'); // Teacher dashboard'a yönlendir
      // }
      else if (userRoles.includes('ROLE_STUDENT')) {
        console.log("Login sonrası yönlendirme: STUDENT -> /student-dashboard");
        navigate('/student-dashboard'); // Student dashboard'a yönlendir
      }
       else {
           // Beklenmedik bir rol veya rol yoksa (olmaması gereken durum)
           console.warn("Login sonrası yönlendirme: Beklenmedik rol veya rol yok. Anasayfaya yönlendiriliyor.");
           navigate('/'); // Anasayfaya yönlendir
       }


        } catch (error) {
            // İstek sırasında bir hata olursa (örn: backend'den 401, 403, 500 gelirse veya ağ hatası)
            console.error('Login hatası:', error); // Hata objesinin tamamını logla

            // Hata yanıtının yapısını kontrol ederek kullanıcıya anlamlı bir mesaj gösterelim
            if (error.response) {
            // Backend'den bir HTTP yanıtı geldiyse (örn: 401 Unauthorized, 404 Not Found vb.)
            console.error('Hata yanıtı status:', error.response.status); // Hata status kodunu logla
            console.error('Hata yanıtı verisi:', error.response.data); // Backend'den gelen hata detayı (Genel olarak GenericErrorResponseDto objesi bekliyoruz)

            // Backend'den gelen hata verisi içinde 'message' alanı varsa onu kullan
            if (error.response.data && error.response.data.message) {
                setError(error.response.data.message); // Backend'in döndürdüğü mesajı error state'ine set et
            } else {
            // Backend'den özel bir hata mesajı gelmezse veya format farklıysa genel bir mesaj göster
            setError('Login Başarısız. Lütfen kullanıcı adı/email ve şifrenizi kontrol edin.');
            }

        } else if (error.request) {
            // İstek gönderildi ama backend'den yanıt alınamadı (örn: backend çalışmıyor, ağ bağlantısı yok)
            // error.request, XMLHttpRequest veya http.ClientRequest örneğidir
            setError('Sunucuya ulaşılamıyor. Lütfen backend uygulamasının çalıştığından emin olun.');
            } else {
            // Hata isteği ayarlarken veya başka bir şeyde oluştu
            setError('Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin veya yönetici ile iletişime geçin.');
            }
        } finally {
            // İşlem tamamlandığında (başarılı veya hatalı), loading durumunu sonlandır
            setIsLoading(false);
            }
        }
    
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


        <div style={{ marginBottom: '20px' }}> {/* Şifre input alanı için kapsayıcı */}
            <label htmlFor="password" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Şifre:</label> {/* Label stili eklendi */}
            <input
                type="password" // Şifre input alanı (girilen karakterleri gizler)
                id="password" // Label ile eşleşen ID
                name="password" // Form verisi için isim
                value={password} // Inputun güncel değeri state'ten alınır
            onChange={handlePasswordChange} // Input değeri değiştikçe handlePasswordChange çalışır ve state'i günceller
            required // Alanın doldurulması zorunlu
            disabled={isLoading} // İstek gönderilirken inputu devre dışı bırak
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }} 
            />
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
