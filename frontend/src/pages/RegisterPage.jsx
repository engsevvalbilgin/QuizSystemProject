// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\pages\RegisterPage.jsx
import React, { useState } from 'react';
import axiosInstance from '../api/axiosInstance'; // Axios instance'ımızı import et
// Gerekirse useNavigate import edilebilir ama şimdilik gerek yok, başarı mesajı göstereceğiz

function RegisterPage() {
  // Form alanları için state
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '', // Parola onayı için
    name: '',
    surname: '',
    age: '', // String olarak alıp sonra parse edeceğiz
    // role: 'STUDENT', // Şimdilik rolü hardcode edelim, sonra radio buton ekleriz
  });

  // Yüklenme, hata ve başarı mesajları için state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [formErrors, setFormErrors] = useState({}); // Client-side validasyon hataları için

  // Input değişikliklerini yönetme
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
    // Kullanıcı yazdıkça ilgili validasyon hatasını temizle
    if (formErrors[name]) {
      setFormErrors({
        ...formErrors,
        [name]: null,
      });
    }
    // Parola veya Parola Onayı değiştiğinde eşleşme hatasını da kontrol et
     if (name === 'password' || name === 'confirmPassword') {
         if (formErrors.confirmPassword && value === formData[name === 'password' ? 'confirmPassword' : 'password']) {
             setFormErrors({
                 ...formErrors,
                 confirmPassword: null,
             });
         }
     }
  };

  // Client-side validasyon fonksiyonu
  const validateForm = () => {
    const errors = {};
    if (!formData.username) errors.username = 'Kullanıcı adı zorunludur.';
    if (!formData.email) {
        errors.email = 'Email zorunludur.';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) { // Basit email format kontrolü
        errors.email = 'Geçerli bir email adresi girin.';
    }
    if (!formData.password) {
        errors.password = 'Şifre zorunludur.';
    } else if (formData.password.length < 6) { // Min şifre uzunluğu (backend ile uyumlu olmalı)
         errors.password = 'Şifre en az 6 karakter olmalıdır.';
    }
    if (!formData.confirmPassword) {
        errors.confirmPassword = 'Şifre onayı zorunludur.';
    } else if (formData.confirmPassword !== formData.password) {
        errors.confirmPassword = 'Şifreler eşleşmiyor.';
    }
     if (!formData.name) errors.name = 'İsim zorunludur.';
     if (!formData.surname) errors.surname = 'Soyisim zorunludur.';
     if (!formData.age) {
         errors.age = 'Yaş zorunludur.';
     } else if (isNaN(formData.age) || parseInt(formData.age) <= 0) { // Yaşın sayı ve pozitif olduğunu kontrol et
          errors.age = 'Geçerli bir yaş girin.';
     }
     // Role şimdilik hardcode olduğu için valide etmeye gerek yok

    setFormErrors(errors);
    return Object.keys(errors).length === 0; // Hata yoksa true döner
  };


  // Form gönderildiğinde çalışacak fonksiyon
  const handleSubmit = async (e) => {
    e.preventDefault(); // Sayfanın yeniden yüklenmesini engelle

    setError(null); // Önceki hataları temizle
    setSuccessMessage(null); // Önceki başarı mesajlarını temizle

    // Client-side validasyonu çalıştır
    const isValid = validateForm();
    if (!isValid) {
      console.log("Client-side validasyon hatası");
      return; // Validasyon başarısızsa gönderme
    }

    setLoading(true); // Yüklenme durumunu başlat

    try {
      // Backend'in beklediği UserRegistrationRequest DTO formatına uygun veriyi hazırla
      const requestData = {
        username: formData.username,
        email: formData.email,
        password: formData.password, // Şifre zaten backend'de şifrelenecek
        name: formData.name,
        surname: formData.surname,
        age: parseInt(formData.age), // Yaşı int'e çevir
        // role: 'STUDENT', // Şimdilik rol backend'de atanıyor ama DTO'da varsa gönderebiliriz
      };
      // Backend DTO'nuzda 'role' alanı varsa, yukarıdaki requestData'ya bunu ekleyin.
      // Backend RegisterRequest DTO'sunu kontrol edin. AuthenticationService'te ROLE_STUDENT hardcode edilmişti.
      // Eğer DTO'da role varsa, bu satırı ekleyin: requestData.role = formData.role;

      console.log("Kayıt isteği gönderiliyor:", requestData);

      // POST isteği yap - axiosInstance token eklemeyecek çünkü login değil kayıt
      const response = await axiosInstance.post('/auth/register', requestData); // Backend kayıt endpoint'i

      // Başarılı kayıt
      console.log("Kayıt başarılı:", response.data);
      setSuccessMessage(response.data.message || "Kayıt başarılı! Lütfen email adresinizi kontrol edin."); // Backend mesajını kullan veya varsayılan göster
      // İsterseniz formu temizleyebilirsiniz: setFormData({ ... });
      // İsterseniz login sayfasına yönlendirebilirsiniz: navigate('/login');

    } catch (err) {
      console.error("Kayıt hatası:", err);
      // Hata mesajını kullanıcıya göster
      if (err.response) {
        // Backend'den bir hata yanıtı geldiyse (örn: 400, 409)
         if (err.response.data && err.response.data.message) {
             // Backend'in döndürdüğü hata mesajını göster
             setError(err.response.data.message);
         } else if (err.response.data) {
              // Backend'den gelen diğer hata detayları
             setError(JSON.stringify(err.response.data)); // Tüm hata objesini stringe çevirip göster
         }
         else {
            // Sadece status kodu varsa
             setError(`Kayıt hatası. Status: ${err.response.status}`);
         }

      } else if (err.request) {
        // İstek yapıldı ama yanıt alınamadı (örn: sunucu çalışmıyor)
        setError("Sunucuya ulaşılamadı. Lütfen daha sonra tekrar deneyin.");
      } else {
        // Başka bir hata oluştu
        setError("Beklenmeyen bir hata oluştu.");
      }
    } finally {
      setLoading(false); // Yüklenme durumunu sonlandır
    }
  };

  // --- Komponentin Render Ettiği JSX ---
  return (
    <div style={{ maxWidth: '500px', margin: '20px auto', padding: '20px', border: '1px solid #ddd', borderRadius: '8px' }}>
      <h2>Kaydol</h2>

      {/* Başarı Mesajı */}
      {successMessage && <p style={{ color: 'green' }}>{successMessage}</p>}

      {/* Hata Mesajı */}
      {error && <p style={{ color: 'red' }}>{error}</p>}

      {/* Kayıt Formu */}
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="username">Kullanıcı Adı:</label>
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleInputChange}
            disabled={loading} // Yüklenirken inputları devre dışı bırak
          />
          {/* Validasyon hatası göster */}
          {formErrors.username && <p style={{ color: 'red', fontSize: '0.8em', marginTop: '5px' }}>{formErrors.username}</p>}
        </div>

         <div>
          <label htmlFor="name">İsim:</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            disabled={loading}
          />
           {formErrors.name && <p style={{ color: 'red', fontSize: '0.8em', marginTop: '5px' }}>{formErrors.name}</p>}
        </div>

         <div>
          <label htmlFor="surname">Soyisim:</label>
          <input
            type="text"
            id="surname"
            name="surname"
            value={formData.surname}
            onChange={handleInputChange}
            disabled={loading}
          />
           {formErrors.surname && <p style={{ color: 'red', fontSize: '0.8em', marginTop: '5px' }}>{formErrors.surname}</p>}
        </div>

         <div>
          <label htmlFor="age">Yaş:</label>
          <input
            type="number" // Yaş için number tipi
            id="age"
            name="age"
            value={formData.age}
            onChange={handleInputChange}
            disabled={loading}
            min="1" // Yaş en az 1 olabilir
          />
           {formErrors.age && <p style={{ color: 'red', fontSize: '0.8em', marginTop: '5px' }}>{formErrors.age}</p>}
        </div>


        <div>
          <label htmlFor="email">Email:</label>
          <input
            type="email" // Email için email tipi
            id="email"
            name="email"
            value={formData.email}
            onChange={handleInputChange}
            disabled={loading}
          />
          {formErrors.email && <p style={{ color: 'red', fontSize: '0.8em', marginTop: '5px' }}>{formErrors.email}</p>}
        </div>

        <div>
          <label htmlFor="password">Şifre:</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleInputChange}
            disabled={loading}
          />
          {formErrors.password && <p style={{ color: 'red', fontSize: '0.8em', marginTop: '5px' }}>{formErrors.password}</p>}
        </div>

        <div>
          <label htmlFor="confirmPassword">Şifre Onayı:</label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleInputChange}
            disabled={loading}
          />
          {formErrors.confirmPassword && <p style={{ color: 'red', fontSize: '0.8em', marginTop: '5px' }}>{formErrors.confirmPassword}</p>}
        </div>

        {/* TODO: Rol seçimi için radio butonlar veya select box buraya eklenecek (Öğrenci/Öğretmen) */}
        {/* Şimdilik sadece Öğrenci kaydedildiği için bu kısım eksik */}

        <button type="submit" disabled={loading}>
          {loading ? 'Kaydediliyor...' : 'Kaydol'}
        </button>
      </form>
    </div>
  );
}

export default RegisterPage; // Komponenti dışarıya aktarıyoruz