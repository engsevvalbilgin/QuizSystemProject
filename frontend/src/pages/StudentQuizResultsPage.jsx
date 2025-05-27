import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';


function StudentQuizResultsPage() {
    const [quizAttempts, setQuizAttempts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [student, setStudent] = useState({ name: '', surname: '' });
    const navigate = useNavigate();
    
    // Kullanıcı bilgilerini getir
    useEffect(() => {
        const fetchCurrentUser = async () => {
            try {
                const response = await axiosInstance.get('/users/profile');
                console.log('Raw API response:', response); // API'den gelen ham yanıtı logluyoruz
                console.log('Response data:', response.data); // response.data içeriğini logluyoruz
                setStudent(response.data);
            } catch (err) {
                console.error('Kullanıcı bilgileri yüklenirken hata:', err);
            }
        };
        
        fetchCurrentUser();
    }, []);

    useEffect(() => {
        const fetchQuizAttempts = async () => {
            try {
                setLoading(true);
                setError(null);
                const response = await axiosInstance.get('/student/quiz-attempts');
                console.log('Raw API response:', response);
                console.log('Response data:', response.data);
                console.log('First attempt details:', response.data[0]); // İlk deneme detaylarını logla
                setQuizAttempts(response.data);
            } catch (err) {
                const errorMessage = err.response?.data?.message || 'Quiz sonuçları yüklenirken bir hata oluştu.';
                setError(errorMessage);
                console.error('Quiz attempts error:', err);
                if (err.response) {
                    console.error('Error response data:', err.response.data);
                    console.error('Error status:', err.response.status);
                }
            } finally {
                setLoading(false);
            }
        };

        fetchQuizAttempts();
    }, []);

    const formatDateTime = (dateString) => {
        if (!dateString) return 'Tarih bilgisi yok';
        const date = new Date(dateString);
        return date.toLocaleString('tr-TR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };


    // Puanı x/y formatında göster
    const formatScore = (earnedPoints, totalPoints) => {
        console.log('formatScore called with:', { earnedPoints, totalPoints });
        if (earnedPoints === undefined || earnedPoints === null || totalPoints === undefined || totalPoints === null) {
            console.log('Missing points data, showing --/--');
            return '--/--';
        }
        return `${earnedPoints}/${totalPoints}`;
    };

    // Geçen süreyi formatla (saniye -> dakika:saniye)
    const formatTimeSpent = (seconds) => {
        if (!seconds) return '--:--';
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
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
                                    color: window.location.pathname === '/student/profile' ? '#2563eb' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    fontWeight: window.location.pathname === '/student/profile' ? 'bold' : 'normal'
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
                                    color: window.location.pathname === '/student/solve-quiz' ? '#2563eb' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    fontWeight: window.location.pathname === '/student/solve-quiz' ? 'bold' : 'normal'
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
                                    color: window.location.pathname.startsWith('/quiz-results') ? '#2563eb' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    fontWeight: window.location.pathname.startsWith('/quiz-results') ? 'bold' : 'normal'
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
                                    color: window.location.pathname === '/student/announcements' ? '#2563eb' : '#495057',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '10px',
                                    fontWeight: window.location.pathname === '/student/announcements' ? 'bold' : 'normal'
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
                <h1 className="text-2xl font-bold mb-6">Quiz Sonuçlarım</h1>

            {quizAttempts.length === 0 ? (
                <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
                    <p className="text-yellow-700">Henüz tamamlanmış quiz bulunmamaktadır.</p>
                </div>
            ) : (
                <div className="bg-white shadow-md rounded-lg overflow-hidden">
                    <table className="min-w-full leading-normal">
                        <thead>
                            <tr className="bg-gray-100">
                                <th className="px-5 py-3 border-b-2 border-gray-200 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    Quiz Adı
                                </th>
                                <th className="px-5 py-3 border-b-2 border-gray-200 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    Tamamlanma Tarihi
                                </th>
                                <th className="px-5 py-3 border-b-2 border-gray-200 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    Puan
                                </th>

                                <th className="px-5 py-3 border-b-2 border-gray-200 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    İşlemler
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            {quizAttempts.map((attempt) => (
                                <tr key={attempt.id} className="hover:bg-gray-50">
                                    <td className="px-5 py-4 border-b border-gray-200 text-sm">
                                        <p className="text-gray-900 whitespace-no-wrap font-medium">
                                            {attempt.quizName}
                                        </p>
                                    </td>
                                    <td className="px-5 py-4 border-b border-gray-200 text-sm">
                                        <p className="text-gray-900 whitespace-no-wrap">
                                            {formatDateTime(attempt.completionDate)}
                                        </p>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                                        <div className="flex items-baseline">
                                            <span className="text-lg font-bold">
                                                {formatScore(attempt.earnedPoints, attempt.totalPoints)}
                                            </span>
                                        </div>
                                        {attempt.score !== undefined && attempt.score !== null ? (
                                            <span className="text-xs text-gray-500">
                                                (%{typeof attempt.score === 'number' ? attempt.score.toFixed(2) : '0.00'})
                                            </span>
                                        ) : null}
                                    </td>

                                    <td className="px-5 py-4 border-b border-gray-200 text-sm">
                                        <button 
                                            onClick={() => navigate(`/quiz-results/${attempt.id}`)}
                                            className="px-3 py-1 bg-blue-500 text-white text-xs rounded hover:bg-blue-600 transition-colors"
                                        >
                                            Detaylar
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
            </div>
        </div>
    );
}

export default StudentQuizResultsPage;
