// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\components\AdminLayout.jsx
import React from 'react';
import { Link, Outlet } from 'react-router-dom'; // Navigasyon linkleri ve alt rotaları render etmek için

function AdminLayout() {
  return (
    <div className="admin-layout"> {/* CSS için ana layout class'ı */}
      {/* Sol Menü (Sidebar) */}
      <aside className="admin-sidebar"> {/* CSS için sidebar class'ı */}
        <h3>Admin Menü</h3>
        <nav>
          <ul>
            <li>
              {/* Link to="/admin" dediğimizde App.jsx'te /admin yoluna karşılık gelen alt rotayı arar. */}
              {/* App.jsx'te path="admin" element={<AdminLayout />} altına index element={<AdminDashboardPage />} ekleyeceğiz, yani /admin varsayılan olarak AdminDashboardPage'i render edecek. */}
              <Link to="/admin">Dashboard</Link> {/* <-- Dashboard linki */}
            </li>
            <li>
              {/* Link to="/admin/users" dediğimizde App.jsx'te /admin/users yoluna karşılık gelen alt rotayı arar. */}
              {/* App.jsx'te path="admin" element={<AdminLayout />} altına path="users" element={<AdminUserListPage />} ekleyeceğiz. */}
              <Link to="/admin/users">Kullanıcıları Listele</Link> {/* <-- Kullanıcı Listesi linki */}
            </li>
            <li>
              <Link to="/admin/teacher-requests">Öğretmen Başvuruları</Link>
            </li>
            <li>
              <Link to="/admin/announcements">Duyurular</Link>
            </li>
            
            {/* TODO: Diğer Admin fonksiyonları için linkler buraya eklenecek (örn: Quiz Yönetimi, İstatistikler) */}
          </ul>
        </nav>
      </aside>

      {/* Sağ İçerik Alanı */}
      <main className="admin-content"> {/* CSS için içerik alanı class'ı */}
        {/* Outlet, AdminLayout içindeki eşleşen alt rotanın (sayfanın) komponentini render eder */}
        <Outlet /> {/* <-- Seçilen admin sayfası içeriği buraya gelecek */}
      </main>
    </div>
  );
}

export default AdminLayout; // Komponenti dışarıya aktarıyoruz