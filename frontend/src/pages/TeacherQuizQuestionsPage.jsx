import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function TeacherQuizQuestionsPage() {
    const { quizId } = useParams();
    const navigate = useNavigate();
    const [quiz, setQuiz] = useState(null);
    const [questions, setQuestions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Fetch quiz and questions data
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);
                
                // Fetch quiz details
                console.log(`Quiz ID ${quizId} için detaylar getiriliyor...`);
                const quizResponse = await axiosInstance.get(`/quizzes/${quizId}`);
                setQuiz(quizResponse.data);
                
                // Fetch questions for this quiz
                console.log(`Quiz ID ${quizId} için sorular getiriliyor...`);
                const questionsResponse = await axiosInstance.get(`/quizzes/${quizId}/questions`);
                
                // Sort questions by their number property to ensure consistent order
                const sortedQuestions = [...(questionsResponse.data || [])].sort((a, b) => {
                    if (a.number !== b.number) return a.number - b.number;
                    return a.id - b.id;
                });
                
                setQuestions(sortedQuestions);
                console.log('Getirilen sorular (sıralandı):', sortedQuestions);
                
            } catch (err) {
                console.error('Veri yüklenirken hata:', err);
                
                if (err.response && err.response.status === 401) {
                    setError('Bu quize erişim yetkiniz bulunmamaktadır. Sadece oluşturduğunuz quizlerin sorularını görüntüleyebilirsiniz.');
                } else if (err.response && err.response.status === 404) {
                    setError('Quiz bulunamadı. Silinmiş veya mevcut olmayan bir quize erişmeye çalışıyorsunuz.');
                } else {
                    setError('Quiz veya sorular yüklenirken bir hata oluştu: ' + (err.response?.data?.message || err.message));
                }
            } finally {
                setLoading(false);
            }
        };
        
        fetchData();
    }, [quizId]);
    
    const handleBackToQuizzes = () => {
        navigate('/teacher/my-quizzes');
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
            <div style={{ width: '250px', backgroundColor: 'white', boxShadow: '0 0 10px rgba(0,0,0,0.1)' }}>
                <div style={{ padding: '20px', borderBottom: '1px solid #eaeaea' }}>
                    <h3 style={{ fontSize: '1.2rem', fontWeight: '600', color: '#333' }}>Öğretmen Paneli</h3>
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
                <div>
                    <button onClick={handleBackToQuizzes} className="text-blue-500 hover:underline flex items-center">
                        <span className="mr-1">←</span> Quizlerime Geri Dön
                    </button>
                    {quiz && (
                        <h1 className="text-2xl font-bold mt-2">{quiz.name} - Sorular</h1>
                    )}
                </div>
            </div>
            
            {questions.length > 0 ? (
                <div className="bg-white shadow overflow-hidden rounded-lg">
                    <ul className="divide-y divide-gray-200">
                        {questions.map((question) => (
                            <li key={question.id} className="p-4 hover:bg-gray-50">
                                <div className="flex items-start">
                                    <div className="flex-shrink-0 bg-blue-100 text-blue-800 font-bold rounded-full w-8 h-8 flex items-center justify-center">
                                        {question.number}
                                    </div>
                                    <div className="ml-4 w-full">
                                        <div className="flex justify-between items-start mb-2">
                                            <div className="text-lg font-medium text-gray-900 whitespace-pre-line break-words">
                                                {question.questionSentence.split('\n').map((line, i) => (
                                                    <React.Fragment key={i}>
                                                        {line}
                                                        <br />
                                                    </React.Fragment>
                                                ))}
                                            </div>
                                            <div className="flex items-center space-x-2">
                                                <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs font-medium rounded-md">
                                                    {question.type?.typeName || question.questionType || 'Belirtilmemiş'}
                                                </span>
                                                <span className="px-2 py-1 bg-green-100 text-green-800 text-xs font-medium rounded-md">
                                                    {question.points} Puan
                                                </span>
                                            </div>
                                        </div>
                                        
                                        {question.options && question.options.length > 0 ? (
                                            <div className="mt-3 bg-gray-50 p-3 rounded-md">
                                                <h4 className="text-sm font-medium text-gray-500 mb-2">CEVAP SEÇENEKLERİ</h4>
                                                <ul className="space-y-2">
                                                    {question.options.map((option) => (
                                                        <li key={option.id} 
                                                            className={`p-2 rounded-md ${option.isCorrect 
                                                                ? 'bg-green-100 text-green-800' 
                                                                : 'bg-gray-100 text-gray-800'}`}>
                                                            <div className="flex items-center">
                                                                <div className={`w-4 h-4 mr-2 rounded-full ${option.isCorrect ? 'bg-green-500' : 'bg-gray-300'}`}></div>
                                                                <span>{option.text}</span>
                                                                {option.isCorrect && (
                                                                    <span className="ml-auto text-xs font-medium text-green-800">
                                                                        Doğru Cevap
                                                                    </span>
                                                                )}
                                                            </div>
                                                        </li>
                                                    ))}
                                                </ul>
                                            </div>
                                        ) : (question.type?.typeName === "Açık Uçlu" || question.questionType === "Açık Uçlu") ? (
                                            <div className="mt-3 bg-gray-50 p-3 rounded-md">
                                                <h4 className="text-sm font-medium text-gray-500 mb-2">AÇIK UÇLU SORU</h4>
                                                <p className="italic text-gray-600">Bu soru için öğrencilerin yazdığı cevaplar manuel olarak değerlendirilecektir.</p>
                                            </div>
                                        ) : null}
                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>
            ) : (
                <div className="bg-white shadow overflow-hidden rounded-lg p-8 text-center">
                    <div className="flex flex-col items-center">
                        <h3 className="text-lg font-medium text-gray-900 mb-2">Bu quiz için henüz soru bulunmuyor</h3>
                        <p className="text-sm text-gray-500 mb-4">
                            Quiz başarıyla oluşturulmuş, ancak içerisine henüz soru eklenmemiş.
                        </p>
                        <div className="mt-2">
                            <button 
                                onClick={handleBackToQuizzes}
                                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                            >
                                Quizlerime Geri Dön
                            </button>
                        </div>
                    </div>
                </div>
            )}
                </div>
            </div>
        </div>
    );
}

export default TeacherQuizQuestionsPage;
