import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance'; // axiosInstance.js dosyanızın doğru olduğundan emin olun

function SolveQuizPage() {
    const { quizId } = useParams();
    const navigate = useNavigate();
    const token = localStorage.getItem('token'); // Token'ı component mount olduğunda al

    // States
    const [quiz, setQuiz] = useState(null);
    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    const [selectedAnswers, setSelectedAnswers] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [timeRemaining, setTimeRemaining] = useState(0);
    const [isSubmitted, setIsSubmitted] = useState(false);
    const [quizStartTime, setQuizStartTime] = useState(null);
    const [results, setResults] = useState(null);
    const [sessionId, setSessionId] = useState(null); // Quiz oturum kimliğini tutacak state
    const [textAnswers, setTextAnswers] = useState({}); // Açık uçlu soru cevapları için state

    // Check authentication and redirect if not logged in
    useEffect(() => {
        if (!token) {
            setError('Lütfen önce giriş yapın.');
            // navigate('/login', { state: { from: `/solve-quiz/${quizId}` } }); // Kullanıcıyı geri yönlendirmek için
            navigate('/login'); // Basitçe login sayfasına yönlendir
        }
    }, [token, navigate, quizId]); // quizId'yi de ekledik

    // Load quiz data and questions
    useEffect(() => {
        // Token yoksa quiz yüklemeyi deneme, çünkü yetkisiz olacak.
        if (!token) {
            setLoading(false);
            return;
        }

        const loadQuizData = async () => {
            try {
                setLoading(true);
                setError(null);

                // 1. Start the quiz session and get all data at once
                // axiosInstance global interceptor sayesinde token'ı otomatik ekleyecek.
                const response = await axiosInstance.post(`/student/quiz-sessions/start/${quizId}`);
                
                const responseData = response.data;
                
                // Debug: Log the raw quiz data from the API
                console.log('Raw response data from API:', responseData);

                // Extract session data from the response
                // The response has the format: { success: true, message: '...', session: { sessionId: X, quiz: {...} } }
                const sessionData = responseData.data?.session || responseData.session;
                // The session ID is in sessionData.sessionId, not sessionData.id
                const sessionId = sessionData?.sessionId || sessionData?.id;
                const quizData = sessionData?.quiz;
                
                console.log('Session data structure:', sessionData);
                console.log('Extracted sessionId:', sessionId);
                console.log('Extracted quizData:', quizData);
                
                console.log('Setting sessionId:', sessionId);
                console.log('Session data:', sessionData);
                console.log('Quiz data:', quizData);
                
                if (!sessionId || !quizData) {
                    console.error('Invalid response format:', responseData);
                    throw new Error('Quiz oturumu başlatılamadı: Eksik oturum veya quiz verisi');
                }
                
                setSessionId(sessionId);
                
                // 2. Show error if quiz is not active
                if (!quizData.active) {
                    throw new Error('Bu quiz şu anda aktif değil.');
                }

                // 3. Check if quiz has questions
                if (!quizData.questions || quizData.questions.length === 0) {
                    setQuiz({
                        ...quizData,
                        questions: [],
                        hasNoQuestions: true
                    });
                    return;
                }

                // 4. Ensure questions have the correct structure for rendering
                const processedQuestions = quizData.questions.map(question => {
                    // Set default points if not provided
                    const questionWithPoints = {
                        ...question,
                        points: question.points || 1 // Default to 1 point if not provided
                    };
                    
                    // Handle backend format differences (options vs answers)
                    if (!questionWithPoints.options && questionWithPoints.answers) {
                        questionWithPoints.options = questionWithPoints.answers;
                    }
                    
                    return questionWithPoints;
                });

                // 5. Set the quiz data with processed questions
                console.log('Processed quiz data:', {
                    ...quizData,
                    questions: processedQuestions
                });
                setQuiz({
                    ...quizData,
                    questions: processedQuestions
                });

                // 6. Set timer if quiz has a duration
                if (quizData.durationMinutes) {
                    setTimeRemaining(quizData.durationMinutes * 60);
                    setQuizStartTime(new Date());
                } else {
                    setTimeRemaining(60 * 60); // Default 60 minutes if no duration
                    setQuizStartTime(new Date());
                }

            } catch (err) {
                console.error('Quiz yüklenirken hata oluştu:', err);

                if (err.response?.status === 401) {
                    setError('Oturumunuz sona erdi veya yetkiniz yok. Lütfen tekrar giriş yapın.');
                   /* navigate('/login', {
                        state: {
                            from: `/solve-quiz/${quizId}`,
                            message: 'Oturumunuz sona erdi. Lütfen tekrar giriş yapın.'
                        }
                    });*/
                } else if (err.response?.data) {
                    // Handle case where response data might be an object with a message property
                    const errorData = err.response.data;
                    const errorMessage = typeof errorData === 'string' 
                        ? errorData 
                        : (errorData.message || JSON.stringify(errorData));
                        
                    if (errorMessage.includes('zaten çözmüş')) {
                        setError('Bu quizi zaten çözmüşsünüz.');
                    } else if (errorMessage.includes('çözülebilir durumda değil')) {
                        setError('Bu quiz şu anda çözülebilir durumda değil.');
                    } else if (errorMessage.includes('öğrenci değil')) {
                        setError('Bu işlem için öğrenci hesabına ihtiyacınız var.');
                    } else {
                        setError('Quiz yüklenirken bir hata oluştu: ' + errorMessage);
                    }
                } else {
                    setError('Quiz yüklenirken bir hata oluştu: ' + (err.message || 'Bilinmeyen hata'));
                }
            } finally {
                setLoading(false);
            }
        };

        // Eğer token varsa quiz verilerini yükle
        if (token) {
            loadQuizData();
        }
    }, [quizId, token, navigate]); // token'ı da bağımlılık olarak ekledik

    // Timer countdown effect
    useEffect(() => {
        if (!timeRemaining || isSubmitted || !quizStartTime) return; // quizStartTime ekledik

        const timer = setInterval(() => {
            setTimeRemaining(prevTime => {
                if (prevTime <= 1) {
                    clearInterval(timer);
                    handleSubmitQuiz(); // Auto-submit when time runs out
                    return 0;
                }
                return prevTime - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, [timeRemaining, isSubmitted, quizStartTime]); // quizStartTime'ı da ekledik

    // Format time remaining as MM:SS
    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    // Refresh token before submitting quiz (Güvenlik zafiyetini göz önünde bulundurarak!)
    const refreshToken = async () => {
        try {
            const response = await axiosInstance.post('/auth/refresh-token'); // Interceptor token'ı ekleyecek
            localStorage.setItem('token', response.data.token);
            console.log('Token başarıyla yenilendi');
            return true;
        } catch (error) {
            console.error('Token yenileme hatası:', error);
            // Burada kullanıcıyı login'e yönlendirmek en güvenlisi olabilir.
            // localStorage'dan username/password alma kısmı SİLİNMELİ.
            setError('Oturum yenileme başarısız. Lütfen tekrar giriş yapın.');
            return false;
        }
    };

    // Handle quiz submission
    const handleSubmitQuiz = async () => {
        console.log('handleSubmitQuiz called');
        if (isSubmitted) {
            console.log('Already submitted, returning');
            return;
        }
        if (!sessionId) {
            console.error('No sessionId available');
            setError('Oturum bilgisi bulunamadı. Lütfen sayfayı yenileyip tekrar deneyin.');
            return;
        }

        try {
            console.log('Setting isSubmitted and loading to true');
            setIsSubmitted(true);
            setLoading(true);
            
            // Prepare answers array with both multiple choice and open-ended answers
            const answers = [];
            
            console.log('Processing selectedAnswers:', selectedAnswers);
            // Process multiple choice answers
            Object.entries(selectedAnswers).forEach(([questionId, optionId]) => {
                if (optionId) { // Only add if there's a selected option
                    answers.push({
                        questionId: parseInt(questionId),
                        selectedOptionIds: [parseInt(optionId)],
                        answerType: 'MULTIPLE_CHOICE'
                    });
                }
            });
            
            console.log('Processing textAnswers:', textAnswers);
            // Process open-ended answers
            Object.entries(textAnswers).forEach(([questionId, textAnswer]) => {
                // Only add if there's actual text and not already added as multiple choice
                if (textAnswer && textAnswer.trim() !== '' && !answers.some(a => a.questionId === parseInt(questionId))) {
                    answers.push({
                        questionId: parseInt(questionId),
                        openEndedAnswer: textAnswer.trim(),
                        answerType: 'OPEN_ENDED'
                    });
                }
            });
            
            // Prepare the request payload according to backend expectations
            const payload = {
                quizSessionId: parseInt(sessionId),
                answers: answers.map(answer => {
                    if (answer.answerType === 'MULTIPLE_CHOICE') {
                        return {
                            questionId: answer.questionId,
                            selectedOptionIds: answer.selectedOptionIds
                        };
                    } else {
                        return {
                            questionId: answer.questionId,
                            openEndedAnswer: answer.openEndedAnswer
                        };
                    }
                })
            };
            
            console.log('Submitting quiz answers with payload:', JSON.stringify(payload, null, 2));
            
            try {
                console.log('Sending request to /student/quiz-sessions/complete');
                const response = await axiosInstance.post('/student/quiz-sessions/complete', payload, {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                    }
                });
                console.log('Response from server:', response.data);
                
                // If successful, navigate to quiz results list page
                if (response.data) {
                    console.log('Quiz completed successfully, navigating to results list');
                    navigate('/quiz-results');
                } else {
                    console.error('No data in response');
                }
                
                setResults(response.data);
                
            } catch (err) {
                console.error('Error submitting quiz:', {
                    message: err.message,
                    response: err.response?.data,
                    status: err.response?.status,
                    headers: err.response?.headers
                });

                if (err.response?.status === 401) {
                    const errorMsg = 'Oturumunuz sona erdi. Lütfen tekrar giriş yapın.';
                    console.error(errorMsg);
                    setError(errorMsg);
                } else {
                    const errorMessage = err.response?.data?.message || 'Quiz gönderilirken hata oluştu. Lütfen tekrar deneyin.';
                    console.error('Server error:', errorMessage);
                    setError(errorMessage);
                }
                
                // Eğer bir sonuç veya attemptId dönüyorsa, hata olsa bile sonuç sayfasına yönlendir
                if (err.response?.data?.attemptId) {
                    console.log('Navigating to results page with attemptId:', err.response.data.attemptId);
                    navigate(`/quiz-results/${err.response.data.attemptId}`);
                }
                
                // Allow retry on error
                console.log('Allowing retry after error');
                setIsSubmitted(false);
                // Don't re-throw as it's already handled
            }
            
        } catch (error) {
            console.error('Unexpected error in handleSubmitQuiz:', error);
            setError('Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.');
            setIsSubmitted(false);
        } finally {
            console.log('Setting loading to false');
            setLoading(false);
        }
    };

    // Diğer render kısımları aynı kalabilir.
    // ... (if (loading), if (error), if (isSubmitted && results), if (!quiz), if (quiz.hasNoQuestions) )

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-50">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mx-auto"></div>
                    <p className="mt-4 text-gray-600">Quiz yükleniyor...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="max-w-4xl mx-auto p-6 mt-10 bg-red-50 border border-red-200 rounded-lg text-center">
                <h2 className="text-2xl font-bold text-red-600 mb-2">Hata</h2>
                <p className="text-red-700 mb-4">{error}</p>
                <button
                    onClick={() => navigate('/student')}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                    Quiz Listesine Dön
                </button>
            </div>
        );
    }

    if (isSubmitted && results) {
        return (
            <div className="max-w-4xl mx-auto p-6 mt-10 bg-green-50 border border-green-200 rounded-lg text-center">
                <h2 className="text-2xl font-bold text-green-600 mb-2">Quiz Tamamlandı!</h2>
                <p className="text-green-700 mb-4">Puanınız: {results.score}%</p>
                <p className="text-green-700 mb-4">Doğru cevaplar: {results.correctAnswers} / {results.totalQuestions}</p>
                <button
                    onClick={() => navigate(`/quiz-results/${results.attemptId || sessionId}`)}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 mr-4"
                >
                    Detaylı Sonuçları Görüntüle
                </button>
                <button
                    onClick={() => navigate('/student')}
                    className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
                >
                    Panele Dön
                </button>
            </div>
        );
    }

    if (!quiz) {
        return (
            <div className="max-w-4xl mx-auto p-6 mt-10 bg-yellow-50 border border-yellow-200 rounded-lg text-center">
                <h2 className="text-2xl font-bold text-yellow-600 mb-2">Quiz Bulunamadı</h2>
                <p className="text-yellow-700 mb-4">Bu quiz artık mevcut değil veya erişim izniniz yok.</p>
                <button
                    onClick={() => navigate('/student')}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                    Quiz Listesine Dön
                </button>
            </div>
        );
    }

    if (quiz.hasNoQuestions) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
                <div className="bg-white p-6 rounded-lg shadow-md max-w-md w-full text-center">
                    <div className="text-yellow-500 text-5xl mb-4">
                        <i className="fas fa-info-circle"></i>
                    </div>
                    <h2 className="text-2xl font-bold text-gray-800 mb-2">Quiz Hazır Değil</h2>
                    <p className="text-gray-600 mb-6">Bu quizde henüz soru bulunmuyor.</p>
                    <button
                        onClick={() => navigate('/student/quizzes')}
                        className="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-6 rounded-lg transition duration-200"
                    >
                        Quiz Listesine Dön
                    </button>
                </div>
            </div>
        );
    }

    // Quiz verilerini ve mevcut soruyu al
    const currentQuestion = quiz.questions[currentQuestionIndex];
    const totalQuestions = quiz.questions.length;

    const handleNextQuestion = () => {
        if (currentQuestionIndex < quiz.questions.length - 1) {
            setCurrentQuestionIndex(prev => prev + 1);
        }
    };

    const handlePreviousQuestion = () => {
        if (currentQuestionIndex > 0) {
            setCurrentQuestionIndex(prev => prev - 1);
        }
    };

    // Handle answer selection for multiple choice questions
    const handleAnswerSelect = (questionId, optionId) => {
        setSelectedAnswers(prev => ({
            ...prev,
            [questionId]: optionId
        }));
    };

    // Handle text input for open-ended questions
    const handleTextAnswerChange = (questionId, text) => {
        setTextAnswers(prev => ({
            ...prev,
            [questionId]: text
        }));
    };

 

    return (
        <div className="min-h-screen bg-gray-50 py-8 px-4">
            <div className="flex max-w-6xl mx-auto gap-6">
                {/* Navigation Menu */}
                <div className="w-64 flex-shrink-0">
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
    
                {/* Quiz Content */}
                <div className="flex-1">
                    <div className="bg-white rounded-xl shadow-md overflow-hidden">
                        {/* Header */}
                        <div className="p-6 bg-gradient-to-r from-blue-500 to-blue-600 text-white">
                            <div className="flex flex-col space-y-4">
                                <div className="flex justify-between items-start">
                                    <div>
                                        <h1 className="text-2xl font-bold">{quiz.name}</h1>
                                        {quiz.topic && (
                                            <p className="text-blue-100 mt-1">
                                                <span className="font-medium">Konu:</span> {quiz.topic}
                                            </p>
                                        )}
                                        {quiz.description && (
                                            <p className="text-blue-100 text-sm mt-2">
                                                {quiz.description}
                                            </p>
                                        )}
                                    </div>
    
                                    {/* Timer Badge */}
                                    <div className="flex flex-col items-end gap-2">
                                        <div className="bg-white bg-opacity-20 backdrop-blur-sm rounded-lg p-3 text-center">
                                            <div className="text-xs text-blue-100 mb-1">KALAN SÜRE</div>
                                            <div className="text-2xl font-bold font-mono tracking-wider">
                                                {formatTime(timeRemaining)}
                                            </div>
                                        </div>
                                        <button
                                            onClick={handleSubmitQuiz}
                                            className="bg-white text-blue-600 hover:bg-blue-50 font-medium py-2 px-4 rounded-lg transition duration-200 w-full"
                                            disabled={isSubmitted}
                                        >
                                            {isSubmitted ? 'Gönderiliyor...' : 'Sınavı Bitir'}
                                        </button>
                                    </div>
                                </div>
    
                                {/* Question Progress */}
                                <div className="flex items-center justify-between bg-white bg-opacity-10 p-3 rounded-lg">
                                    <div className="flex items-center space-x-4 w-full">
                                        <span className="bg-white bg-opacity-20 px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap">
                                            Soru <span className="font-bold">{currentQuestionIndex + 1}</span> / {quiz.questions.length}
                                        </span>
                                        <div className="w-full bg-gray-200 bg-opacity-50 rounded-full h-3">
                                            <div
                                                className="bg-white h-3 rounded-full transition-all duration-300 ease-in-out"
                                                style={{ width: `${((currentQuestionIndex + 1) / quiz.questions.length) * 100}%` }}
                                            ></div>
                                        </div>
                                        <span className="text-sm font-medium whitespace-nowrap">
                                            %{Math.round(((currentQuestionIndex + 1) / quiz.questions.length) * 100)} Tamamlandı
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
    
                        {/* Question */}
                        <div className="p-6">
                            {currentQuestion && (
                                <>
                                    <div className="mb-6">
                                        <div className="flex justify-between items-center mb-4">
                                            <h2 className="text-xl font-semibold text-gray-800">
                                                {currentQuestion.questionText || currentQuestion.questionSentence || currentQuestion.text || currentQuestion.content || 'Soru metni yüklenemedi'}
                                            </h2>
                                            <div className="text-sm text-gray-600">
                                                {currentQuestion.points !== undefined
                                                    ? currentQuestion.points
                                                    : (currentQuestion.pointValue !== undefined
                                                        ? currentQuestion.pointValue
                                                        : 1)} Puan
                                            </div>
                                        </div>
    
                                        {currentQuestion.imageUrl && (
                                            <div className="my-4 max-w-full">
                                                <img
                                                    src={currentQuestion.imageUrl}
                                                    alt="Soru görseli"
                                                    className="max-h-64 mx-auto rounded-lg shadow-sm"
                                                />
                                            </div>
                                        )}
                                    </div>
    
                                    {/* Answer options section */}
                                    <div className="space-y-3 mb-8">
                                        {/* Multiple choice question - Backend'den gelen soru tipini kontrol et */}
                                        {(currentQuestion.questionTypeId === 1 || currentQuestion.questionType === 'MULTIPLE_CHOICE') && currentQuestion.options && currentQuestion.options.map((option) => (
                                            <div
                                                key={option.id}
                                                className={`p-3 border rounded-lg cursor-pointer transition-all duration-200 ${
                                                    selectedAnswers[currentQuestion.id] === option.id
                                                        ? 'border-blue-500 bg-blue-50'
                                                        : 'border-gray-200 hover:border-blue-300 hover:bg-blue-50'
                                                }`}
                                                onClick={() => handleAnswerSelect(currentQuestion.id, option.id)}
                                            >
                                                <div className="flex items-center">
                                                    <input
                                                        type="radio"
                                                        checked={selectedAnswers[currentQuestion.id] === option.id}
                                                        onChange={() => handleAnswerSelect(currentQuestion.id, option.id)}
                                                        className="w-4 h-4 mr-3 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                                                    />
                                                    <span className="text-gray-800">{option.text}</span>
                                                </div>
                                            </div>
                                        ))}
    
                                        {/* Open-ended question - Backend'den gelen soru tipini kontrol et */}
                                        {(currentQuestion.questionTypeId === 2 || currentQuestion.questionType === 'TEXT' || currentQuestion.questionType === 'OPEN_ENDED') && (
                                            <div className="p-3 border rounded-lg border-gray-200">
                                                <textarea
                                                    rows={5}
                                                    placeholder="Cevabınızı buraya yazın..."
                                                    className="w-full p-2 border border-gray-300 rounded focus:ring-blue-500 focus:border-blue-500"
                                                    value={textAnswers[currentQuestion.id] || ''}
                                                    onChange={(e) => handleTextAnswerChange(currentQuestion.id, e.target.value)}
                                                />
                                            </div>
                                        )}
                                    </div>
    
                                    {/* Navigation buttons - Daha minimal ve modern butonlar */}
                                    <div className="flex justify-between mt-8">
                                        <button
                                            onClick={handlePreviousQuestion}
                                            disabled={currentQuestionIndex === 0}
                                            className={`px-3 py-1 text-sm rounded transition-colors ${
                                                currentQuestionIndex === 0
                                                    ? 'text-gray-400 cursor-not-allowed'
                                                    : 'text-blue-600 hover:bg-blue-50'
                                            }`}
                                        >
                                            Önceki Soru
                                        </button>
    
                                        <div className="flex space-x-2">
                                            {currentQuestionIndex === totalQuestions - 1 ? (
                                                <button
                                                    onClick={handleSubmitQuiz}
                                                    className="px-4 py-1 bg-red-600 text-white text-sm rounded hover:bg-red-700 transition-colors"
                                                >
                                                    Sınavı Bitir
                                                </button>
                                            ) : (
                                                <button
                                                    onClick={handleNextQuestion}
                                                    className="px-3 py-1 text-sm text-blue-600 hover:bg-blue-50 rounded transition-colors"
                                                >
                                                    Sonraki Soru
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default SolveQuizPage;