import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function StudentQuizzesPage() {
    const [quizzes, setQuizzes] = useState([]);
    const [filteredQuizzes, setFilteredQuizzes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedTeacher, setSelectedTeacher] = useState('all');
    const navigate = useNavigate();

    useEffect(() => {
        const fetchQuizzes = async () => {
            try {
                setLoading(true);
                setError(null); // Hata durumunu sıfırla
                console.log('Quizler yükleniyor...');
                const response = await axiosInstance.get('/student/quizzes');
                console.log('Quizler başarıyla yüklendi:', response.data);
                
                // Debug: Log the attempted status of each quiz
                response.data.forEach(quiz => {
                    console.log(`Quiz ID: ${quiz.id}, Attempted: ${quiz.attempted}, Name: ${quiz.name}`);
                });
                
                setQuizzes(response.data);
                setFilteredQuizzes(response.data);
            } catch (err) {
                const errorMessage = err.response?.data?.message || 'Quizler yüklenirken bir hata oluştu.';
                setError(errorMessage);
                console.error('Quizler yüklenirken hata:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchQuizzes();
    }, []);

    const handleQuizClick = (quizId) => {
        navigate(`/solve-quiz/${quizId}`);
    };

    const formatTime = (date) => {
        return new Date(date).toLocaleDateString('tr-TR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    // Filter quizzes based on search term, selected teacher, and if attempted
    useEffect(() => {
        console.log('Filtering quizzes...');
        let result = [...quizzes];
        
        // Debug: Log all quizzes before filtering
        console.log('All quizzes before filtering:', result.map(q => ({
            id: q.id,
            name: q.name,
            attempted: q.attempted,
            teacher: q.teacher?.name
        })));
        
        // Filter out attempted quizzes
        result = result.filter(quiz => {
            const shouldShow = !quiz.attempted;
            console.log(`Quiz ID ${quiz.id} (${quiz.name}): attempted=${quiz.attempted}, showing=${shouldShow}`);
            return shouldShow;
        });
        
        // Filter by search term
        if (searchTerm) {
            const term = searchTerm.toLowerCase();
            result = result.filter(quiz => 
                quiz.name.toLowerCase().includes(term) || 
                (quiz.teacher?.name?.toLowerCase() || '').includes(term) ||
                (quiz.description?.toLowerCase() || '').includes(term)
            );
        }
        
        // Filter by teacher
        if (selectedTeacher !== 'all') {
            result = result.filter(quiz => quiz.teacher?.id?.toString() === selectedTeacher);
        }
        
        console.log('Filtered quizzes:', result);
        setFilteredQuizzes(result);
    }, [searchTerm, selectedTeacher, quizzes]);

    // Get unique teachers for filter
    const teachers = [...new Set(quizzes.map(quiz => JSON.stringify({
        id: quiz.teacher.id,
        name: quiz.teacher.name
    })))].map(JSON.parse);

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
    
    if (quizzes.length === 0) {
        return (
            <div className="container mx-auto px-4 py-8">
                <h1 className="text-2xl font-bold mb-6">Mevcut Sınavlar</h1>
                <div className="bg-yellow-100 border-l-4 border-yellow-500 text-yellow-700 p-4" role="alert">
                    <p className="font-bold">Henüz aktif sınav bulunmamaktadır.</p>
                    <p>Lütfen daha sonra tekrar kontrol ediniz.</p>
                </div>
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
                        {localStorage.getItem('userName') || 'Öğrenci'}
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
                                <span>Duyurular</span> {/* Added closing span for "Duyurular" */}
                            </button>
                        </li>
                    </ul>
                </nav>
            </div>
            
            {/* Main Content */}
            <div style={{ flex: 1, padding: '20px', overflowY: 'auto' }}>
                <h1 className="text-3xl font-bold mb-6">Çözülebilir Quizler</h1>
                
                {/* Search and Filter Section */}
                <div className="bg-white p-4 rounded-lg shadow-md mb-6">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Ara</label>
                            <input
                                type="text"
                                placeholder="Quiz veya öğretmen ara..."
                                className="w-full p-2 border rounded-md"
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Öğretmene Göre Filtrele</label>
                            <select
                                className="w-full p-2 border rounded-md"
                                value={selectedTeacher}
                                onChange={(e) => setSelectedTeacher(e.target.value)}
                            >
                                <option value="all">Tüm Öğretmenler</option>
                                {teachers.map((teacher) => (
                                    <option key={teacher.id} value={teacher.id}>
                                        {teacher.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="flex items-end">
                            <button
                                onClick={() => {
                                    setSearchTerm('');
                                    setSelectedTeacher('all');
                                }}
                                className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                            >
                                Filtreleri Temizle
                            </button>
                        </div>
                    </div>
                </div>
                
                {filteredQuizzes.length === 0 ? (
                    <div className="text-center py-10">
                        <p className="text-gray-600">Şu anda aktif quiz bulunmamaktadır.</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {filteredQuizzes.map((quiz) => (
                            <div
                                key={quiz.id}
                                className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow flex flex-col h-full"
                            >
                                <div className="flex-grow">
                                    <h2 className="text-xl font-semibold mb-2">{quiz.name}</h2>
                                    {quiz.topic && (
                                        <p className="text-gray-700 font-medium mb-1">
                                            <span className="text-gray-600">Konu:</span> {quiz.topic}
                                        </p>
                                    )}
                                    {quiz.description && (
                                        <p className="text-gray-600 text-sm mb-3 line-clamp-2">
                                            {quiz.description}
                                        </p>
                                    )}
                                    <div className="border-t border-gray-100 pt-2 mt-2">
                                        <p className="text-gray-600 text-sm">Öğretmen: {quiz.teacher.name}</p>
                                        <p className="text-gray-600 text-sm">Soru Sayısı: {quiz.questionCount}</p>
                                        <p className="text-gray-600 text-sm">Süre: {quiz.durationMinutes} dakika</p>
                                    </div>
                                </div>
                                <div className="mt-4">
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            navigate(`/solve-quiz/${quiz.id}`);
                                        }}
                                        className="w-full bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded-md transition-colors"
                                    >
                                        Quiz'i Çöz
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default StudentQuizzesPage;
