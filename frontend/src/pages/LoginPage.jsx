import React, { useState } from 'react';
import axiosInstance from '../api/axiosInstance';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; 

function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth(); 


 
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
 
  const [password, setPassword] = useState('');
  
  const [showPassword, setShowPassword] = useState(false);
 
  const [error, setError] = useState('');
 
  const [isLoading, setIsLoading] = useState(false);
 
  const handleUsernameOrEmailChange = (event) => {
    setUsernameOrEmail(event.target.value); 
  };
  const handlePasswordChange = (event) => {
    setPassword(event.target.value); 
  };

 
  const handleSubmit = async (event) => {
    event.preventDefault(); 

    setError(''); 
    setIsLoading(true); 

    console.log('Login Denemesi:', { usernameOrEmail, password });

    try {
      const response = await axiosInstance.post('/auth/login', {
        usernameOrEmail: usernameOrEmail,
        password: password
      });

      console.log('Login başarılı:', response.data);

      const { userId, username, roles, token, refreshToken } = response.data;
      
      if (!token) {
        throw new Error('Invalid response from server: Missing token');
      }

      console.log('Token ve kullanıcı bilgileri kaydedildi');
      
      const userDetails = { 
        id: userId, 
        username, 
        roles,
      };
      
      localStorage.setItem('user', JSON.stringify(userDetails));
      console.log('Kullanıcı bilgileri kaydedildi:', userDetails);

      
      login(token, refreshToken, userDetails);

      
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
      
      let errorMessage = 'Giriş başarısız. Lütfen bilgilerinizi kontrol edin.';
      
      if (error.response) {
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
        console.error('Sunucudan yanıt alınamadı:', error.request);
        errorMessage = 'Sunucuya bağlanılamıyor. Lütfen internet bağlantınızı kontrol edin.';
      } else {
        console.error('İstek oluşturulurken hata:', error.message);
        errorMessage = 'İstek gönderilirken bir hata oluştu.';
      }
      
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  
  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px', border: '1px solid #ddd', borderRadius: '8px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}> 
            <h2>Giriş Yap</h2>

            
            {error && <p style={{ color: 'red', marginBottom: '15px' }}>{error}</p>}

            
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '15px' }}>
                    <label htmlFor="usernameOrEmail" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Kullanıcı Adı / Email:</label> 
                    
                    <input
                        type="text" 
                id="usernameOrEmail" 
                name="usernameOrEmail" 
                value={usernameOrEmail} 
                onChange={handleUsernameOrEmailChange} 
                required 
                disabled={isLoading} 
                style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }} 
            />
        </div>


        <div style={{ marginBottom: '10px', position: 'relative' }}> 
            <label htmlFor="password" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Şifre:</label> 
            <div style={{ display: 'flex', alignItems: 'center' }}>
                <input
                    type={showPassword ? 'text' : 'password'} 
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
                        marginRight: '30px' 
                    }}
                />
                
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

            <button type="submit" disabled={isLoading} style={{ width: '100%', padding: '10px', backgroundColor: '#5cb85c', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '1em', transition: 'background-color 0.3s ease' }}>
                {isLoading ? 'Giriş Yapılıyor...' : 'Giriş Yap'}
            </button>
        </form>
    </div>
);
}

export default LoginPage; 
