import React, { useState, useMemo } from 'react';
import axiosInstance from '../api/axiosInstance';

function AdminPanel() {

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [filterUsernameOrEmail, setFilterUsernameOrEmail] = useState('');
  const [filterRole, setFilterRole] = useState('');

 
  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    setUsers([]); 
    try {
      const response = await axiosInstance.get('/users'); 
      setUsers(response.data);
    } catch (err) {
      console.error("Kullanıcı listesi getirilirken hata oluştu:", err);
      if (err.response) {
        if (err.response.status === 401 || err.response.status === 403) {
          setError("Bu listeyi görüntüleme yetkiniz yok.");
        } else if (err.response.data && err.response.data.message) {
          setError(`Hata: ${err.response.data.message}`);
        } else {
          setError(`Kullanıcı listesi getirilemedi. Durum Kodu: ${err.response.status}`);
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

  const filteredUsers = useMemo(() => {
    if (!users || users.length === 0) return [];

    return users.filter(user => {
      const usernameOrEmailMatch =
        filterUsernameOrEmail === '' ||
        (user.username && user.username.toLowerCase().includes(filterUsernameOrEmail.toLowerCase())) ||
        (user.email && user.email.toLowerCase().includes(filterUsernameOrEmail.toLowerCase()));

      const roleMatch =
        filterRole === '' ||
        (Array.isArray(user.roles) && user.roles.some(role => role.name === filterRole));

      return usernameOrEmailMatch && roleMatch;
    });
  }, [users, filterUsernameOrEmail, filterRole]);

  return (
    <div>
      <h2>Admin Paneli - Kullanıcı Yönetimi</h2> 

      <div style={{
        marginBottom: '20px',
        padding: '15px',
        border: '1px solid #eee',
        borderRadius: '5px'
      }}>
        <div style={{ marginBottom: '10px' }}>
          <button 
            onClick={() => navigate('/leadership-table')}
            style={{
              width: '100%',
              textAlign: 'left',
              padding: '10px 20px',
              border: 'none',
              background: 'none',
              cursor: 'pointer',
              fontSize: '1em',
              color: '#495057',
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              textDecoration: 'none'
            }}
          >
            <span>🏆</span>
            <span>Liderlik Tablosu</span>
          </button>
        </div>
      </div>

      <button onClick={fetchUsers} disabled={loading}>
        {loading ? 'Yükleniyor...' : "Kullanıcı Listesini Backend'den Getir"}
      </button>

      <h3>Kullanıcı Listesi</h3>

      <div style={{ marginBottom: '20px', padding: '15px', border: '1px solid #eee', borderRadius: '5px' }}>
        <h4>Filtreleme Seçenekleri</h4>
        <div>
          <label htmlFor="filterUsernameOrEmail">Kullanıcı Adı / Email Filtre:</label>
          <input
            type="text"
            id="filterUsernameOrEmail"
            value={filterUsernameOrEmail}
            onChange={(e) => setFilterUsernameOrEmail(e.target.value)}
            placeholder="Kullanıcı Adı veya Email"
          />
        </div>
        <div>
          <label htmlFor="filterRole">Rol Filtre:</label>
          <select id="filterRole" value={filterRole} onChange={(e) => setFilterRole(e.target.value)}>
            <option value="">-- Tüm Roller --</option>
            <option value="ROLE_ADMIN">ADMIN</option>
            <option value="ROLE_TEACHER">TEACHER</option>
            <option value="ROLE_STUDENT">STUDENT</option>
          </select>
        </div>
      </div>

      {error && <p style={{ color: 'red' }}>{error}</p>}
      {loading && <p>Kullanıcılar yükleniyor...</p>}

      {!loading && !error && filteredUsers && filteredUsers.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Kullanıcı Adı</th>
              <th>Email</th>
              <th>Roller</th>
              <th>Hesap Etkin mi?</th>
              <th>Öğretmen İsteği Bekliyor mu?</th>
              <th>Oluşturulma Tarihi</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.map(user => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.username}</td>
                <td>{user.email}</td>
                <td>{Array.isArray(user.roles) ? user.roles.map(role => role.name).join(', ') : 'Rol Yok'}</td>
                <td>{user.enabled ? 'Evet' : 'Hayır'}</td>
                <td>{user.teacherRequestPending !== undefined ? (user.teacherRequestPending ? 'Evet' : 'Hayır') : 'N/A'}</td>
                <td>{user.createdAt ? new Date(user.createdAt).toLocaleString() : 'N/A'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {!loading && !error && (!filteredUsers || filteredUsers.length === 0) && (
        users.length > 0 ? (
          <p>Filtreleme sonucuna uyan kullanıcı bulunmuyor.</p>
        ) : (
          <p>Henüz kullanıcı bulunmuyor veya liste getirilmedi.<br />Listelemek için yukarıdaki düğmeye basın.</p>
        )
      )}

    </div>
  );
}

export default AdminPanel;