import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { CheckCircleIcon, XCircleIcon } from '@heroicons/react/24/outline';

function QuizResultsPage() {
    const { attemptId } = useParams();
    const [results, setResults] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const fetchResults = useCallback(async () => {
        if (!attemptId) return;
        
        try {
            setLoading(true);
            setError(null);
            console.log('Fetching detailed results for attempt:', attemptId);
            
            const response = await axiosInstance.get(`/student/quiz-attempts/${attemptId}/details`);
            
            console.log('=== API RESPONSE ===');
            console.log('Full response:', response);
            console.log('Response data keys:', Object.keys(response.data));
            console.log('Quiz data:', response.data.quizName);
            console.log('Topic:', response.data.topic);
            console.log('Description:', response.data.description);
            console.log('timeSpentSeconds:', response.data.timeSpentSeconds);
            console.log('completionDate:', response.data.completionDate);
            console.log('Question results count:', response.data.questionResults?.length);
            console.log('All questions:', response.data.questionResults);
            
            const questionTypes = response.data.questionResults?.map(q => q.questionType) || [];
            console.log('Question types:', [...new Set(questionTypes)]);
            
            const mcqExample = response.data.questionResults?.find(q => q.questionType === 'MULTIPLE_CHOICE');
            console.log('Multiple choice example:', mcqExample);
            
            const openEndedExample = response.data.questionResults?.find(q => q.questionType === 'OPEN_ENDED');
            console.log('Open-ended example:', openEndedExample);
            console.log('==================');
            
            setResults(response.data);
        } catch (err) {
            const errorMessage = err.response?.data?.message || 'Sonuçlar yüklenirken bir hata oluştu.';
            setError(errorMessage);
            console.error('Sonuçlar yüklenirken hata:', {
                message: err.message,
                status: err.response?.status,
                data: err.response?.data
            });
            
            if (err.response?.status === 401) {
                console.log('Unauthorized, redirecting to login...');
                navigate('/login', { replace: true });
            }
        } finally {
            setLoading(false);
        }
    }, [attemptId, navigate]);

    useEffect(() => {
        if (attemptId) {
            fetchResults();
        } else {
            setError('Geçersiz sınav denemesi ID\'si');
            setLoading(false);
        }
    }, [attemptId, fetchResults]);

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }


    if (error) {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
                    <strong className="font-bold">Hata! </strong>
                    <span className="block sm:inline">{error}</span>
                    <button 
                        onClick={() => navigate(-1)}
                        className="mt-2 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                    >
                        Geri Dön
                    </button>
                </div>
            </div>
        );
    }

    if (!results) {
        return (
            <div className="container mx-auto px-4 py-8 text-center">
                <div className="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded relative">
                    Sonuç bulunamadı.
                </div>
                <button 
                    onClick={() => navigate(-1)}
                    className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                    Geri Dön
                </button>
            </div>
        );
    }

    const scorePercentage = Math.round((results.correctAnswers / results.totalQuestions) * 100);
    const isPassed = scorePercentage >= results.passingScore;

    const renderMultipleChoiceQuestion = (question, index) => {
        const isCorrect = question.isCorrect;
        const options = Array.isArray(question.options) ? question.options : [];
        const selectedOptionIds = Array.isArray(question.selectedOptionIds) ? question.selectedOptionIds : [];
        
    
        const selectedOptions = selectedOptionIds
            .map(id => options.find(opt => opt.id === id))
            .filter(Boolean);

        
        const correctOption = options.find(opt => opt.correct);
        
        console.log(`Soru ${index + 1} detayları:`, {
            question,
            selectedOptionIds,
            selectedOptions: selectedOptions.map(opt => ({
                id: opt.id,
                text: opt.text.replace(/Doğru Cevap:?/g, '').trim()
            })),
            correctOption: correctOption ? { id: correctOption.id, text: correctOption.text.replace(/Doğru Cevap:?/g, '').trim() } : null
        });
        
        return (
            <div key={question.questionId} className="mb-6 p-4 border rounded-lg">
                <h3 className="text-lg font-medium mb-3">Soru {index + 1}: {question.questionText}</h3>
                
                <div className="ml-4">
                    <p className="text-sm font-medium text-gray-700 mb-2">Seçenekler:</p>
                    {options.length > 0 ? (
                        <ul className="space-y-1">
                            {options.map((option) => {
                                const cleanText = option.text
                                    .replace(/Doğru Cevap:?/g, '')
                                    .trim();
                                const isSelected = selectedOptionIds.includes(option.id);
                                const isCorrectOption = option.correct;
                                    
                                return (
                                    <li key={option.id} className="py-1">
                                        {isSelected ? (
                                            <span className="font-medium text-blue-600">
                                                {cleanText} <span className="text-gray-500 text-xs">(Seçilen cevap)</span>
                                            </span>
                                        ) : (
                                            <span>{cleanText}</span>
                                        )}
                                        {isCorrectOption && (
                                            <span className="ml-2 px-2 py-0.5 bg-green-200 text-green-800 text-xs font-semibold rounded-full">
                                                Doğru Cevap
                                            </span>
                                        )}
                                    </li>
                                );
                            })}
                        </ul>
                    ) : (
                        <p className="text-sm text-gray-500 italic">Bu soru için şıklar bulunamadı.</p>
                    )}
                </div>
                
                {correctOption && (
                    <div className="mt-4 p-2 bg-green-100 border border-green-300 rounded-md">
                        <p className="text-sm font-semibold text-green-800">Doğru Cevap: {correctOption.text.replace(/Doğru Cevap:?/g, '').trim()}</p>
                    </div>
                )}

                <div className="mt-2 text-sm text-gray-600">
                    Puan: {question.earnedPoints} / {question.maxPoints}
                </div>
            </div>
        );
    };
    
    const renderOpenEndedQuestion = (question, index) => {
        const isCorrect = question.isCorrect;
        
        console.log(`Open-ended ${index + 1}:`, {
            questionId: question.questionId,
            questionText: question.questionText,
            submittedTextAnswer: question.submittedTextAnswer,
            isCorrect: question.isCorrect,
            earnedPoints: question.earnedPoints,
            maxPoints: question.maxPoints,
            correctAnswerText: question.correctAnswerText,
            aiExplanation: question.aiExplanation,
            aiScore: question.aiScore
        });

        return (
            <div key={question.questionId} className="mb-6 p-4 border rounded-lg shadow-sm">
                <div className="flex items-start mb-3">
                    {isCorrect ? (
                        <CheckCircleIcon className="h-5 w-5 text-green-500 mr-2 mt-0.5 flex-shrink-0" />
                    ) : (
                        <XCircleIcon className="h-5 w-5 text-red-500 mr-2 mt-0.5 flex-shrink-0" />
                    )}
                    <h3 className="text-lg font-medium">Soru {index + 1}: {question.questionText}</h3>
                </div>
                
                <div className="ml-7">
                    <p className="text-sm font-medium text-gray-700 mb-2">Cevabınız:</p>
                    <div className="p-3 bg-gray-100 rounded border border-gray-200">
                        {question.submittedTextAnswer ? (
                            <p className="whitespace-pre-wrap">{question.submittedTextAnswer}</p>
                        ) : (
                            <p className="text-gray-500 italic">Cevap verilmedi</p>
                        )}
                    </div>
                    
                    {question.correctAnswerText && question.correctAnswerText !== 'Belirtilmemiş' && (
                        <div className="mb-4">
                            <p className="text-sm font-medium text-gray-700 mb-1">Doğru Cevap:</p>
                            <div className="p-3 bg-green-50 rounded border border-green-200">
                                <p className="whitespace-pre-wrap text-green-800">{question.correctAnswerText}</p>
                            </div>
                        </div>
                    )}
                    
                    {question.aiExplanation && (
                        <div className="mb-4">
                            <p className="text-sm font-medium text-gray-700 mb-1">AI Değerlendirmesi:</p>
                            <div className="p-3 bg-blue-50 rounded border border-blue-200">
                                <p className="whitespace-pre-wrap text-blue-800">{question.aiExplanation}</p>
                                {question.aiScore !== null && (
                                    <div className="mt-2 text-sm font-medium text-blue-700 bg-blue-100 px-2 py-1 rounded inline-block">
                                        AI Puanı: {question.aiScore} / {question.maxPoints}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>
                
                <div className="mt-3 text-sm font-medium text-gray-700 px-2 py-1 bg-gray-50 rounded inline-block">
                    Puan: {question.earnedPoints} / {question.maxPoints}
                </div>
            </div>
        );
    };
    
    const renderQuestionResult = (question, index) => {
        
        try {
            if (question.questionType === 'MULTIPLE_CHOICE') {
                return renderMultipleChoiceQuestion(question, index);
            } else {
                return renderOpenEndedQuestion(question, index);
            }
        } catch (error) {
            console.error('Error rendering question:', error);
            return (
                <div key={`error-${index}`} className="mb-6 p-4 border border-red-300 rounded-lg bg-red-50">
                    <p className="text-red-700">Bu soru gösterilirken bir hata oluştu: {error.message}</p>
                    <pre className="mt-2 text-xs overflow-auto bg-red-100 p-2 rounded">
                        {JSON.stringify(question, null, 2)}
                    </pre>
                </div>
            );
        }
    };

    const sortedQuestionResults = results.questionResults ? 
        [...results.questionResults].sort((a, b) => {
            if (a.number !== undefined && a.number !== null && b.number !== undefined && b.number !== null) {
                return a.number - b.number;
            }
            return a.questionId - b.questionId;
        }) : 
        [];

    return (
        <div style={{ display: 'flex', minHeight: '100vh' }}>
            <div style={{
                width: '250px',
                backgroundColor: '#f8f9fa',
                borderRight: '1px solid #dee2e6',
                padding: '20px 0',
                flexShrink: 0
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
                                <span>Duyurular</span> 
                            </button>
                        </li>
                    </ul>
                </nav>
            </div>
            
            <div style={{ flex: 1, padding: '20px', overflowY: 'auto' }}>
                <div className="max-w-4xl mx-auto">
                    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                        <h1 className="text-2xl font-bold text-gray-800 mb-4">{results.quizName || 'Sınav Detayları'}</h1>
                        
                        <div className="border-t border-b border-gray-200 py-4 mb-6">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div>
                                    <h3 className="text-sm font-medium text-gray-500 mb-2">Sınav Adı</h3>
                                    <p className="text-gray-900 font-medium">
                                        {results.quizName || 'Bilinmiyor'}
                                    </p>
                                </div>
                                
                                <div>
                                    <h3 className="text-sm font-medium text-gray-500 mb-2">Öğretmen</h3>
                                    <p className="text-gray-900">
                                        {results.teacherName || 'Bilinmiyor'}
                                    </p>
                                </div>
                                
                                <div>
                                    <h3 className="text-sm font-medium text-gray-500 mb-2">Harcanan Süre</h3>
                                    <p className="text-gray-900">
                                        {results.timeSpentSeconds ? 
                                            `${Math.floor(results.timeSpentSeconds / 60)} dakika ${results.timeSpentSeconds % 60} saniye` : 
                                            'Bilgi yok'}
                                    </p>
                                </div>
                                
                                <div>
                                    <h3 className="text-sm font-medium text-gray-500 mb-2">Tamamlanma Zamanı</h3>
                                    <p className="text-gray-900">
                                        {results.completionDate ? 
                                            new Date(results.completionDate).toLocaleString('tr-TR') : 
                                            'Tamamlanmadı'}
                                    </p>
                                </div>
                                
                                <div>
                                    <h3 className="text-sm font-medium text-gray-500 mb-2">Konu</h3>
                                    <p className="text-gray-900">
                                        {results.topic || 'Belirtilmemiş'}
                                    </p>
                                </div>
                                
                            </div>
                            {results.description && (
                                <div className="md:col-span-2 mt-4">
                                    <h3 className="text-sm font-medium text-gray-500 mb-2">Açıklama</h3>
                                    <div className="p-4 bg-gray-50 rounded-md">
                                        <p className="text-gray-800 whitespace-pre-wrap">
                                            {results.description}
                                        </p>
                                    </div>
                                </div>
                            )}
                        </div>
                        
                        <div className="mt-8">
                            <h2 className="text-xl font-semibold mb-4">Soru Detayları</h2>
                            
                            <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <p className="text-sm text-gray-600">Toplam Soru:</p>
                                        <p className="font-medium">{results.questionResults?.length || 0}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-600">Toplam Puan:</p>
                                        <p className="font-medium">{results.earnedPoints} / {results.totalPoints}</p>
                                    </div>
                                </div>
                            </div>
                            
                            <div className="space-y-6">
                                {sortedQuestionResults.length > 0 ? (
                                    sortedQuestionResults.map((question, index) => renderQuestionResult(question, index))
                                ) : (
                                    <div className="text-center py-8 bg-gray-50 rounded-lg">
                                        <p className="text-gray-500">Sınav sonuç detayları bulunamadı.</p>
                                    </div>
                                )}
                            </div>
                        </div>
                        
                        <div className="mt-8 flex justify-center">
                            <button
                                onClick={() => navigate('/student/solve-quiz')}
                                className="px-6 py-3 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors"
                            >
                                Yeni Quiz Çöz
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default QuizResultsPage;
