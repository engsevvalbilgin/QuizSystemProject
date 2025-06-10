import React from 'react';
import { Link, Outlet } from 'react-router-dom'; 

function AdminLayout() {
  return (
    <div className="admin-layout"> 
      <aside className="admin-sidebar"> 
        <h3>Admin Menü</h3>
        <nav>
          <ul>
            <li>
              <Link to="/admin">Dashboard</Link> 
            </li>
            <li>
              <Link to="/admin/users">Kullanıcıları Listele</Link> 
            </li>
            <li>
              <Link to="/admin/teacher-requests">Öğretmen Başvuruları</Link>
            </li>
            <li>
              <Link to="/admin/announcements">Duyurular</Link>
            </li>
            
          </ul>
        </nav>
      </aside>

      <main className="admin-content"> 
        <Outlet /> 
      </main>
    </div>
  );
}

export default AdminLayout; 