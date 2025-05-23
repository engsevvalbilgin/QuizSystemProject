import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function TeacherMyQuizzesPage() {
    const [quizzes, setQuizzes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [userId, setUserId] = useState(null);
    const navigate = useNavigate();

    // Navigate to create quiz page
    const handleCreateQuiz = () => {
        navigate('/teacher/create-quiz');
    };

    // Navigate to edit quiz page
    const handleEditQuiz = (quizId) => {
        navigate(`/teacher/edit-quiz/${quizId}`);
    };

    // Delete a quiz
    const handleDeleteQuiz = async (quizId) => {
        if (!window.confirm('Bu quizi silmek istediğinize emin misiniz?')) {
            return;
        }
        
        try {
            await axiosInstance.delete(`/quizzes/${quizId}`);
            // Update the quizzes list after deletion
            setQuizzes(quizzes.filter(quiz => quiz.id !== quizId));
        } catch (err) {
            console.error('Quiz silinirken hata:', err);
            alert('Quiz silinirken bir hata oluştu: ' + (err.response?.data?.message || err.message));
        }
    };
    
    // Activate a quiz
    const handleActivateQuiz = async (quizId) => {
        try {
            console.log(`Quiz aktifleştiriliyor - Quiz ID: ${quizId}`);
            const response = await axiosInstance.put(`/quizzes/${quizId}/activate`);
            console.log('Quiz aktifleştirme yanıtı:', response.data);
            
            // Update the quizzes list to reflect the new active status
            setQuizzes(quizzes.map(quiz => 
                quiz.id === quizId ? { ...quiz, isActive: true } : quiz
            ));
            
            alert('Quiz başarıyla aktifleştirildi!');
        } catch (err) {
            console.error('Quiz aktifleştirilirken hata:', err);
            alert('Quiz aktifleştirilirken bir hata oluştu: ' + (err.response?.data?.message || err.message));
        }
    };

    // Navigate to view quiz questions
    const handleViewQuestions = (quizId) => {
        console.log(`Soruları Gör butonuna tıklandı - Quiz ID: ${quizId}`);
        try {
            navigate(`/teacher/quiz/${quizId}/questions`);
            console.log(`Navigation attempted to: /teacher/quiz/${quizId}/questions`);
        } catch (err) {
            console.error('Soruları görüntüleme sayfasına yönlendirme hatası:', err);
        }
    };
    
    // Activate all quizzes at once
    const handleActivateAllQuizzes = async () => {
        if (!userId) {
            alert('Kullanıcı bilgisi bulunamadı.');
            return;
        }

        try {
            console.log(`Tüm quizler aktifleştiriliyor - Öğretmen ID: ${userId}`);
            const response = await axiosInstance.post(`/quizzes/teacher/${userId}/activate-all`);
            console.log('Tüm quizleri aktifleştirme yanıtı:', response.data);
            
            // Update all quizzes to active
            setQuizzes(quizzes.map(quiz => ({ ...quiz, isActive: true })));
            
            const activatedCount = response.data.activatedCount;
            if (activatedCount > 0) {
                alert(`${activatedCount} quiz başarıyla aktifleştirildi!`);
            } else {
                alert('Tüm quizleriniz zaten aktif durumda.');
            }
        } catch (err) {
            console.error('Quizler aktifleştirilirken hata:', err);
            alert('Quizler aktifleştirilirken bir hata oluştu: ' + (err.response?.data?.message || err.message));
        }
    };

    // Data fetching effect
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);
                
                // Get current user profile
                const userResponse = await axiosInstance.get('/users/profile');
                if (!userResponse.data || userResponse.data.role !== 'ROLE_TEACHER') {
                    setError('Bu sayfaya erişim için öğretmen yetkisi gerekiyor.');
                    return;
                }
                
                const teacherId = userResponse.data.id;
                setUserId(teacherId);
                
                // Fetch teacher's quizzes
                console.log(`Öğretmen quizleri getiriliyor - Öğretmen ID: ${teacherId}`);
                const quizzesResponse = await axiosInstance.get(`/quizzes/teacher/${teacherId}`);
                console.log('Alınan quizler:', quizzesResponse.data);
                
                // Set quizzes data
                setQuizzes(quizzesResponse.data || []);
                
                // Automatically activate all quizzes
                console.log('Tüm quizler otomatik olarak aktifleştiriliyor...');
                await axiosInstance.post(`/quizzes/teacher/${teacherId}/activate-all`);
                
                // Update quizzes list to show all as active
                if (quizzesResponse.data && quizzesResponse.data.length > 0) {
                    setQuizzes(quizzesResponse.data.map(quiz => ({ ...quiz, isActive: true })));
                }
            } catch (err) {
                setError('Quizler yüklenirken bir hata oluştu.');
                console.error('Quizler yüklenirken hata:', err);
            } finally {
                setLoading(false);
            }
        };
        
        fetchData();
    }, []);
    
    // Redirect effect when no quizzes
    useEffect(() => {
        if (!loading && !error && quizzes.length === 0) {
            const redirectTimer = setTimeout(() => {
                console.log('Quiz bulunamadığı için yönlendiriliyor...');
                navigate('/teacher/create-quiz');
            }, 5000);
            
            return () => clearTimeout(redirectTimer);
        }
    }, [loading, error, quizzes.length, navigate]);

    // Loading state
    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    // Error state
    if (error) {
        return (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
                <strong className="font-bold">Hata! </strong>
                <span className="block sm:inline">{error}</span>
            </div>
        );
    }

    // Main render    
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
                                <span>Quiz Oluştur</span>
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
                                    background: '#e9ecef',
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
                <div className="container mx-auto">
                    <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Quizlerim</h1>
                <div className="flex space-x-2">
                    {quizzes.length > 0 && (
                        <button 
                            onClick={handleActivateAllQuizzes}
                            className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded flex items-center"
                        >
                            <span className="mr-1">✓</span> Tüm Quizleri Aktifleştir
                        </button>
                    )}
                    <button 
                        onClick={handleCreateQuiz}
                        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
                    >
                        Yeni Quiz Oluştur
                    </button>
                </div>
            </div>

            {quizzes.length === 0 ? (
                <div className="bg-yellow-100 border-l-4 border-yellow-500 text-yellow-700 p-4 flex flex-col items-center" role="alert">
                    <p className="text-lg font-semibold mb-4">Henüz hiç quiz oluşturmamışsınız!</p>
                    <p className="mb-6">Hemen ilk quizinizi oluşturarak öğrencileriniz için interaktif sınavlar hazırlamaya başlayın.</p>
                    <button 
                        onClick={handleCreateQuiz}
                        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg shadow-lg transition duration-300 transform hover:scale-105"
                    >
                        Şimdi İlk Quizimi Oluştur
                    </button>
                </div>
            ) : (
                <div className="overflow-x-auto">
                    <table className="min-w-full bg-white border border-gray-200">
                        <thead>
                            <tr>
                                <th className="py-2 px-4 border-b">Quiz Adı</th>
                                <th className="py-2 px-4 border-b">Konu</th>
                                <th className="py-2 px-4 border-b">Açıklama</th>
                                <th className="py-2 px-4 border-b">Soru Sayısı</th>
                                <th className="py-2 px-4 border-b">Durum</th>
                                <th className="py-2 px-4 border-b">İşlemler</th>
                            </tr>
                        </thead>
                        <tbody>
                            {quizzes.map((quiz) => (
                                <tr key={quiz.id} className="hover:bg-gray-50">
                                    <td className="py-2 px-4 border-b">{quiz.name}</td>
                                    <td className="py-2 px-4 border-b">{quiz.topic || 'Belirtilmemiş'}</td>
                                    <td className="py-2 px-4 border-b">{quiz.description}</td>
                                    <td className="py-2 px-4 border-b">{quiz.questionCount || 0}</td>
                                    <td className="py-2 px-4 border-b">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${quiz.isActive ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                                            {quiz.isActive ? 'Aktif' : 'Pasif'}
                                        </span>
                                    </td>
                                    <td className="py-2 px-4 border-b">
                                        <div className="flex space-x-2">
                                            <button 
                                                onClick={() => handleViewQuestions(quiz.id)}
                                                className="bg-blue-500 hover:bg-blue-700 text-white text-xs py-1 px-2 rounded"
                                                title="Quizin sorularını görüntüle"
                                            >
                                                Soruları Gör
                                            </button>
                                            <button 
                                                onClick={() => handleEditQuiz(quiz.id)}
                                                className="bg-yellow-500 hover:bg-yellow-700 text-white text-xs py-1 px-2 rounded"
                                                title="Quizi düzenle"
                                            >
                                                Düzenle
                                            </button>
                                            <button 
                                                onClick={() => handleDeleteQuiz(quiz.id)}
                                                className="bg-red-500 hover:bg-red-700 text-white text-xs py-1 px-2 rounded"
                                                title="Quizi sil"
                                            >
                                                Sil
                                            </button>
                                            {!quiz.isActive && (
                                                <button 
                                                    onClick={() => handleActivateQuiz(quiz.id)}
                                                    className="bg-green-500 hover:bg-green-700 text-white text-xs py-1 px-2 rounded"
                                                    title="Quizi aktifleştir"
                                                >
                                                    Aktifleştir
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
                )}
                </div>
            </div>
        </div>
    );
}

export default TeacherMyQuizzesPage;