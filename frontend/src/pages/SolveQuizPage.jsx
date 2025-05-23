import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function SolveQuizPage() {
    const { quizId } = useParams();
    const [quiz, setQuiz] = useState(null);
    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    const [selectedAnswers, setSelectedAnswers] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [timeRemaining, setTimeRemaining] = useState(0);
    const [isSubmitted, setIsSubmitted] = useState(false);

    useEffect(() => {
        const fetchQuiz = async () => {
            try {
                setLoading(true);
                const response = await axiosInstance.get(`/quizzes/${quizId}`);
                setQuiz(response.data);
                setTimeRemaining(response.data.timeLimit * 60);
            } catch (err) {
                setError('Quiz yüklenirken bir hata oluştu.');
                console.error('Quiz yüklenirken hata:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchQuiz();
    }, [quizId]);

    useEffect(() => {
        if (quiz && !isSubmitted) {
            const timer = setInterval(() => {
                setTimeRemaining(prev => {
                    if (prev <= 0) {
                        clearInterval(timer);
                        submitQuiz();
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);

            return () => clearInterval(timer);
        }
    }, [quiz, isSubmitted]);

    const handleAnswerChange = (questionId, answerId) => {
        setSelectedAnswers(prev => ({
            ...prev,
            [questionId]: answerId
        }));
    };

    const submitQuiz = async () => {
        try {
            setIsSubmitted(true);
            const response = await axiosInstance.post('/quiz-attempts', {
                quizId: quiz.id,
                answers: Object.entries(selectedAnswers).map(([questionId, answerId]) => ({
                    questionId: parseInt(questionId),
                    answerId: parseInt(answerId)
                }))
            });
            
            // Handle the response (e.g., show results page)
            console.log('Quiz submitted successfully:', response.data);
        } catch (err) {
            console.error('Error submitting quiz:', err);
        }
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

    if (!quiz) {
        return <div>Quiz bulunamadı.</div>;
    }

    const currentQuestion = quiz.questions[currentQuestionIndex];

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-2xl font-bold mb-6">{quiz.title}</h1>
            
            <div className="mb-4">
                <span className="text-sm text-gray-600">
                    {currentQuestionIndex + 1}/{quiz.questions.length} soru
                </span>
                <span className="ml-4 text-sm text-gray-600">
                    Kalan süre: {Math.floor(timeRemaining / 60)}:{timeRemaining % 60 < 10 ? '0' : ''}{timeRemaining % 60}
                </span>
            </div>

            <div className="bg-white rounded-lg shadow-md p-6">
                <h2 className="text-xl font-semibold mb-4">{currentQuestion.text}</h2>
                
                {currentQuestion.options.map((option) => (
                    <div key={option.id} className="mb-3">
                        <input
                            type="radio"
                            id={`option-${option.id}`}
                            name={`question-${currentQuestion.id}`}
                            value={option.id}
                            checked={selectedAnswers[currentQuestion.id] === option.id}
                            onChange={() => handleAnswerChange(currentQuestion.id, option.id)}
                        />
                        <label className="ml-2" htmlFor={`option-${option.id}`}>
                            {option.text}
                        </label>
                    </div>
                ))}
            </div>

            <div className="mt-4 flex justify-between">
                <button
                    className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                    onClick={() => {
                        if (currentQuestionIndex > 0) {
                            setCurrentQuestionIndex(prev => prev - 1);
                        }
                    }}
                    disabled={currentQuestionIndex === 0}
                >
                    Önceki Soru
                </button>
                
                <button
                    className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                    onClick={() => {
                        if (currentQuestionIndex < quiz.questions.length - 1) {
                            setCurrentQuestionIndex(prev => prev + 1);
                        } else {
                            submitQuiz();
                        }
                    }}
                >
                    {currentQuestionIndex < quiz.questions.length - 1 ? 'Sonraki Soru' : 'Quizi Bitir'}
                </button>
            </div>
        </div>
    );
}

export default SolveQuizPage;