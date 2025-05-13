// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\App.jsx
import React from 'react';
import { Routes, Route } from 'react-router-dom';

// Layout Komponentlerini Import Et
import MainLayout from './components/MainLayout';
import ProtectedRoutes from './components/ProtectedRoutes'; // Korumalı Rota
import AdminLayout from './components/AdminLayout'; // AdminLayout

// Sayfa Komponentlerini Import Et
import MainScreen from './pages/MainScreen.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import VerifyEmailPage from './pages/VerifyEmailPage.jsx'; // Email Doğrulama Sayfası
import AdminDashboardPage from './pages/AdminDashboardPage.jsx'; // Admin Dashboard Sayfası
import AdminUserListPage from './pages/AdminUserListPage.jsx'; // Kullanıcı listeleme sayfası
import StudentPanel from './pages/StudentPanel.jsx'; // StudentPanel importu

// axiosInstance importu buradan KALDIRILDI. App.jsx'in buna ihtiyacı yok.


function App() {
  return (
    <div className="App">
      {/* Tüm rotaları ve sayfaları sarmalayacak Ana Layout */}
      <Routes>
        <Route path="/" element={<MainLayout />} > {/* Ana Layout Rotası */}

             {/* --- Herkese Açık Rotalar (Layout içinde) --- */}
            <Route index element={<MainScreen />} /> {/* Anasayfa (index) rotası */}
            <Route path="login" element={<LoginPage />} /> {/* Login sayfası rotası */}
            <Route path="register" element={<RegisterPage />} /> {/* Kayıt sayfası rotası */}
            <Route path="verify-email" element={<VerifyEmailPage />} /> {/* Email Doğrulama rotası */}


             {/* --- Korumalı Rotalar Grubu (Layout içinde) --- */}
            {/* Bu ProtectedRoutes ADMIN ve STUDENT rolleri için kullanılabilir, veya ayrı ayrı tanımlanabilir */}
            {/* ADMIN rotalarını ayrı bir ProtectedRoutes sarmalına almak daha temiz olabilir */}

             {/* ADMIN Rotaları (Sadece ADMIN rolüne sahip kullanıcılar) */}
            <Route element={<ProtectedRoutes requiredRoles={['ROLE_ADMIN']} />} > {/* ADMIN Korumalı Rotalar için kapsayıcı */}
                 {/* Admin Paneli Rotası */}
                <Route path="admin" element={<AdminLayout />} > {/* Admin Paneli Layout Rotası */}
                    {/* /admin'in varsayılan alt rotası */}
                   <Route index element={<AdminDashboardPage />} /> {/* Admin Dashboard Sayfası */}
                   {/* Kullanıcı Listeleme Sayfası Rotası */}
                    <Route path="users" element={<AdminUserListPage />} /> {/* Kullanıcı listeleme sayfası */}
                </Route>
            </Route>

             {/* STUDENT Rotaları (Sadece STUDENT rolüne sahip kullanıcılar) */}
            <Route element={<ProtectedRoutes requiredRoles={['ROLE_STUDENT']} />} > {/* STUDENT Korumalı Rotalar için kapsayıcı */}
                 {/* Student Paneli Rotası */}
                 <Route path="student-dashboard" element={<StudentPanel />} /> {/* Student Paneli rotası */}
                 {/* TODO: Öğrencilere özel diğer rotalar buraya eklenecek */}
                 {/* <Route path="quizzes" element={<StudentQuizListPage />} /> */}
                 {/* <Route path="results" element={<StudentResultsPage />} /> */}
            </Route>

             {/* TODO: TEACHER paneli rotaları buraya eklenecek (Kendi ProtectedRoutes sarmalında veya mevcut STUDENT sarmalında) */}
             {/* TEACHER Rotaları (Sadece TEACHER rolüne sahip kullanıcılar) */}
             {/* <Route element={<ProtectedRoutes requiredRoles={['ROLE_TEACHER']} />} >
                   <Route path="teacher-dashboard" element={<TeacherPanel />} />
                 </Route> */}

             {/* Login olmuş herhangi bir kullanıcının erişebileceği rotalar (örn: Profil Sayfası) */}
             {/* <Route element={<ProtectedRoutes requiredRoles={['ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT']} />} >
                   <Route path="profile" element={<ProfilePage />} />
                 </Route> */}


            {/* Eşleşmeyen tüm yollar için 404 sayfası (Layout içinde) */}
            {/* <Route path="*" element={<NotFoundPage />} /> */}

        </Route> {/* Ana Layout Rotası Sonu */}


        {/* Layout dışında render edilecek tam sayfa rotaları buraya */}

      </Routes>
    </div>
  );
}

export default App;
