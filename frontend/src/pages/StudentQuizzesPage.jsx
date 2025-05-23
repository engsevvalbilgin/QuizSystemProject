import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function StudentQuizzesPage() {
    const [quizzes, setQuizzes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchQuizzes = async () => {
            try {
                setLoading(true);
                const response = await axiosInstance.get('/quizzes/student');
                setQuizzes(response.data);
            } catch (err) {
                setError('Quizler yüklenirken bir hata oluştu.');
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
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-6">Kullanılabilir Quizler</h1>
            {quizzes.length === 0 ? (
                <div className="text-center py-10">
                    <p className="text-gray-600">Şu anda aktif quiz bulunmamaktadır.</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {quizzes.map((quiz) => (
                        <div
                            key={quiz.id}
                            className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow cursor-pointer"
                            onClick={() => handleQuizClick(quiz.id)}
                        >
                            {/* Assuming 'quiz.title' and 'quiz.description' exist in your quiz object.
                                If not, use 'quiz.name' and potentially add a description field from your backend. */}
                            <h2 className="text-xl font-semibold mb-2">{quiz.name}</h2>
                            <p className="text-gray-600 mb-2">Öğretmen: {quiz.teacher.name}</p>
                            <p className="text-gray-600 mb-2">Başlangıç Tarihi: {formatTime(quiz.startDate)}</p>
                            <p className="text-gray-600 mb-2">Bitiş Tarihi: {formatTime(quiz.endDate)}</p>
                            <p className="text-gray-600 mb-4">Süre: {quiz.duration} dakika</p>
                            {/* The "Quiz'i Çöz" button was removed from the individual card and the entire card is now clickable */}
                            {/* If you want the button, you'll need to decide if the card itself should also be clickable or only the button. */}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default StudentQuizzesPage;