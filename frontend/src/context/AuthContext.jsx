// AuthContext.jsx
import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import axiosInstance, { setupAxiosInterceptors } from '../api/axiosInstance'; // axiosInstance ve setupAxiosInterceptors'ı import edin

// Authentication Context'ini oluştur
const AuthContext = createContext(null);

// AuthProvider Komponenti: Uygulama ağacını sarmalar ve kimlik doğrulama durumunu sağlar
export const AuthProvider = ({ children }) => {
    // localStorage'dan başlangıç değerlerini oku
    // Sayfa yenilendiğinde veya uygulama ilk yüklendiğinde kullanıcıyı login kalmış gibi başlatmak için
    const initialToken = localStorage.getItem('token');
    const initialUserString = localStorage.getItem('user');
    const initialUser = initialUserString ? JSON.parse(initialUserString) : null;

    console.log("AuthContext (Initial Load): localStorage token:", initialToken ? 'Mevcut' : 'Yok', "localStorage user:", initialUser ? initialUser.username : 'Yok');

    // Kimlik doğrulama durumu için state
    const [token, setToken] = useState(initialToken);
    const [user, setUser] = useState(initialUser);

    // Token veya kullanıcı bilgisi değiştiğinde localStorage'ı güncelle
    // Bu useEffect, sadece başlangıç yüklemesi ve logout durumları için çalışacak
    useEffect(() => {
        if (token && user) {
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(user));
            console.log("AuthContext: useEffect - Token ve user bilgileri localStorage'a yazıldı");
        } else {
            // Eğer token veya user yoksa, localStorage'ı temizle
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            console.log("AuthContext: useEffect - Token veya user yok, localStorage temizlendi.");
        }
    }, [token, user]); // token veya user state'i değiştiğinde çalışır

    // Login fonksiyonu: Token ve kullanıcı bilgilerini alır, state'i ve localStorage'ı günceller
    const login = useCallback((newToken, newUser) => {
        if (!newToken || !newUser) {
            console.error('AuthContext: Login çağrısı için hem token hem user gereklidir');
            return;
        }

        localStorage.setItem('token', newToken);
        localStorage.setItem('user', JSON.stringify(newUser));

        setToken(newToken);
        setUser(newUser);

        console.log('AuthContext: Login başarılı. Token ve user bilgileri güncellendi');
    }, []); // Bağımlılık dizisi boş çünkü dışarıdan bir şeye bağlı değil.

    // Logout fonksiyonu: State'i ve localStorage'ı temizler
    const logout = useCallback(() => {
        setToken(null);
        setUser(null);
        // useEffect bu temizleme işlemini zaten yapacak, ancak anında etki için buraya da ekleyebiliriz.
        // localStorage.removeItem('token');
        // localStorage.removeItem('user');
        console.log("AuthContext: Logout işlemi yapıldı.");
    }, []); // Bağımlılık dizisi boş.

    // Token yenileme fonksiyonu
    const refreshToken = useCallback(async () => {
        try {
            // axiosInstance'ı kullanarak token yenileme isteği gönderin
            // Bu istek, Authorization header'ı olmadan gitmelidir, çünkü token zaten geçersiz olabilir.
            // Bu durumda, axiosInstance'ın kendisi de token'ı eklemeye çalışabilir.
            // Eğer refresh token'ı ayrı bir endpoint'e gönderiyorsanız ve o endpoint yetkilendirme gerektirmiyorsa,
            // doğrudan `axios` kullanmak daha güvenli olabilir. Ancak mevcut setup'a göre axiosInstance kullanılıyor.
            // Eğer refresh-token endpoint'i de token gerektiriyorsa ve eski token geçersizse sorun yaşanır.
            // Genellikle refresh token endpoint'leri özel bir refresh token ile çalışır ve access token gerektirmez.
            const response = await axiosInstance.post('/auth/refresh-token', {
                refreshToken: localStorage.getItem('refreshToken') // Eğer refresh token'ınız varsa gönderin
            });

            if (response.status === 200) {
                const newToken = response.data.token;
                const newUser = response.data.user; // Eğer refresh ile user bilgisi de geliyorsa
                setToken(newToken);
                setUser(newUser || user); // Eğer user bilgisi gelmezse mevcut user'ı koru
                localStorage.setItem('token', newToken);
                if (newUser) {
                    localStorage.setItem('user', JSON.stringify(newUser));
                }
                console.log("AuthContext: Token refresh başarılı. Yeni token ve user bilgileri güncellendi.");
                return true;
            }
            console.log("AuthContext: Token refresh başarısız (status:", response.status, ")");
            return false;
        } catch (error) {
            console.error('AuthContext: Token refresh hatası:', error);
            // Hata durumunda token'ı temizle ve false döndür
            setToken(null);
            setUser(null);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            return false;
        }
    }, [user]); // user state'ine bağlı olabilir, eğer refresh sonrası user güncelleniyorsa.

    // Token geçerliliğini kontrol et (isteğe bağlı, backend'e bağlı)
    const validateToken = useCallback(async () => {
        try {
            const currentToken = localStorage.getItem('token');
            if (!currentToken) {
                console.log("AuthContext: validateToken - localStorage'da token yok.");
                return false;
            }

            // Token'ı doğrula - backend'den doğrulama yapabiliriz
            // axiosInstance zaten token'ı header'a ekleyecektir
            const response = await axiosInstance.get('/api/auth/validate-token');
            if (response.status === 200) {
                console.log("AuthContext: validateToken - Token geçerli.");
                return true;
            }
            console.log("AuthContext: validateToken - Token geçerli değil (status:", response.status, ")");
            return false;
        } catch (error) {
            console.error('AuthContext: Token doğrulama hatası:', error);
            // Hata durumunda token'ı temizle
            setToken(null);
            setUser(null);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            return false;
        }
    }, []); // Bağımlılık dizisi boş.

    // Axios interceptor'larını başlatmak için useEffect kullanın
    // Bu, AuthProvider yüklendiğinde bir kez çalışır ve refreshToken/logout fonksiyonlarını interceptor'lara sağlar.
    useEffect(() => {
        console.log("AuthContext: Axios interceptor'ları kuruluyor...");
        setupAxiosInterceptors(refreshToken, logout);
        // Clean-up fonksiyonu (isteğe bağlı):
        // return () => { /* Interceptor'ları temizleme kodu buraya gelebilir */ };
    }, [refreshToken, logout]); // refreshToken ve logout fonksiyonları değiştiğinde tekrar kur.

    // Context değerini sağla
    const contextValue = {
        token, // JWT token
        user, // Kullanıcı bilgileri objesi { id, username, roles }
        login, // Login fonksiyonu
        logout, // Logout fonksiyonu
        refreshToken, // Token refresh fonksiyonu
        validateToken, // Token doğrulama fonksiyonu
        isAuthenticated: !!token && !!user, // Kullanıcının login olup olmadığını kontrol et
        isAdmin: user && user.roles && user.roles.includes('ROLE_ADMIN'), // Admin rolü var mı?
        isStudent: user && user.roles && user.roles.includes('ROLE_STUDENT'), // Student rolü var mı?
        isTeacher: user && user.roles && user.roles.includes('ROLE_TEACHER'), // Teacher rolü var mı?
    };

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
};

// Context'i kullanmak için custom hook
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export default AuthContext; // Context objesini de dışa aktar
