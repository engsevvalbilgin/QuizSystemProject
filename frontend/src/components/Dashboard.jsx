import React, { useState, useEffect } from 'react';
import axiosInstance from '../api/axiosInstance';
import LogoutButton from './LogoutButton';

function Dashboard() {
    const [announcements, setAnnouncements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchAnnouncements();
    }, []);

    const fetchAnnouncements = async () => {
        try {
            setLoading(true);
            const response = await axiosInstance.get('/announcements');
            setAnnouncements(response.data);
            setError(null);
        } catch (err) {
            console.error('Error fetching announcements:', err);
            setError('Duyurular yüklenirken bir hata oluştu');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <h2 className="text-2xl font-bold mb-4">Dashboard</h2>

            {/* Duyuru Alanı */}
            <div className="bg-white rounded-lg shadow p-4 mb-6">
                <h3 className="text-lg font-semibold mb-4">Duyurular</h3>
                
                {loading ? (
                    <p>Yükleniyor...</p>
                ) : error ? (
                    <p className="text-red-600">{error}</p>
                ) : announcements.length === 0 ? (
                    <p>Şu anda aktif duyuru bulunmamaktadır.</p>
                ) : (
                    <div className="space-y-4">
                        {announcements.map((announcement) => (
                            <div key={announcement.id} className="p-4 border rounded">
                                <h4 className="font-semibold mb-2">{announcement.title}</h4>
                                <p className="mb-2">{announcement.content}</p>
                                <div className="text-sm text-gray-500">
                                    {new Date(announcement.date).toLocaleString()}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Diğer dashboard içeriği buraya gelecek */}
            <div className="mt-4">
                <LogoutButton />
            </div>
        </div>
    );
}

export default Dashboard;
