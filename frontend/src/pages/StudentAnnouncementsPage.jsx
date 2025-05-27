import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function StudentAnnouncementsPage() {
    const [announcements, setAnnouncements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [student, setStudent] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchStudentProfile = async () => {
            try {
                const response = await axiosInstance.get('/users/profile');
                setStudent(response.data);
            } catch (err) {
                console.error('Profil bilgileri yüklenirken hata oluştu:', err);
            }
        };

        fetchStudentProfile();
    }, []);

    useEffect(() => {
        const fetchAnnouncements = async () => {
            try {
                const response = await axiosInstance.get('/announcements');
                setAnnouncements(response.data);
                setLoading(false);
            } catch (err) {
                console.error('Duyurular yüklenirken hata oluştu:', err);
                setError('Duyurular yüklenirken bir hata oluştu.');
                setLoading(false);
            }
        };

        fetchAnnouncements();
    }, []);

    const formatDate = (dateString) => {
        const options = { 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        };
        return new Date(dateString).toLocaleDateString('tr-TR', options);
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
                <strong className="font-bold">Hata! </strong>
                <span className="block sm:inline">{error}</span>
            </div>
        );
    }

    return (
        <div style={{ display: 'flex', minHeight: '100vh' }}>
            {/* Sidebar */}
            <div style={{
                width: '250px',
                backgroundColor: '#f8f9fa',
                borderRight: '1px solid #dee2e6',
                padding: '20px 0'
            }}>
                <div style={{ padding: '0 15px 15px 15px', borderBottom: '1px solid #dee2e6' }}>
                    <h3 style={{ margin: '0', color: '#495057' }}>Öğrenci Paneli</h3>
                    <p style={{ margin: '5px 0 0 0', fontSize: '0.9em', color: '#6c757d' }}>
                        {student ? `${student.name} ${student.surname}` : 'Yükleniyor...'}
                    </p>
                </div>
                
                <nav style={{ marginTop: '15px' }}>
                    <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                        <li>
                            <button 
                                onClick={() => navigate('/student/profile')}
                                style={{
                                    width: '100%',
                                    textAlign: 'left',
                                    padding: '10px 20px',
                                    border: 'none',
                                    background: window.location.pathname === '/student/profile' ? 'rgba(13, 110, 253, 0.1)' : 'none',
                                    cursor: 'pointer',
                                    fontSize: '1em',
                                    color: window.location.pathname === '/student/profile' ? '#0d6efd' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    borderRight: window.location.pathname === '/student/profile' ? '3px solid #0d6efd' : 'none'
                                }}
                            >
                                <span>👤</span>
                                <span>Profilim</span>
                            </button>
                        </li>
                        <li>
                            <button 
                                onClick={() => navigate('/student/solve-quiz')}
                                style={{
                                    width: '100%',
                                    textAlign: 'left',
                                    padding: '10px 20px',
                                    border: 'none',
                                    background: 'none',
                                    cursor: 'pointer',
                                    fontSize: '1em',
                                    color: window.location.pathname === '/student/solve-quiz' ? '#0d6efd' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    borderRight: window.location.pathname === '/student/solve-quiz' ? '3px solid #0d6efd' : 'none'
                                }}
                            >
                                <span>📝</span>
                                <span>Quiz Çöz</span>
                            </button>
                        </li>
                        <li>
                            <button 
                                onClick={() => navigate('/quiz-results')}
                                style={{
                                    width: '100%',
                                    textAlign: 'left',
                                    padding: '10px 20px',
                                    border: 'none',
                                    background: 'none',
                                    cursor: 'pointer',
                                    fontSize: '1em',
                                    color: window.location.pathname.startsWith('/quiz-results') ? '#0d6efd' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    borderRight: window.location.pathname.startsWith('/quiz-results') ? '3px solid #0d6efd' : 'none'
                                }}
                            >
                                <span>📊</span>
                                <span>Sonuçlarım</span>
                            </button>
                        </li>
                        <li>
                            <button 
                                onClick={() => navigate('/student/announcements')}
                                style={{
                                    width: '100%',
                                    textAlign: 'left',
                                    padding: '10px 20px',
                                    border: 'none',
                                    background: window.location.pathname === '/student/announcements' ? 'rgba(13, 110, 253, 0.1)' : 'none',
                                    cursor: 'pointer',
                                    fontSize: '1em',
                                    color: window.location.pathname === '/student/announcements' ? '#0d6efd' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    borderRight: window.location.pathname === '/student/announcements' ? '3px solid #0d6efd' : 'none'
                                }}
                            >
                                <span>📢</span>
                                <span>Duyurular</span>
                            </button>
                        </li>
                    </ul>
                </nav>
            </div>
            
            {/* Main Content */}
            <div style={{ flex: 1, padding: '20px', overflowY: 'auto' }}>
                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">Duyurular</h1>
                    <p className="text-gray-600">Sistemdeki tüm duyuruları buradan görüntüleyebilirsiniz.</p>
                </div>

                <div className="space-y-4">
                    {announcements.length === 0 ? (
                        <div className="bg-white p-6 rounded-lg shadow-md text-center text-gray-500">
                            Henüz duyuru bulunmamaktadır.
                        </div>
                    ) : (
                        announcements.map((announcement) => (
                            <div key={announcement.id} className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow">
                                <div className="flex justify-between items-start">
                                    <div>
                                        <h2 className="text-xl font-semibold text-gray-800">
                                            {announcement.title}
                                        </h2>
                                        <p className="mt-2 text-gray-600">
                                            {announcement.content}
                                        </p>
                                    </div>
                                    <span className="text-sm text-gray-500 whitespace-nowrap ml-4">
                                        {formatDate(announcement.date)}
                                    </span>
                                </div>
                                <div className="mt-3 pt-3 border-t border-gray-100 text-sm text-gray-500">
                                    <span>Yayınlayan: </span>
                                    <span className="font-medium text-gray-700">
                                        {announcement.user?.name || 'Bilinmeyen Kullanıcı'}
                                    </span>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}

export default StudentAnnouncementsPage;
