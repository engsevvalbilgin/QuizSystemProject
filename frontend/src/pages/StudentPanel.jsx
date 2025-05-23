// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\pages\StudentPanel.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function StudentPanel() {
    const [student, setStudent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

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
                                    color: '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px'
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
                                    color: '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px'
                                }}
                            >
                                <span>📝</span>
                                <span>Quiz Çöz</span>
                            </button>
                        </li>
                        <li>
                            <button 
                                onClick={() => navigate('/student/my-results')}
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
                                    gap: '10px'
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
                                    color: '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px'
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
            <div style={{ flex: 1, padding: '20px' }}>
                <h2>Hoş Geldiniz, {student.name} {student.surname}!</h2>
                <p>Öğrenci panelinize hoş geldiniz. Sol menüden işlemlerinizi gerçekleştirebilirsiniz.</p>
            </div>
        </div>
    );
}

export default StudentPanel; // Komponenti dışarıya aktarıyoruz