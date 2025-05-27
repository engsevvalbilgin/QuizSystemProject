import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function TeacherPanel() {
    const [teacher, setTeacher] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchTeacherProfile = async () => {
            try {
                setLoading(true);
                setError(null);
                const response = await axiosInstance.get('/users/profile');
                
                if (response.data) {
                    if (response.data.role === 'ROLE_TEACHER') {
                        setTeacher(response.data);
                    } else {
                        setError(`Bu sayfaya erişim yetkiniz yok. Rolünüz: ${response.data.role || 'Tanımsız'}`);
                    }
                } else {
                    setError('Profil bilgileri alınamadı.');
                }
            } catch (error) {
                setError('Profil bilgileri yüklenirken bir hata oluştu.');
                console.error('Öğretmen profili yüklenirken hata:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchTeacherProfile();
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
    if (!teacher) {
        return (
            <div className="text-center py-10">
                <p className="text-gray-600">Öğretmen bilgileri bulunamadı.</p>
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
                    <h3 style={{ margin: '0', color: '#495057' }}>Öğretmen Paneli</h3>
                    <p style={{ margin: '5px 0 0 0', fontSize: '0.9em', color: '#6c757d' }}>
                        {teacher.name} {teacher.surname}
                    </p>
                </div>
                
                <nav style={{ marginTop: '15px' }}>
                    <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                        <li>
                            <button 
                                onClick={() => navigate('/teacher/profile')}
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
                        <li style={{ paddingLeft: '30px' }}>
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
                                    gap: '10px'
                                }}
                            >
                                <span>🏆</span>
                                <span>Liderlik Tablosu</span>
                            </button>
                        </li>
                        <li>
                            <button 
                                onClick={() => navigate('/teacher/my-quizzes')}
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
                                <span>📋</span>
                                <span>Quizlerim</span>
                            </button>
                        </li>
                        <li>
                            <button 
                                onClick={() => navigate('/teacher/create-quiz')}
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
                                <span>Yeni Quiz Oluştur</span>
                            </button>
                        </li>
                        <li>
                            <button 
                                onClick={() => navigate('/teacher/quiz/:quizId/questions')}
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
                                <span>Sorular</span>
                            </button>
                        </li>
                        <li>
                            <button 
                                onClick={() => navigate('/teacher/announcements')}
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
                <h2>Hoş Geldiniz, {teacher.name} {teacher.surname}!</h2>
                <p>Öğretmen panelinize hoş geldiniz. Sol menüden işlemlerinizi gerçekleştirebilirsiniz.</p>
            </div>
        </div>
    );
}

export default TeacherPanel;
