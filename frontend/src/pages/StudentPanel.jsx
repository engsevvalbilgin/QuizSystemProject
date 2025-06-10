import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import StudentLeadershipTablePage from './StudentLeadershipTablePage';

function StudentPanel() {
    const [student, setStudent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [notification, setNotification] = useState(null);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        if (location.state?.quizCompleted) {
            setNotification({
                type: 'success',
                message: 'Quiz başarıyla tamamlandı!',
                attemptId: location.state.attemptId
            });
            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [location, navigate]); 

    useEffect(() => {
        const fetchStudentProfile = async () => {
            try {
                setLoading(true);
                setError(null);
                const response = await axiosInstance.get('/users/profile');
                
                if (response.data) {
                    if (response.data.role === 'ROLE_STUDENT') {
                        setStudent(response.data);
                    } else {
                        setError(`Bu sayfaya erişim yetkiniz yok. Rolünüz: ${response.data.role || 'Tanımsız'}`);
                    }
                } else {
                    setError('Profil bilgileri alınamadı.');
                }
            } catch (error) {
                setError('Profil bilgileri yüklenirken bir hata oluştu.');
                console.error('Öğrenci profili yüklenirken hata:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchStudentProfile();
    }, []);

    const [showLeadershipTable, setShowLeadershipTable] = useState(false);

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

    if (!student) {
        return (
            <div className="text-center py-10">
                <p className="text-gray-600">Öğrenci bilgileri bulunamadı.</p>
            </div>
        );
    }
    
    return (
        <div style={{ display: 'flex', minHeight: '100vh' }}>
            <div style={{
                width: '250px',
                backgroundColor: '#f8f9fa',
                borderRight: '1px solid #dee2e6',
                padding: '20px 0'
            }}>
                <div style={{ padding: '0 15px 15px 15px', borderBottom: '1px solid #dee2e6' }}>
                    <h3 style={{ margin: '0', color: '#495057' }}>Öğrenci Paneli</h3>
                    <p style={{ margin: '5px 0 0 0', fontSize: '0.9em', color: '#6c757d' }}>
                        {student.name} {student.surname}
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
                                    background: 'none',
                                    cursor: 'pointer',
                                    fontSize: '1em',
                                    color: location.pathname === '/student/profile' ? '#2563eb' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    fontWeight: location.pathname === '/student/profile' ? 'bold' : 'normal'
                                }}
                            >
                                <span>👤</span>
                                <span>Profilim</span>
                            </button>
                        </li>
                        <li>
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
                                    color: location.pathname === '/leadership-table' ? '#2563eb' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    fontWeight: location.pathname === '/leadership-table' ? 'bold' : 'normal'
                                }}
                            >
                                <span>🏆</span>
                                <span>Liderlik Tablosu</span>
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
                                    color: location.pathname === '/student/solve-quiz' ? '#2563eb' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    fontWeight: location.pathname === '/student/solve-quiz' ? 'bold' : 'normal'
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
                                    color: location.pathname.startsWith('/quiz-results') ? '#2563eb' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    fontWeight: location.pathname.startsWith('/quiz-results') ? 'bold' : 'normal'
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
                                    background: 'none',
                                    cursor: 'pointer',
                                    fontSize: '1em',
                                    color: location.pathname === '/student/announcements' ? '#2563eb' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    fontWeight: location.pathname === '/student/announcements' ? 'bold' : 'normal'
                                }}
                            >
                                <span>📢</span>
                                <span>Duyurular</span>
                            </button>
                        </li>
                    </ul>
                </nav>
            </div>
            <div style={{ flexGrow: 1, padding: '20px' }}>
                {notification && (
                    <div className="mb-4 p-4 rounded" style={{ 
                        backgroundColor: notification.type === 'success' ? '#d1fae5' : '#fee2e2',
                        borderLeft: `4px solid ${notification.type === 'success' ? '#10b981' : '#ef4444'}`,
                        marginBottom: '20px'
                    }}>
                        <div className="flex items-center">
                            <span style={{ marginRight: '10px' }}>
                                {notification.type === 'success' ? '✅' : '⚠️'}
                            </span>
                            <div>
                                <p style={{ fontWeight: 'bold', margin: '0 0 5px 0' }}>{notification.message}</p>
                                {notification.attemptId && (
                                    <button 
                                        onClick={() => navigate(`/quiz-results/${notification.attemptId}`)}
                                        style={{
                                            backgroundColor: '#3b82f6',
                                            color: 'white',
                                            border: 'none',
                                            padding: '8px 12px',
                                            borderRadius: '4px',
                                            cursor: 'pointer',
                                            fontSize: '0.875rem',
                                            marginTop: '5px'
                                        }}
                                    >
                                        Sonuçları Görüntüle
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                )}
                
                <h2>Hoş Geldiniz, {student.name} {student.surname}!</h2>
                <p>Öğrenci panelinize hoş geldiniz. Sol menüden işlemlerinizi gerçekleştirebilirsiniz.</p>

                {showLeadershipTable && (
                    <div id="leadership-table-container" style={{
                        marginTop: '20px',
                        padding: '20px',
                        backgroundColor: '#fff',
                        borderRadius: '8px',
                        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                    }}>
                        <StudentLeadershipTablePage />
                    </div>
                )}
            </div>
        </div>
    );
}

export default StudentPanel;