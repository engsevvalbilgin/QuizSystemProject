// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\context\AuthContext.jsx
import React, { createContext, useState, useContext, useEffect } from 'react';

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
  // Bu useEffect, state değişikliklerini localStorage ile senkronize eder
  // Bu useEffect artık sadece başlangıç yüklemesi ve logout durumları için bir fallback görevi görecek.
  // Login sırasında token'ın localStorage'a yazılması login fonksiyonu içinde senkron olarak yapılacak.
  useEffect(() => {
    console.log("AuthContext: useEffect çalıştı.");
    console.log("AuthContext: useEffect anında Token state:", token ? 'Mevcut' : 'Yok', "User state:", user ? user.username : 'Yok');
    console.log("AuthContext: useEffect anında localStorage token:", localStorage.getItem('token') ? 'Mevcut' : 'Yok', "localStorage user:", localStorage.getItem('user') ? 'Mevcut' : 'Yok');


    // Eğer token state'i null ise (logout durumu veya başlangıçta yoksa) localStorage'ı temizle
    if (!token) {
       localStorage.removeItem('token');
       console.log("AuthContext: useEffect - Token state null, localStorage token temizlendi.");
    } else {
        // Token state'i mevcutsa, localStorage'da da olduğundan emin ol (login fonksiyonu zaten yazmalı)
        if (!localStorage.getItem('token')) {
             localStorage.setItem('token', token);
             console.log("AuthContext: useEffect - Token state mevcut ama localStorage boş, localStorage'a yazıldı.");
        }
    }

    // Eğer user state'i null ise (logout durumu veya başlangıçta yoksa) localStorage'ı temizle
    if (!user) {
       localStorage.removeItem('user');
       console.log("AuthContext: useEffect - User state null, localStorage user temizlendi.");
    } else {
         // User state'i mevcutsa, localStorage'da da olduğundan emin ol (login fonksiyonu zaten yazmalı)
         if (!localStorage.getItem('user')) {
             localStorage.setItem('user', JSON.stringify(user));
             console.log("AuthContext: useEffect - User state mevcut ama localStorage boş, localStorage'a yazıldı.");
         }
    }

     console.log("AuthContext: localStorage güncellendi (useEffect sonu). Token:", localStorage.getItem('token') ? 'Mevcut' : 'Yok', "Kullanıcı:", localStorage.getItem('user') ? 'Mevcut' : 'Yok');

  }, [token, user]); // token veya user state'i değiştiğinde çalışır

  // Login fonksiyonu: Token ve kullanıcı bilgilerini alır, state'i ve localStorage'ı günceller
  const login = (newToken, newUser) => {
    console.log("AuthContext: login fonksiyonu başladı. Gelen newToken:", newToken ? 'Mevcut' : 'Yok', "Gelen newUser:", newUser ? newUser.username : 'Yok');

    // Token'ı localStorage'a senkron olarak hemen kaydet
    if (newToken) {
        localStorage.setItem('token', newToken); // <-- Token localStorage'a hemen yazıldı
        console.log("AuthContext: login fonksiyonu - Token localStorage'a senkron olarak yazıldı.");
    } else {
        localStorage.removeItem('token');
        console.log("AuthContext: login fonksiyonu - Token localStorage'dan temizlendi (login null token).");
    }

     // User'ı localStorage'a senkron olarak hemen kaydet
     if (newUser) {
         localStorage.setItem('user', JSON.stringify(newUser)); // <-- User localStorage'a hemen yazıldı
         console.log("AuthContext: login fonksiyonu - User localStorage'a senkron olarak yazıldı.");
     } else {
         localStorage.removeItem('user');
         console.log("AuthContext: login fonksiyonu - User localStorage'dan temizlendi (login null user).");
     }


    // State'leri güncelle (asenkrondur)
    setToken(newToken);
    setUser(newUser);

    console.log("AuthContext: login fonksiyonu bitti. setToken ve setUser çağrıldı.");
  };

  // Logout fonksiyonu: State'i ve localStorage'ı temizler
  const logout = () => {
    setToken(null);
    setUser(null);
    // localStorage zaten useEffect içinde temizlenecek, ama burada da açıkça yapabiliriz
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    console.log("AuthContext: Logout işlemi yapıldı.");
  };

  // Context değerini sağla
  const contextValue = {
    token, // JWT token
    user, // Kullanıcı bilgileri objesi { id, username, roles }
    login, // Login fonksiyonu
    logout, // Logout fonksiyonu
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
