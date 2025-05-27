import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function StudentQuizDetailsPage() {
    const { quizId } = useParams();
    const [quiz, setQuiz] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchQuizDetails = async () => {
            try {
                setLoading(true);
                setError(null);
                const response = await axiosInstance.get(`/api/quizzes/${quizId}`);
                setQuiz(response.data);
            } catch (err) {
                setError(err.response?.data?.message || 'Quiz detayları yüklenirken bir hata oluştu.');
            } finally {
                setLoading(false);
            }
        };

        fetchQuizDetails();
    }, [quizId]);

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
        return (
            <div className="text-center py-10">
                <p className="text-gray-600">Quiz bulunamadı.</p>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="bg-white rounded-lg shadow-md p-6">
                <h1 className="text-2xl font-bold mb-4">{quiz.name}</h1>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <h3 className="text-lg font-semibold mb-2">Quiz Detayları</h3>
                        <div className="space-y-3">
                            <p><strong>Öğretmen:</strong> {quiz.teacher?.name}</p>
                            <p><strong>Konu:</strong> {quiz.topic}</p>
                            <p><strong>Sınıf:</strong> {quiz.grade}</p>
                            <p><strong>Sorular:</strong> {quiz.questions?.length || 0}</p>
                            <p><strong>Toplam Puan:</strong> {quiz.totalPoints}</p>
                            <p><strong>Bitiş Tarihi:</strong> {new Date(quiz.endDate).toLocaleDateString('tr-TR')}</p>
                        </div>
                    </div>
                    
                    <div>
                        <h3 className="text-lg font-semibold mb-2">Quiz Açıklaması</h3>
                        <p className="text-gray-700 whitespace-pre-wrap">{quiz.description || 'Quiz açıklaması bulunmamaktadır.'}</p>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default StudentQuizDetailsPage;
