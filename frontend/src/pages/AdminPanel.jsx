// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\pages\AdminPanel.jsx
import React, { useState, useMemo } from 'react'; // useState ve useMemo hooklarını import et (useEffect kaldırıldı)
import axiosInstance from '../api/axiosInstance'; // Oluşturduğumuz özel axios instance'ını import et

function AdminPanel() {
  // Kullanıcı listesi için state (Backend'den gelen ham liste)
  const [users, setUsers] = useState([]);
  // Yüklenme durumu için state
  const [loading, setLoading] = useState(false);
  // Hata durumu için state
  const [error, setError] = useState(null);

  // Filtre input değerleri için state
  const [filterUsernameOrEmail, setFilterUsernameOrEmail] = useState('');
  const [filterRole, setFilterRole] = useState(''); // Örneğin 'ROLE_STUDENT', 'ROLE_TEACHER', '' (hepsi)


  // --- Kullanıcı Listesini Getiren Fonksiyon ---
  const fetchUsers = async () => {
     setError(null);
    setLoading(true);
    setUsers([]); // Eski listeyi temizle

    console.log("AdminPanel: Kullanıcı listesi getirme başlatıldı...");

    try {
      // axiosInstance kullanarak GET isteği yapıyoruz
      const response = await axiosInstance.get('/users');

      // İstek başarılıysa, kullanıcı listesini state'e kaydet
      setUsers(response.data);

    } catch (err) {
      console.error("AdminPanel: Kullanıcı listesi getirilirken hata oluştu:", err);
         if (err.response) {
             if (err.response.status === 401 || err.response.status === 403) {
                 setError("Bu listeyi görüntüleme yetkiniz yok.");
             } else if (err.response.data && err.response.data.message) {
                  setError(`Hata: ${err.response.data.message}`);
             }
              else {
                 setError(`Kullanıcı listesi getirilemedi. Status: ${err.response.status}`);
             }
         } else if (err.request) {
             setError("Sunucuya ulaşılamadı. Lütfen tekrar deneyin.");
         } else {
             setError("Beklenmeyen bir hata oluştu.");
         }

    } finally {
      setLoading(false);
      console.log("AdminPanel: Kullanıcı listesi getirme tamamlandı.");
    }
  };

  // --- Filtrelenmiş Kullanıcı Listesini Hesaplama ---
  // users state'i veya filtre state'leri değiştiğinde yeniden hesaplanır
  const filteredUsers = useMemo(() => {
    // Eğer users listesi boşsa, filtrelemeye gerek yok
    if (!users || users.length === 0) {
      return [];
    }

    // users listesini filtrele
    return users.filter(user => {
      // 1. Kullanıcı Adı veya Email Filtresi (Büyük/küçük harf duyarsız)
      const usernameOrEmailMatch = filterUsernameOrEmail === '' ||
                                   (user.username && user.username.toLowerCase().includes(filterUsernameOrEmail.toLowerCase())) ||
                                   (user.email && user.email.toLowerCase().includes(filterUsernameOrEmail.toLowerCase()));


      // 2. Rol Filtresi (user.roles'un dizi olduğundan emin ol)
      // Eğer filterRole boşsa veya kullanıcının rolleri arasında bu rol varsa eşleşir
      const roleMatch = filterRole === '' ||
                        (Array.isArray(user.roles) && user.roles.some(role => role.name === filterRole)); // Rol tam eşleşmeli


      // Hem kullanıcı adı/email filtresi hem de rol filtresi eşleşiyorsa true döndür
      return usernameOrEmailMatch && roleMatch;
    });
  }, [users, filterUsernameOrEmail, filterRole]); // users, filterUsernameOrEmail veya filterRole değiştiğinde bu useMemo çalışır


  // --- Komponentin Render Ettiği JSX ---
  return (
    <div>
      <h2>Admin Paneli</h2>

      {/* Kullanıcıları Getirme Düğmesi */}
      {/* fetchUsers fonksiyonu sadece backend'den ham listeyi getirir */}
      <button onClick={fetchUsers} disabled={loading}>
         {loading ? 'Yükleniyor...' : 'Kullanıcı Listesini Backend\'den Getir'}
      </button>

      <h3>Kullanıcı Listesi</h3> {/* Liste başlığı */}

      {/* --- Filtre Alanları --- */}
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
              {/* Rol seçimi için basit bir select box */}
              <select id="filterRole" value={filterRole} onChange={(e) => setFilterRole(e.target.value)}>
                  <option value="">-- Tüm Roller --</option>
                  {/* Varsayımsal roller, backend'den dinamik olarak alınabilirler */}
                  <option value="ROLE_ADMIN">ADMIN</option>
                  <option value="ROLE_TEACHER">TEACHER</option>
                  <option value="ROLE_STUDENT">STUDENT</option>
                  {/* Daha fazla rol varsa buraya ekleyin */}
              </select>
          </div>
      </div>


      {/* Hata Durumunu Göster */}
      {error && <p style={{ color: 'red' }}>{error}</p>}

      {/* Yüklenme Durumunu Göster (Veri ilk kez getirilirken veya yeniden getirilirken) */}
       {loading && <p>Kullanıcılar yükleniyor...</p>}


      {/* Kullanıcı Listesini Göster */}
      {/* loading false AND error null AND filtrelenmiş kullanıcı listesi boş değilse tabloyu göster */}
      {/* Tabloyu göstermek için filtrelenmiş listeyi kullanıyoruz */}
      {!loading && !error && filteredUsers && filteredUsers.length > 0 && (
        // DİKKAT: Aşağıdaki <table>, <thead>, <tbody>, <tr>, <td> tagleri arasında gereksiz boşluk (space, tab, newline) OLMAMALI!
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
              {/* <th>Güncelleme Tarihi</th> */}
            </tr>
          </thead>
          <tbody>
            {/* filtrelenmiş kullanıcı listesindeki her kullanıcı için bir satır oluştur */}
            {filteredUsers.map(user => (
              // DİKKAT: Aşağıdaki <tr> ve <td> tagleri arasında gereksiz boşluk (space, tab, newline) OLMAMALI!
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.username}</td>
                <td>{user.email}</td>
                {/* Rolleri virgülle ayrılmış string olarak veya 'Rol Yok' göster */}
                <td>{Array.isArray(user.roles) ? user.roles.map(role => role.name).join(', ') : 'Rol Yok'}</td>
                <td>{user.enabled ? 'Evet' : 'Hayır'}</td>
                {/* Backend'den gelen teacherRequestPending alanını göster */}
                 <td>{user.teacherRequestPending !== undefined ? (user.teacherRequestPending ? 'Evet' : 'Hayır') : 'N/A'}</td>
                {/* Oluşturulma tarihini göster ve formatla */}
                <td>{user.createdAt ? new Date(user.createdAt).toLocaleString() : 'N/A'}</td>
                {/* Güncelleme tarihi */}
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* Kullanıcı listesi boşsa (yüklenmiyor, hata yok ve liste boşsa veya filtrelenmiş liste boşsa) */}
       {!loading && !error && (!filteredUsers || filteredUsers.length === 0) && ( // Updated check for filteredUsers
           users.length > 0 ? ( // Eğer backend'den veri geldiyse ama filtre sonucu boşsa
                <p>Filtreleme sonucuna uyan kullanıcı bulunmuyor.</p>
           ) : ( // Eğer backend'den hiç veri gelmediyse (ilk durum veya getirme sonrası boşsa)
                <p>Henüz kullanıcı bulunmuyor veya liste getirilmedi.<br/>Listelemek için yukarıdaki düğmeye basın.</p>
           )
       )}


      {/* Buraya Admin paneli ile ilgili diğer özellikler eklenecek (örn: kullanıcı ekle, rol değiştirme formları vb.) */}
    </div>
  );
}

export default AdminPanel;