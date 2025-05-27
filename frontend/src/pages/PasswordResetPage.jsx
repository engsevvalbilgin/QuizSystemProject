import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import '../styles/AuthStyles.css';

function PasswordResetPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const [step, setStep] = useState(1); // 1: Email form, 2: Reset form
    const [email, setEmail] = useState('');
    const [token, setToken] = useState('');
    const [passwords, setPasswords] = useState({
        newPassword: '',
        confirmPassword: ''
    });
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [loading, setLoading] = useState(false);

    // Extract token from URL if present
    React.useEffect(() => {
        const params = new URLSearchParams(location.search);
        const tokenParam = params.get('token');
        
        console.log("PasswordResetPage: URL path:", location.pathname);
        console.log("PasswordResetPage: URL'den token alındı:", tokenParam);
        console.log("PasswordResetPage: Full URL:", window.location.href);
        
        if (tokenParam) {
            setToken(tokenParam);
            setStep(2); // Move to reset form if token is in URL
            console.log("PasswordResetPage: Token bulundu, adım 2'ye geçildi (şifre sıfırlama formu)");
        } else {
            console.log("PasswordResetPage: Token bulunamadı, adım 1'de kalındı (email formu)");
        }
    }, [location]);

    const handleRequestReset = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setLoading(true);
        
        if (!email) {
            setError('Lütfen e-posta adresinizi girin.');
            setLoading(false);
            return;
        }
        
        try {
            console.log("PasswordResetPage: Şifre sıfırlama isteği gönderiliyor...");
            // Remove duplicate /api prefix - axiosInstance already has baseURL configured
            const response = await axiosInstance.post('/users/password-reset/request', { email });
            console.log("PasswordResetPage: Şifre sıfırlama isteği başarılı:", response.data);
            setSuccess('E-posta adresinize şifre sıfırlama talimatları gönderildi. Lütfen e-postanızı kontrol edin.');
        } catch (err) {
            console.error('Şifre sıfırlama isteği hatası:', err);
            // Always show the same message to prevent email enumeration
            setSuccess('E-posta adresinize şifre sıfırlama talimatları gönderildi. Lütfen e-postanızı kontrol edin.');
        } finally {
            setLoading(false);
        }
    };
    
    const handleResetPassword = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setLoading(true);
        
        if (!token) {
            setError('Geçersiz veya eksik token. Lütfen e-postanızdaki bağlantıyı kullanın.');
            setLoading(false);
            return;
        }
        
        if (!passwords.newPassword || !passwords.confirmPassword) {
            setError('Lütfen tüm alanları doldurun.');
            setLoading(false);
            return;
        }
        
        if (passwords.newPassword !== passwords.confirmPassword) {
            setError('Şifreler eşleşmiyor.');
            setLoading(false);
            return;
        }
        
        if (passwords.newPassword.length < 6) {
            setError('Şifre en az 6 karakter olmalıdır.');
            setLoading(false);
            return;
        }
        
        try {
            console.log("PasswordResetPage: Şifre sıfırlama tamamlama isteği gönderiliyor...");
            console.log("PasswordResetPage: Kullanılan token:", token);
            
            // Add additional debug for token
            console.log("PasswordResetPage: Token uzunluk:", token ? token.length : 0);
            
            // Double check that token is properly formatted
            if (!token || token.length < 10) {
                throw new Error("Token geçersiz veya eksik. Doğru token değerinin kullanıldığından emin olun.");
            }
            
            // Ensure we use the correct API endpoint path without duplicate /api prefix
            const response = await axiosInstance.post('/users/password-reset/complete', {
                token: token,
                newPassword: passwords.newPassword,
                confirmPassword: passwords.confirmPassword
            });
            
            // Log entire response for debugging
            console.log("PasswordResetPage: Server yanıtı tam:", response);
            
            console.log("PasswordResetPage: Şifre sıfırlama başarılı:", response.data);
            
            setSuccess('Şifreniz başarıyla sıfırlandı. Şimdi giriş yapabilirsiniz.');
            
            // Kullanıcıyı 3 saniye sonra giriş sayfasına yönlendir
            setTimeout(() => {
                navigate('/login');
            }, 3000);
            
        } catch (err) {
            console.error('Şifre sıfırlama hatası:', err);
            if (err.response?.status === 400) {
                setError('Geçersiz veya süresi dolmuş token. Lütfen yeni bir şifre sıfırlama isteği gönderin.');
            } else {
                setError(err.response?.data || 'Şifre sıfırlama sırasında bir hata oluştu.');
            }
        } finally {
            setLoading(false);
        }
    };
    
    const handlePasswordChange = (e) => {
        setPasswords({
            ...passwords,
            [e.target.name]: e.target.value
        });
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2>Şifre Sıfırlama</h2>
                <p className="auth-subheader">{step === 1 ? 'Şifrenizi sıfırlamak için e-posta adresinizi girin' : 'Yeni şifrenizi belirleyin'}</p>
                
                {error && <div className="error-message">{error}</div>}
                {success && <div className="success-message">{success}</div>}
                
                {step === 1 ? (
                    <form onSubmit={handleRequestReset}>
                        <div className="form-group">
                            <label htmlFor="email">E-posta Adresiniz</label>
                            <input
                                type="email"
                                id="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="E-posta adresinizi girin"
                                required
                            />
                        </div>
                        
                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? 'Gönderiliyor...' : 'Şifre Sıfırlama Bağlantısı Gönder'}
                        </button>
                        
                        <div className="auth-links">
                            <a onClick={() => navigate('/login')} style={{cursor: 'pointer'}}>Giriş sayfasına dön</a>
                        </div>
                    </form>
                ) : (
                    <form onSubmit={handleResetPassword}>
                        <div className="form-group">
                            <label htmlFor="newPassword">Yeni Şifre</label>
                            <input
                                type="password"
                                id="newPassword"
                                name="newPassword"
                                value={passwords.newPassword}
                                onChange={handlePasswordChange}
                                placeholder="Yeni şifrenizi girin"
                                required
                            />
                        </div>
                        
                        <div className="form-group">
                            <label htmlFor="confirmPassword">Şifre Tekrar</label>
                            <input
                                type="password"
                                id="confirmPassword"
                                name="confirmPassword"
                                value={passwords.confirmPassword}
                                onChange={handlePasswordChange}
                                placeholder="Yeni şifrenizi tekrar girin"
                                required
                            />
                        </div>
                        
                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? 'İşlem Yapılıyor...' : 'Şifremi Sıfırla'}
                        </button>
                        
                        <div className="auth-links">
                            <a onClick={() => navigate('/login')} style={{cursor: 'pointer'}}>Giriş sayfasına dön</a>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
}

export default PasswordResetPage;
