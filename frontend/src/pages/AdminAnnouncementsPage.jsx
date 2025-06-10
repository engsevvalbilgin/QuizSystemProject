import React, { useState, useEffect } from 'react';
import axiosInstance from '../api/axiosInstance';

function AdminAnnouncementsPage() {
    const [announcements, setAnnouncements] = useState([]);
    const [showAddAnnouncement, setShowAddAnnouncement] = useState(false);
    const [newAnnouncement, setNewAnnouncement] = useState({
        title: '',
        content: ''
    });

    const fetchAnnouncements = async () => {
        try {
            const response = await axiosInstance.get('/announcements');
            setAnnouncements(response.data);
        } catch (err) {
            console.error('Duyuruları getirirken hata:', err);
            alert('Duyurular yüklenirken bir hata oluştu.');
        }
    };

    const handleCreateAnnouncement = async () => {
        if (!newAnnouncement.title || !newAnnouncement.content) {
            alert('Başlık ve içerik boş olamaz!');
            return;
        }

        try {
            const response = await axiosInstance.post('/announcements', {
                title: newAnnouncement.title,
                content: newAnnouncement.content
            });

            if (response.status === 201) {
                setAnnouncements(prev => [...prev, response.data]);
                setShowAddAnnouncement(false);
                setNewAnnouncement({ title: '', content: '' });
                alert('Duyuru başarıyla oluşturuldu!');
                fetchAnnouncements();
            } else {
                throw new Error('Sunucu beklenmedik bir yanıt gönderdi.');
            }
        } catch (err) {
            console.error('Duyuru oluşturma hatası:', err);
            let errorMessage = 'Duyuru oluşturulamadı.';
            if (err.response) {
                switch (err.response.status) {
                    case 403:
                        errorMessage = 'Bu işlemi gerçekleştirmek için yetkiniz yok.';
                        break;
                    case 400:
                        errorMessage = 'Geçersiz duyuru bilgileri.';
                        break;
                    default:
                        errorMessage = err.response.data?.message || 'Sunucu hatası oluştu.';
                }
            } else if (err.request) {
                errorMessage = 'Sunucuya ulaşılamadı.';
            } else {
                errorMessage = err.message;
            }
            alert(errorMessage);
        }
    };

    const handleDeleteAnnouncement = async (announcementId) => {
        if (!window.confirm('Bu duyuruyu silmek istediğinizden emin misiniz?')) {
            return;
        }

        try {
            await axiosInstance.delete(`/announcements/${announcementId}`);
            setAnnouncements(prev => prev.filter(a => a.id !== announcementId));
            alert('Duyuru başarıyla silindi!');
        } catch (err) {
            console.error('Duyuru silme hatası:', err);
            alert('Duyuru silinirken bir hata oluştu.');
        }
    };

    useEffect(() => {
        fetchAnnouncements();
    }, []);

    return (
        <div>
            <h2>Duyurular Yönetimi</h2>

            <div className="mt-4">
                <button
                    onClick={() => setShowAddAnnouncement(true)}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                    Yeni Duyuru Oluştur
                </button>
            </div>

            {showAddAnnouncement && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4">
                    <div className="bg-white rounded-lg p-6 w-96">
                        <h3 className="text-lg font-semibold mb-4">Yeni Duyuru</h3>
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium mb-1">Başlık</label>
                                <input
                                    type="text"
                                    value={newAnnouncement.title}
                                    onChange={(e) => setNewAnnouncement({ ...newAnnouncement, title: e.target.value })}
                                    className="w-full p-2 border rounded"
                                    placeholder="Duyuru başlığı..."
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium mb-1">İçerik</label>
                                <textarea
                                    value={newAnnouncement.content}
                                    onChange={(e) => setNewAnnouncement({ ...newAnnouncement, content: e.target.value })}
                                    className="w-full p-2 border rounded h-32"
                                    placeholder="Duyuru içeriği..."
                                />
                            </div>
                        </div>
                        <div className="flex justify-end space-x-2 mt-4">
                            <button
                                onClick={() => setShowAddAnnouncement(false)}
                                className="px-4 py-2 text-gray-600 hover:text-gray-800"
                            >
                                İptal
                            </button>
                            <button
                                onClick={handleCreateAnnouncement}
                                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                            >
                                Oluştur
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <div className="mt-8">
                {announcements.length === 0 ? (
                    <p>Henüz duyuru bulunmuyor.</p>
                ) : (
                    announcements.map((announcement) => (
                        <div key={announcement.id} className="p-4 border rounded mb-4">
                            <div className="flex justify-between items-start">
                                <div>
                                    <h4 className="font-semibold mb-2">{announcement.title}</h4>
                                    <p className="mb-2">{announcement.content}</p>
                                    <div className="text-sm text-gray-500">
                                        {new Date(announcement.date).toLocaleString()}
                                    </div>
                                </div>
                                <button
                                    onClick={() => handleDeleteAnnouncement(announcement.id)}
                                    className="text-red-600 hover:text-red-800"
                                >
                                    Sil
                                </button>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

export default AdminAnnouncementsPage;
