import React, { useState, useMemo, useEffect } from 'react';
import axiosInstance from '../api/axiosInstance';

function AdminUserListPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filterUsernameOrEmail, setFilterUsernameOrEmail] = useState('');
  const [filterRole, setFilterRole] = useState('');
  const [updatingUsers, setUpdatingUsers] = useState({}); // Track which users are being updated

  // Kullanıcı rolleri için görünen isimler
  const roleDisplayNames = {
    'ROLE_ADMIN': 'Admin',
    'ROLE_TEACHER': 'Öğretmen',
    'ROLE_STUDENT': 'Öğrenci'
  };

  // Kullanıcı tipine göre rol belirleme
  const getUserRole = (user) => {
    if (user.roles && user.roles.length > 0) {
      return user.roles.map(role => {
        if (typeof role === 'object') {
          return roleDisplayNames[role.name] || role.name || role.authority || 'Bilinmeyen Rol';
        }
        return roleDisplayNames[role] || role;
      }).join(', ');
    }
    
    if (user.dtype) {
      const roleMap = {
        'Admin': 'Admin',
        'Teacher': 'Öğretmen',
        'Student': 'Öğrenci',
        'admin': 'Admin',
        'teacher': 'Öğretmen',
        'student': 'Öğrenci'
      };
      return roleMap[user.dtype] || user.dtype;
    }
    
    if (user.username && user.username.toLowerCase().includes('admin')) {
      return 'Admin';
    } else if (user.username && user.username.toLowerCase().includes('teacher') || 
               user.username && user.username.toLowerCase().includes('ogretmen') ||
               user.username && user.username.toLowerCase().includes('hoca')) {
      return 'Öğretmen';
    }
    
    return 'Öğrenci';
  };

  // Kullanıcının aktiflik durumunu değiştiren fonksiyon
  const toggleUserActivation = async (userId, currentStatus) => {
    if (window.confirm(`Bu kullanıcıyı ${currentStatus ? 'pasif' : 'aktif'} hale getirmek istediğinize emin misiniz?`)) {
      try {
        setUpdatingUsers(prev => ({ ...prev, [userId]: true }));
        
        const token = localStorage.getItem('token');
        const response = await axiosInstance.put(
          `/users/${userId}/toggle-activation`,
          {},
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        
        // Update the user's status in the local state
        setUsers(prevUsers => 
          prevUsers.map(user => 
            user.id === userId 
              ? { ...user, enabled: !currentStatus } 
              : user
          )
        );
        
        // Show success message
        alert(response.data.message);
      } catch (err) {
        console.error('Kullanıcı durumu güncellenirken hata oluştu:', err);
        const errorMessage = err.response?.data?.message || 'Bir hata oluştu. Lütfen tekrar deneyin.';
        alert(errorMessage);
      } finally {
        setUpdatingUsers(prev => ({ ...prev, [userId]: false }));
      }
    }
  };

  // Kullanıcıları backend'den getiren fonksiyon
  const fetchUsers = async () => {
    setError(null);
    setLoading(true);
    setUsers([]);

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Oturum bulunamadı. Lütfen tekrar giriş yapın.');
      }

      const response = await axiosInstance.get('/users', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      const usersWithRoles = response.data.map(user => {
        const role = user.role || 'ROLE_STUDENT';
        
        const roleDisplayMap = {
          'ROLE_ADMIN': 'Admin',
          'ROLE_TEACHER': 'Öğretmen',
          'ROLE_STUDENT': 'Öğrenci'
        };
        
        return {
          ...user,
          roles: [role],
          roleDisplay: roleDisplayMap[role] || role,
          dtype: {
            'ROLE_ADMIN': 'Admin',
            'ROLE_TEACHER': 'Teacher',
            'ROLE_STUDENT': 'Student'
          }[role] || 'Student'
        };
      });
      
      setUsers(usersWithRoles);
    } catch (err) {
      console.error("Kullanıcı listesi getirilirken hata oluştu:", err);
      if (err.response) {
        if (err.response.status === 401 || err.response.status === 403) {
          setError("Bu listeyi görüntüleme yetkiniz yok.");
        } else {
          setError(`Hata: ${err.response.data?.message || 'Bilinmeyen bir hata oluştu'}`);
        }
      } else if (err.request) {
        setError("Sunucuya ulaşılamadı. Lütfen internet bağlantınızı kontrol edin.");
      } else {
        setError("Beklenmeyen bir hata oluştu.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const checkAndFetch = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        await fetchUsers();
      } else {
        setError('Oturum bulunamadı. Lütfen tekrar giriş yapın.');
      }
    };
    
    checkAndFetch();
  }, []);

  const filteredUsers = useMemo(() => {
    if (!users || users.length === 0) return [];

    return users.filter(user => {
      const usernameOrEmailMatch =
        filterUsernameOrEmail === '' ||
        (user.username && user.username.toLowerCase().includes(filterUsernameOrEmail.toLowerCase())) ||
        (user.email && user.email.toLowerCase().includes(filterUsernameOrEmail.toLowerCase()));

      let roleMatch = true;
      if (filterRole) {
        roleMatch = Array.isArray(user.roles) && 
                  user.roles.some(role => {
                    if (typeof role === 'string') {
                      return role === filterRole;
                    } else if (role && typeof role === 'object') {
                      return role.name === filterRole || 
                             (role.authority && role.authority === filterRole);
                    }
                    return false;
                  });
      }

      return usernameOrEmailMatch && roleMatch;
    });
  }, [users, filterUsernameOrEmail, filterRole]);

  return (
    <div style={{ padding: '20px' }}>
      <h2 style={{ marginBottom: '20px' }}>Kullanıcı Listesi Yönetimi</h2>

      <div style={{ marginBottom: '20px' }}>
        <button 
          onClick={fetchUsers} 
          disabled={loading} 
          style={{
            padding: '8px 16px',
            backgroundColor: '#4CAF50',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            marginRight: '10px'
          }}
        >
          {loading ? 'Yükleniyor...' : 'Kullanıcı Listesini Yenile'}
        </button>
      </div>

      {/* Filtreleme Seçenekleri */}
      <div style={{ 
        marginBottom: '20px', 
        padding: '15px', 
        border: '1px solid #eee', 
        borderRadius: '5px',
        backgroundColor: '#f9f9f9'
      }}>
        <h4 style={{ marginTop: 0 }}>Kullanıcıları Filtrele</h4>
        <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: '500' }}>
              Kullanıcı Adı/Email:
            </label>
            <input
              type="text"
              value={filterUsernameOrEmail}
              onChange={(e) => setFilterUsernameOrEmail(e.target.value)}
              placeholder="Kullanıcı adı veya email ile filtrele..."
              style={{
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ddd',
                width: '250px'
              }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: '500' }}>
              Rol:
            </label>
            <select
              value={filterRole}
              onChange={(e) => setFilterRole(e.target.value)}
              style={{
                padding: '8px',
                borderRadius: '4px',
                border: '1px solid #ddd',
                minWidth: '150px'
              }}
            >
              <option value="">Tüm Roller</option>
              <option value="ROLE_ADMIN">Admin</option>
              <option value="ROLE_TEACHER">Öğretmen</option>
              <option value="ROLE_STUDENT">Öğrenci</option>
            </select>
          </div>
        </div>
      </div>

      {/* Hata Mesajı */}
      {error && (
        <div style={{ 
          color: '#d32f2f', 
          backgroundColor: '#fde7e9', 
          padding: '15px', 
          borderRadius: '4px',
          marginBottom: '20px',
          border: '1px solid #f5c6cb'
        }}>
          {error}
        </div>
      )}

      {/* Kullanıcı Listesi */}
      {loading ? (
        <p>Kullanıcılar yükleniyor...</p>
      ) : !error && filteredUsers && filteredUsers.length > 0 ? (
        <div style={{ marginTop: '20px', overflowX: 'auto' }}>
          <table style={{ 
            width: '100%', 
            borderCollapse: 'collapse', 
            minWidth: '800px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
          }}>
            <thead>
              <tr style={{ 
                backgroundColor: '#f2f2f2',
                borderBottom: '2px solid #ddd'
              }}>
                <th style={{ 
                  padding: '12px', 
                  textAlign: 'left',
                  fontWeight: '600',
                  color: '#333'
                }}>ID</th>
                <th style={{ 
                  padding: '12px', 
                  textAlign: 'left',
                  fontWeight: '600',
                  color: '#333'
                }}>Kullanıcı Adı</th>
                <th style={{ 
                  padding: '12px', 
                  textAlign: 'left',
                  fontWeight: '600',
                  color: '#333'
                }}>Email</th>
                <th style={{ 
                  padding: '12px', 
                  textAlign: 'left',
                  fontWeight: '600',
                  color: '#333'
                }}>Rol</th>
                <th style={{ 
                  padding: '12px', 
                  textAlign: 'left',
                  fontWeight: '600',
                  color: '#333'
                }}>Durum</th>
                <th style={{ 
                  padding: '12px', 
                  textAlign: 'left',
                  fontWeight: '600',
                  color: '#333'
                }}>Oluşturulma Tarihi</th>
                <th style={{ 
                  padding: '12px', 
                  textAlign: 'left',
                  fontWeight: '600',
                  color: '#333',
                  width: '120px'
                }}>İşlemler</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((user, index) => (
                <tr 
                  key={user.id} 
                  style={{ 
                    borderBottom: '1px solid #eee',
                    backgroundColor: index % 2 === 0 ? '#fff' : '#f9f9f9',
                    transition: 'background-color 0.2s'
                  }}
                >
                  <td style={{ 
                    padding: '12px', 
                    borderBottom: '1px solid #eee'
                  }}>{user.id}</td>
                  <td style={{ 
                    padding: '12px', 
                    borderBottom: '1px solid #eee'
                  }}>{user.username}</td>
                  <td style={{ 
                    padding: '12px', 
                    borderBottom: '1px solid #eee'
                  }}>{user.email}</td>
                  <td style={{ 
                    padding: '12px', 
                    borderBottom: '1px solid #eee'
                  }}>
                    <span style={{
                      padding: '4px 10px',
                      borderRadius: '12px',
                      fontSize: '0.85em',
                      fontWeight: 500,
                      backgroundColor: 
                        user.role === 'ROLE_ADMIN' ? '#e3f2fd' : 
                        user.role === 'ROLE_TEACHER' ? '#e8f5e9' : '#f5f5f5',
                      color: 
                        user.role === 'ROLE_ADMIN' ? '#0d47a1' : 
                        user.role === 'ROLE_TEACHER' ? '#2e7d32' : '#424242'
                    }}>
                      {user.roleDisplay || getUserRole(user)}
                    </span>
                  </td>
                  <td style={{ 
                    padding: '12px', 
                    borderBottom: '1px solid #eee'
                  }}>
                    <span style={{
                      color: user.enabled ? '#2e7d32' : '#d32f2f',
                      fontWeight: 500
                    }}>
                      {user.enabled ? 'Aktif' : 'Pasif'}
                    </span>
                  </td>
                  <td style={{ 
                    padding: '12px', 
                    borderBottom: '1px solid #eee'
                  }}>
                    {user.createdAt ? new Date(user.createdAt).toLocaleString('tr-TR') : 'N/A'}
                  </td>
                  <td style={{ 
                    padding: '12px', 
                    borderBottom: '1px solid #eee',
                    whiteSpace: 'nowrap'
                  }}>
                    {user.role !== 'ROLE_ADMIN' && (
                      <button
                        onClick={() => toggleUserActivation(user.id, user.enabled)}
                        disabled={updatingUsers[user.id]}
                        style={{
                          padding: '6px 12px',
                          borderRadius: '4px',
                          border: 'none',
                          backgroundColor: user.enabled ? '#f8d7da' : '#d4edda',
                          color: user.enabled ? '#721c24' : '#155724',
                          cursor: 'pointer',
                          fontWeight: 500,
                          transition: 'all 0.2s',
                          opacity: updatingUsers[user.id] ? 0.7 : 1,
                          pointerEvents: updatingUsers[user.id] ? 'none' : 'auto'
                        }}
                        title={user.enabled ? 'Kullanıcıyı pasif yap' : 'Kullanıcıyı aktif yap'}
                      >
                        {updatingUsers[user.id] 
                          ? 'İşleniyor...' 
                          : (user.enabled ? 'Pasif Yap' : 'Aktif Yap')}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : !error ? (
        <div style={{ 
          padding: '20px', 
          backgroundColor: '#f8f9fa', 
          borderRadius: '4px',
          border: '1px dashed #ddd',
          textAlign: 'center',
          color: '#6c757d'
        }}>
          {users.length > 0 ? (
            <p>Filtreleme sonucuna uyan kullanıcı bulunmuyor.</p>
          ) : (
            <p>Henüz kullanıcı bulunmuyor veya liste yüklenmedi.</p>
          )}
        </div>
      ) : null}
    </div>
  );
}

export default AdminUserListPage;