// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\pages\AdminUserListPage.jsx
import React, { useState, useMemo } from 'react'; // useState ve useMemo hooklarını import et
import axiosInstance from '../api/axiosInstance'; // Oluşturduğumuz özel axios instance'ını import et

// Bu komponent, Admin panel layoutu içinde /admin/users yolunda render edilecek
function AdminUserListPage() {
  // Kullanıcı listesi için state (Backend'den gelen ham liste)
  const [users, setUsers] = useState([]);
  // Yüklenme durumu için state
  const [loading, setLoading] = useState(false);
  // Hata durumu için state
  const [error, setError] = useState(null);

   // NOT: Filtreleme state'leri (filterUsernameOrEmail, filterRole) ve filtreleme inputları bu sayfada şimdilik YOK.
   // Filtreleme mantığı (filteredUsers useMemo'su) hala var ama filtre inputları olmadığı için tam çalışmayacak veya sadece boş filtreyle çalışacak.
   // İsterseniz useMemo'yu da silebilir, sadece users listesini kullanabilirsiniz şimdilik.
   // Filtrelemeyi daha sonra tekrar ekleyebiliriz.

   // Şimdilik useMemo'yu filtreleme inputları olmadan sadece users listesini dönecek şekilde güncelleyelim veya kaldıralım.
   // useMemo'yu kaldırmak daha temiz olur şimdilik.
   // Filtrelenmiş kullanıcı listesi state'i yerine doğrudan users state'ini kullanacağız render'da.
   // const [filterUsernameOrEmail, setFilterUsernameOrEmail] = useState('');
   // const [filterRole, setFilterRole] = useState('');


  // --- Kullanıcı Listesini Getiren Fonksiyon ---
  // Bu fonksiyon gerektiğinde (örneğin sayfa yüklendiğinde veya bir düğmeye basıldığında) çağrılabilir.
  // Şimdilik yine düğme ile tetikleyelim.
  const fetchUsers = async () => {
     setError(null);
     setLoading(true);
     setUsers([]); // Eski listeyi temizle

    console.log("AdminUserListPage: Kullanıcı listesi getirme başlatıldı...");

    try {
      // axiosInstance kullanarak GET isteği yapıyoruz
      const response = await axiosInstance.get('/users');

      // İstek başarılıysa, kullanıcı listesini state'e kaydet
      setUsers(response.data);

    } catch (err) {
      console.error("AdminUserListPage: Kullanıcı listesi getirilirken hata oluştu:", err);
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
      console.log("AdminUserListPage: Kullanıcı listesi getirme tamamlandı.");
    }
  };

  // --- Komponent Yüklendiğinde Liste Getirme (İsteğe Bağlı - Düğme Kullanıyorsak Gerekmez) ---
  // useMemo hook'u bu sayfada şimdilik kullanılmıyor.
  // useEffect(() => {
  //   fetchUsers(); // Sayfa yüklendiğinde otomatik getir
  // }, []); // Boş dependency array []

  // --- Komponentin Render Ettiği JSX ---
  return (
    <div>
      {/* Bu sayfa AdminLayout içindeki içerik alanında görünecek */}
      <h3>Kullanıcı Listesi</h3> {/* Sayfa Başlığı */}

      {/* Kullanıcıları Getirme Düğmesi (fetchUsers fonksiyonunu çağırır) */}
       <button onClick={fetchUsers} disabled={loading}>
          {loading ? 'Yükleniyor...' : 'Kullanıcı Listesini Getir'}
       </button>

      {/* NOT: Filtre Alanları bu sayfada şimdilik YOK */}


      {/* Hata Durumunu Göster */}
      {error && <p style={{ color: 'red' }}>{error}</p>}

      {/* Yüklenme Durumunu Göster */}
      {loading && <p>Kullanıcılar yükleniyor...</p>}


      {/* Kullanıcı Listesini Göster */}
      {/* loading false AND error null AND users listesi boş değilse tabloyu göster */}
      {/* Doğrudan users state'ini kullanıyoruz */}
      {!loading && !error && users && users.length > 0 && (
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
            {/* users dizisindeki her kullanıcı için bir satır oluştur */}
            {users.map(user => (
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

      {/* Kullanıcı listesi boşsa (yüklenmiyor, hata yok ve liste boşsa) */}
       {!loading && !error && (!users || users.length === 0) && (
           users.length > 0 ? ( // Eğer backend'den veri geldiyse ama liste boşsa
                <p>Filtreleme sonucuna uyan kullanıcı bulunmuyor.</p> // Bu mesaj normalde filtreden sonra görünür, şimdilik backend'den boş gelirse de görülebilir.
           ) : ( // Eğer backend'den hiç veri gelmediyse (ilk durum veya getirme sonrası boşsa)
                <p>Henüz kullanıcı bulunmuyor veya liste getirilmedi.<br/>Listelemek için yukarıdaki düğmeye basın.</p> // Başlangıçta bu görünür
           )
       )}

      {/* Buraya Kullanıcı listesi sayfası ile ilgili diğer özellikler eklenecek */}

    </div>
  );
}

export default AdminUserListPage; // Komponenti dışarıya aktarıyoruz