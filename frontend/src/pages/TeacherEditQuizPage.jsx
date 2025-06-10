import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function TeacherEditQuizPage() {
    const navigate = useNavigate();
    const { quizId } = useParams();

    const [quiz, setQuiz] = useState({
        name: '',
        description: '',
        durationMinutes: 30,
        topic: '',
        isActive: true, 
        questions: []
    });

    const [errors, setErrors] = useState({
        name: false,
        topic: false,
        questions: [] 
    });

    const [isLoading, setIsLoading] = useState(true);
    const [fetchError, setFetchError] = useState(null); 
    const [isSaving, setIsSaving] = useState(false);

    const QUESTION_TYPES = [
        { value: 1, label: 'Çoktan Seçmeli' },
        { value: 2, label: 'Açık Uçlu' }
    ];

    useEffect(() => {
        const fetchQuizData = async () => {
            setIsLoading(true);
            setFetchError(null);
            try {
                const quizResponse = await axiosInstance.get(`/quizzes/${quizId}`);
                const quizData = quizResponse.data;

                const questionsResponse = await axiosInstance.get(`/quizzes/${quizId}/questions`);
                const questionsData = questionsResponse.data || [];

                const sortedQuestions = [...questionsData].sort((a, b) => a.number - b.number);

                setQuiz({
                    name: quizData.name || '',
                    description: quizData.description || '',
                    durationMinutes: quizData.durationMinutes || 30,
                    topic: quizData.topic || '',
                    isActive: quizData.isActive === true, 
                    questions: sortedQuestions.map(q => {
                        let questionTypeId = 1; 
                        if (q.questionTypeId !== undefined) {
                            questionTypeId = parseInt(q.questionTypeId, 10);
                        } else if (q.questionType) {
                            if (typeof q.questionType === 'object' && q.questionType.id !== undefined) {
                                questionTypeId = parseInt(q.questionType.id, 10);
                            } else if (typeof q.questionType === 'string') {
                                if (q.questionType.toLowerCase().includes('açık uçlu') || q.questionType.toLowerCase().includes('open_ended') || q.questionType === "2") {
                                    questionTypeId = 2;
                                } else {
                                    questionTypeId = 1;
                                }
                            } else if (typeof q.questionType === 'number') {
                                questionTypeId = q.questionType;
                            }
                        }
                        
                        let currentOptions = [];
                        if (questionTypeId === 1) { 
                            if (q.options && Array.isArray(q.options) && q.options.length > 0) {
                                currentOptions = q.options.map(opt => ({
                                    id: opt.id,
                                    text: opt.text || '',
                                    isCorrect: opt.isCorrect === true || opt.isCorrect === 'true'
                                }));
                            } else {
                                currentOptions = [
                                    { id: undefined, text: '', isCorrect: false },
                                    { id: undefined, text: '', isCorrect: false },
                                    { id: undefined, text: '', isCorrect: false },
                                    { id: undefined, text: '', isCorrect: false }
                                ];
                            }
                        }

                        return {
                            id: q.id,
                            questionSentence: q.questionSentence || '',
                            questionTypeId: questionTypeId,
                            points: q.points || 1,
                            options: currentOptions,
                            correctAnswerText: q.correctAnswerText || '' 
                        };
                    })
                });
                 
                setErrors(prevErrors => ({
                    ...prevErrors,
                    questions: sortedQuestions.map(() => ({
                        questionSentence: false,
                        options: [], 
                        noCorrectOption: false
                    }))
                }));

            } catch (error) {
                console.error('Quiz yüklenirken hata:', error);
                setFetchError('Quiz yüklenirken bir hata oluştu. Lütfen tekrar deneyin.');
            } finally {
                setIsLoading(false);
            }
        };
        fetchQuizData();
    }, [quizId]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setQuiz(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const addQuestion = () => {
        const newQuestion = {
            questionSentence: '',
            questionTypeId: 1, 
            points: 1,
            options: [ 
                { id: Date.now(), text: '', isCorrect: false },
                { id: Date.now() + 1, text: '', isCorrect: false },
                { id: Date.now() + 2, text: '', isCorrect: false },
                { id: Date.now() + 3, text: '', isCorrect: false }
            ],
            correctAnswerText: ''
        };
        setQuiz(prev => ({
            ...prev,
            questions: [...prev.questions, newQuestion]
        }));
        setErrors(prevErrors => ({
            ...prevErrors,
            questions: [
                ...prevErrors.questions,
                { questionSentence: false, options: [false,false,false,false], noCorrectOption: false }
            ]
        }));
    };

    const removeQuestion = async (index) => {
        const questionToRemove = quiz.questions[index];
        if (!window.confirm(`"${questionToRemove.questionSentence || 'Bu'}" soruyu silmek istediğinize emin misiniz?`)) {
            return;
        }
        try {
            if (questionToRemove.id) { 
                await axiosInstance.delete(`/quizzes/${quizId}/questions/${questionToRemove.id}`);
            }
            setQuiz(prev => ({
                ...prev,
                questions: prev.questions.filter((_, i) => i !== index)
            }));
            setErrors(prevErrors => ({
                ...prevErrors,
                questions: prevErrors.questions.filter((_, i) => i !== index)
            }));
        } catch (error) {
            console.error('Soru silinirken hata:', error);
            alert(`Soru silinirken bir hata oluştu: ${error.response?.data?.message || error.message}`);
        }
    };
    
    const handleQuestionChange = (index, field, value) => {
        const newQuestions = quiz.questions.map((q, i) => {
            if (i === index) {
                const updatedQuestion = { ...q, [field]: value };
                if (field === 'questionTypeId') {
                    updatedQuestion.questionTypeId = parseInt(value, 10); 
                    if (updatedQuestion.questionTypeId === 2) { 
                        updatedQuestion.options = []; 
                    } else if (updatedQuestion.questionTypeId === 1 && (!q.options || q.options.length === 0)) {
                       
                        updatedQuestion.options = [
                            { text: '', isCorrect: false }, { text: '', isCorrect: false },
                            { text: '', isCorrect: false }, { text: '', isCorrect: false }
                        ];
                    }
                }
                return updatedQuestion;
            }
            return q;
        });
        setQuiz(prev => ({ ...prev, questions: newQuestions }));
    };

    const handleOptionChange = (questionIndex, optionIndex, field, value) => {
        const newQuestions = quiz.questions.map((q, i) => {
            if (i === questionIndex) {
                const newOptions = q.options.map((opt, j) => {
                    if (j === optionIndex) {
                        return { ...opt, [field]: value };
                    }
                    return opt;
                });
                return { ...q, options: newOptions };
            }
            return q;
        });
        setQuiz(prev => ({ ...prev, questions: newQuestions }));
    };

    const setCorrectAnswer = (questionIndex, optionIndex) => {
        const newQuestions = quiz.questions.map((q, i) => {
            if (i === questionIndex) {
                const newOptions = q.options.map((opt, j) => ({
                    ...opt,
                    isCorrect: j === optionIndex
                }));
                return { ...q, options: newOptions };
            }
            return q;
        });
        setQuiz(prev => ({ ...prev, questions: newQuestions }));
    };

    const validateForm = () => {
        let isValid = true;
        const newQuizErrors = { 
            name: quiz.name.trim() === '',
            topic: quiz.topic.trim() === '',
            questions: [] 
        };

        if (newQuizErrors.name) isValid = false;
        if (newQuizErrors.topic) isValid = false;

        if (quiz.questions.length === 0) {
           
        }


        const newQuestionErrorsArray = quiz.questions.map((q, index) => {
            const questionError = {
                questionSentence: false,
                options: q.options ? q.options.map(() => false) : [], 
                noCorrectOption: false
            };

            if (q.questionSentence.trim() === '') {
                questionError.questionSentence = true;
                isValid = false;
            }

            if (q.questionTypeId === 1) { 
                let correctOptionFound = false;
                if (!q.options || q.options.length === 0) {
                    questionError.noCorrectOption = true; 
                    isValid = false;
                } else {
                    q.options.forEach((opt, optIndex) => {
                        if (opt.text.trim() === '') {
                            questionError.options[optIndex] = true;
                            isValid = false;
                        }
                        if (opt.isCorrect) {
                            correctOptionFound = true;
                        }
                    });
                    if (!correctOptionFound) {
                        questionError.noCorrectOption = true;
                        isValid = false;
                    }
                }
            }
            return questionError;
        });
        
        newQuizErrors.questions = newQuestionErrorsArray;
        setErrors(newQuizErrors);
        return isValid;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) {
            alert('Lütfen tüm zorunlu alanları doldurun, en az bir soru ekleyin ve çoktan seçmeli sorular için bir doğru cevap seçin.');
            return;
        }
        setIsSaving(true);
        setFetchError(null);

        try {
            const quizPayload = {
                name: quiz.name,
                description: quiz.description,
                durationMinutes: parseInt(quiz.durationMinutes) || 30,
                topic: quiz.topic,
                active: true, 
            };
            await axiosInstance.put(`/quizzes/${quizId}`, quizPayload);

            const questionPromises = quiz.questions.map(async (question, index) => {
                const questionData = {
                    number: index + 1, 
                    questionSentence: question.questionSentence,
                    questionTypeId: question.questionTypeId,
                    points: question.points || 1,
                    options: [],
                    correctAnswerText: ''
                };

                if (question.questionTypeId === 1) { 
                    questionData.options = question.options.map(opt => ({
                        id: opt.id, 
                        text: opt.text,
                        isCorrect: !!opt.isCorrect
                    }));
                } else { 
                    questionData.correctAnswerText = question.correctAnswerText || '';
                }
                
                

                if (question.id) { 
                    return axiosInstance.put(`/quizzes/${quizId}/questions/${question.id}`, questionData);
                } else { 
                    return axiosInstance.post(`/quizzes/${quizId}/questions`, questionData);
                }
            });

            await Promise.all(questionPromises);

            alert('Quiz başarıyla güncellendi.');
            navigate('/teacher/my-quizzes');

        } catch (error) {
            console.error('Quiz güncellenirken hata:', error);
            setFetchError(`Quiz güncellenirken bir hata oluştu: ${error.response?.data?.message || error.message}`);
            alert(`Quiz güncellenirken bir hata oluştu: ${error.response?.data?.message || error.message}`);
        } finally {
            setIsSaving(false);
        }
    };

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-screen">
                <div className="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-green-500"></div>
            </div>
        );
    }

    if (fetchError) { 
        return (
            <div className="p-6">
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                    <p>{fetchError}</p>
                    <button
                        onClick={() => navigate('/teacher/my-quizzes')}
                        className="mt-4 bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
                    >
                        Quizlerime Dön
                    </button>
                </div>
            </div>
        );
    }
    
    return (
        <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f8f9fa' }}>
            <div style={{ width: '250px', backgroundColor: 'white', boxShadow: '0 0 10px rgba(0,0,0,0.1)' }}>
                <div style={{ padding: '20px', borderBottom: '1px solid #eaeaea' }}>
                    <h3 style={{ fontSize: '1.2rem', fontWeight: '600', color: '#333' }}>Öğretmen Paneli</h3>
                </div>
                <nav style={{ marginTop: '20px' }}>
                    <ul style={{ listStyleType: 'none', padding: '0' }}>
                         <li><button onClick={() => navigate('/teacher')} style={{ width: '100%', textAlign: 'left', padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer' }}>Ana Sayfa</button></li>
                         <li><button onClick={() => navigate('/teacher/my-quizzes')} style={{ width: '100%', textAlign: 'left', padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', backgroundColor: '#e9ecef' }}>Quizlerim</button></li>
                         <li><button onClick={() => navigate('/teacher/create-quiz')} style={{ width: '100%', textAlign: 'left', padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer' }}>Yeni Quiz Oluştur</button></li>
                    </ul>
                </nav>
            </div>

            <div style={{ flex: 1, padding: '20px' }}>
                <div className="p-4 md:p-6 bg-white rounded-lg shadow-md">
                    <div className="flex justify-between items-center mb-6">
                        <h2 className="text-2xl font-bold text-gray-700">Quiz Düzenle</h2>
                        <button
                            onClick={() => navigate('/teacher/my-quizzes')}
                            className="text-blue-600 hover:underline flex items-center"
                        >
                            <span className="mr-1">←</span> Quizlerime Dön
                        </button>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div>
                            <label htmlFor="name" className="block text-sm font-medium text-gray-700">Quiz Adı:</label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                value={quiz.name}
                                onChange={handleChange}
                                className={`mt-1 block w-full px-3 py-2 border ${errors.name ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm`}
                            />
                            {errors.name && <p className="mt-1 text-xs text-red-600">Quiz adı zorunludur.</p>}
                        </div>

                        <div>
                            <label htmlFor="description" className="block text-sm font-medium text-gray-700">Açıklama:</label>
                            <textarea
                                id="description"
                                name="description"
                                value={quiz.description}
                                onChange={handleChange}
                                rows="3"
                                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                            />
                        </div>

                        <div>
                            <label htmlFor="topic" className="block text-sm font-medium text-gray-700">Konu:</label>
                            <input
                                type="text"
                                id="topic"
                                name="topic"
                                value={quiz.topic}
                                onChange={handleChange}
                                className={`mt-1 block w-full px-3 py-2 border ${errors.topic ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm`}
                            />
                            {errors.topic && <p className="mt-1 text-xs text-red-600">Konu zorunludur.</p>}
                        </div>

                        <div>
                            <label htmlFor="durationMinutes" className="block text-sm font-medium text-gray-700">Süre (dakika):</label>
                            <input
                                type="number"
                                id="durationMinutes"
                                name="durationMinutes"
                                value={quiz.durationMinutes}
                                onChange={handleChange}
                                min="1"
                                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                            />
                        </div>
                        

                        <h3 className="text-lg font-medium leading-6 text-gray-900 pt-4 border-t border-gray-200">Sorular</h3>
                        {quiz.questions.map((q, qIndex) => (
                            <div key={q.id ? `q-${q.id}` : `q-new-${qIndex}`} className="p-4 border border-gray-200 rounded-md space-y-3">
                                <h4 className="text-md font-semibold text-gray-700">Soru {qIndex + 1}</h4>
                                <div>
                                    <label htmlFor={`questionSentence-${qIndex}`} className="block text-xs font-medium text-gray-600">Soru Metni:</label>
                                    <textarea
                                        id={`questionSentence-${qIndex}`}
                                        value={q.questionSentence}
                                        onChange={(e) => handleQuestionChange(qIndex, 'questionSentence', e.target.value)}
                                        className={`mt-1 block w-full px-3 py-2 border ${errors.questions[qIndex]?.questionSentence ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm`}
                                        rows="3"
                                    />
                                    {errors.questions[qIndex]?.questionSentence && <p className="mt-1 text-red-600 text-xs">Soru metni zorunludur.</p>}
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                        <label htmlFor={`questionType-${qIndex}`} className="block text-xs font-medium text-gray-600">Soru Tipi:</label>
                                        <select
                                            id={`questionType-${qIndex}`}
                                            value={q.questionTypeId}
                                            onChange={(e) => handleQuestionChange(qIndex, 'questionTypeId', e.target.value)}
                                            className="mt-1 block w-full pl-3 pr-10 py-1.5 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md"
                                        >
                                            {QUESTION_TYPES.map(type => (
                                                <option key={type.value} value={type.value}>{type.label}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div>
                                        <label htmlFor={`points-${qIndex}`} className="block text-xs font-medium text-gray-600">Puan:</label>
                                        <input
                                            type="number"
                                            id={`points-${qIndex}`}
                                            value={q.points}
                                            onChange={(e) => handleQuestionChange(qIndex, 'points', parseInt(e.target.value) || 0)}
                                            min="0"
                                            className="mt-1 block w-full px-2 py-1.5 border border-gray-300 rounded-md shadow-sm sm:text-sm"
                                        />
                                    </div>
                                </div>

                                {q.questionTypeId === 1 && (
                                    <div className="space-y-2 mt-2">
                                        <p className="text-xs font-medium text-gray-600">Seçenekler:</p>
                                        {errors.questions[qIndex]?.noCorrectOption && <p className="text-red-600 text-xs">Lütfen bir doğru cevap seçin.</p>}
                                        {q.options.map((opt, oIndex) => (
                                            <div key={opt.id ? `opt-${opt.id}` : `opt-new-${qIndex}-${oIndex}`} className="flex items-center space-x-2">
                                                <input
                                                    type="text"
                                                    value={opt.text}
                                                    onChange={(e) => handleOptionChange(qIndex, oIndex, 'text', e.target.value)}
                                                    placeholder={`Seçenek ${oIndex + 1}`}
                                                    className={`flex-grow px-2 py-1.5 border ${errors.questions[qIndex]?.options?.[oIndex] ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm sm:text-sm`}
                                                />
                                                <input
                                                    type="radio"
                                                    name={`correctAnswer-${qIndex}`}
                                                    checked={opt.isCorrect}
                                                    onChange={() => setCorrectAnswer(qIndex, oIndex)}
                                                    className="h-4 w-4 text-indigo-600 border-gray-300 focus:ring-indigo-500"
                                                />
                                                <label className="text-xs text-gray-600">Doğru</label>
                                            </div>
                                        ))}
                                        {q.options.map((opt, oIndex) => errors.questions[qIndex]?.options?.[oIndex] && (
                                            <p key={`err-${qIndex}-${oIndex}`} className="text-red-600 text-xs">
                                                Seçenek {oIndex+1} metni zorunludur.
                                            </p>
                                        ))}

                                    </div>
                                )}
                                {q.questionTypeId === 2 && (
                                    <div className="mt-2 p-2 bg-blue-50 rounded">
                                        <p className="text-sm text-blue-700">Açık uçlu soru - Öğrenciler kendi cevaplarını yazacaklar</p>
                                    </div>
                                )}
                                <button
                                    type="button"
                                    onClick={() => removeQuestion(qIndex)}
                                    className="mt-2 px-3 py-1.5 text-xs font-medium text-red-600 bg-red-100 hover:bg-red-200 rounded-md"
                                >
                                    Soruyu Sil
                                </button>
                            </div>
                        ))}

                        <button
                            type="button"
                            onClick={addQuestion}
                            className="mt-4 px-4 py-2 text-sm font-medium text-white bg-green-600 hover:bg-green-700 rounded-md shadow-sm"
                        >
                            Yeni Soru Ekle
                        </button>
                        
                        <div className="pt-5 border-t border-gray-200">
                            <div className="flex justify-end">
                                <button
                                    type="button"
                                    onClick={() => navigate('/teacher/my-quizzes')}
                                    className="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                                >
                                    İptal
                                </button>
                                <button
                                    type="submit"
                                    disabled={isSaving}
                                    className="ml-3 inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
                                >
                                    {isSaving ? 'Kaydediliyor...' : 'Değişiklikleri Kaydet'}
                                </button>
                            </div>
                        </div>
                         {fetchError && <p className="mt-2 text-red-600 text-sm">{fetchError}</p>} 
                    </form>
                </div>
            </div>
        </div>
    );
}

export default TeacherEditQuizPage;