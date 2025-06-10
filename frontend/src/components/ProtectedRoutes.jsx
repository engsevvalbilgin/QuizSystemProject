import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoutes = ({ requiredRoles }) => {
  const token = localStorage.getItem('token');
  const userString = localStorage.getItem('user'); 
  const user = userString ? JSON.parse(userString) : null; 

  console.log("ProtectedRoutes: Kontrol ediliyor...");
  console.log("ProtectedRoutes: Okunan Token:", token);
  console.log("ProtectedRoutes: Okunan Kullanıcı (string):", userString);
  console.log("ProtectedRoutes: Okunan Kullanıcı (parsed):", user);
  console.log("ProtectedRoutes: Gerekli Roller:", requiredRoles);


  if (!token) {
    console.log("ProtectedRoutes: Token bulunamadı, erişim reddedildi.");
    return null; 
  }

  if (!user || !user.roles || !Array.isArray(user.roles)) {
      console.log("ProtectedRoutes: Kullanıcı bilgileri veya roller eksik/geçersiz, erişim reddedildi.");
       
       localStorage.removeItem('token');
       localStorage.removeItem('user');
      return null; 
  }

  const hasRequiredRole = requiredRoles.some(role => user.roles.includes(role));

  console.log("ProtectedRoutes: Kullanıcı Rolleri:", user.roles);
  console.log("ProtectedRoutes: Gerekli Rollerden biri var mı?", hasRequiredRole);


  if (!hasRequiredRole) {
    console.log("ProtectedRoutes: Gerekli rol bulunamadı, anasayfaya yönlendiriliyor.");
    
    return <Navigate to="/" replace />;
  }

  console.log("ProtectedRoutes: Erişim izni verildi. Çocuk rotalar render ediliyor.");
  return <Outlet />;
};

export default ProtectedRoutes;
