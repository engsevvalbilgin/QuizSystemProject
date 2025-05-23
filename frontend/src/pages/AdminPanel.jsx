import React, { useState, useMemo } from 'react'; // useEffect ve useNavigate artık kullanılmıyor
import axiosInstance from '../api/axiosInstance';

function AdminPanel() {
  // const navigate = useNavigate(); // navigate artık kullanılmıyor, kaldırıldı

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [filterUsernameOrEmail, setFilterUsernameOrEmail] = useState('');
  const [filterRole, setFilterRole] = useState('');

  // Duyuru ile ilgili state'ler tamamen kaldırıldı
  // const [announcements, setAnnouncements] = useState([]);
  // const [showAddAnnouncement, setShowAddAnnouncement] = useState(false);
  // const [newAnnouncement, setNewAnnouncement] = useState({
  //   title: '',
  //   content: ''
  // });

  // Kullanıcıları backend'den getir
  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    setUsers([]); // Eski listeyi temizle
    try {
      const response = await axiosInstance.get('/users'); // Bu endpoint backend'ine göre ayarlanmalı
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

  // Duyuru ile ilgili fonksiyonlar kaldırıldı
  // const fetchAnnouncements = async () => { /* ... */ };
  // useEffect(() => { /* ... */ }, []);
  // const handleCreateAnnouncement = async () => { /* ... */ };

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
      <h2>Admin Paneli - Kullanıcı Yönetimi</h2> {/* Başlık güncellendi */}

      {/* Kullanıcı Listesini Getirme Düğmesi */}
      <button onClick={fetchUsers} disabled={loading}>
        {loading ? 'Yükleniyor...' : "Kullanıcı Listesini Backend'den Getir"}
      </button>

      <h3>Kullanıcı Listesi</h3>

      {/* Filtreleme Seçenekleri */}
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
            {/* Backend'den gelen rol isimlerinin buradaki değerlerle aynı olduğundan emin olun */}
            <option value="ROLE_ADMIN">ADMIN</option>
            <option value="ROLE_TEACHER">TEACHER</option>
            <option value="ROLE_STUDENT">STUDENT</option>
          </select>
        </div>
      </div>

      {/* Hata ve Yüklenme Mesajları */}
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {loading && <p>Kullanıcılar yükleniyor...</p>}

      {/* Kullanıcı Listesi Tablosu */}
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

      {/* Kullanıcı Listesi Boş Durumu Mesajları */}
      {!loading && !error && (!filteredUsers || filteredUsers.length === 0) && (
        users.length > 0 ? (
          <p>Filtreleme sonucuna uyan kullanıcı bulunmuyor.</p>
        ) : (
          <p>Henüz kullanıcı bulunmuyor veya liste getirilmedi.<br />Listelemek için yukarıdaki düğmeye basın.</p>
        )
      )}

      {/* Duyuru ile ilgili tüm JSX kaldırıldı */}
      {/* showAddAnnouncement && ( ... ) */}
      {/* <div className="mt-8"> ... </div> */}

    </div>
  );
}

export default AdminPanel;