// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\components\ProtectedRoutes.jsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoutes = ({ requiredRoles }) => {
  // Read token and user from localStorage
  const token = localStorage.getItem('token');
  const userString = localStorage.getItem('user'); // String olarak oku
  const user = userString ? JSON.parse(userString) : null; // Parse et

  console.log("ProtectedRoutes: Kontrol ediliyor...");
  console.log("ProtectedRoutes: Okunan Token:", token);
  console.log("ProtectedRoutes: Okunan Kullanıcı (string):", userString);
  console.log("ProtectedRoutes: Okunan Kullanıcı (parsed):", user);
  console.log("ProtectedRoutes: Gerekli Roller:", requiredRoles);


  // Check if token exists
  if (!token) {
    console.log("ProtectedRoutes: Token bulunamadı, erişim reddedildi.");
    return null; // Do not render anything
  }

  // Check if user data and roles exist
  if (!user || !user.roles || !Array.isArray(user.roles)) {
      console.log("ProtectedRoutes: Kullanıcı bilgileri veya roller eksik/geçersiz, erişim reddedildi.");
       // Clear potentially incomplete data
       localStorage.removeItem('token');
       localStorage.removeItem('user');
      return null; // Do not render anything
  }

  // Check if user has at least one of the required roles
  const hasRequiredRole = requiredRoles.some(role => user.roles.includes(role));

  console.log("ProtectedRoutes: Kullanıcı Rolleri:", user.roles);
  console.log("ProtectedRoutes: Gerekli Rollerden biri var mı?", hasRequiredRole);


  if (!hasRequiredRole) {
    console.log("ProtectedRoutes: Gerekli rol bulunamadı, anasayfaya yönlendiriliyor.");
    // TODO: Belki yetkisiz erişim sayfası göstermek daha iyi olabilir
    return <Navigate to="/" replace />;
  }

  // If token exists and user has required role, render the child routes
  console.log("ProtectedRoutes: Erişim izni verildi. Çocuk rotalar render ediliyor.");
  return <Outlet />;
};

export default ProtectedRoutes;
