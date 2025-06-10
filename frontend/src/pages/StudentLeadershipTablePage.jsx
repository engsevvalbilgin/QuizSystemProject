import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { CheckCircleIcon, XCircleIcon } from '@heroicons/react/24/outline';

function StudentLeadershipTablePage() {
    const [leaders, setLeaders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isVisible, setIsVisible] = useState(true);
    const navigate = useNavigate();
    
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userRole = user?.roles?.[0]; 

    useEffect(() => {
        fetchLeaders();
    }, []);

    const fetchLeaders = async () => {
        try {
            setLoading(true);
            setError(null);
            
            const response = await axiosInstance.get('/statistics/student-leaders');
            const students = response.data;

            students.sort((a, b) => b.averageScore - a.averageScore);

            setLeaders(students);
        } catch (err) {
            setError('Liderlik tablosu yüklenirken bir hata oluştu.');
            console.error('Error fetching leadership table:', err);
        } finally {
            setLoading(false);
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

    return (
        <div className="p-6">
            <div className="flex justify-between items-center mb-6">
                <div className="flex items-center space-x-4">
                    <button 
                        onClick={() => {
                            if (userRole === 'ROLE_STUDENT') {
                                navigate('/student');
                            } else if (userRole === 'ROLE_TEACHER' || userRole === 'ROLE_ADMIN') {
                                navigate('/teacher');
                            } else {
                                navigate('/');
                            }
                        }}
                        className="text-gray-600 hover:text-gray-900 px-3 py-1 border border-gray-300 rounded"
                    >
                        Geri Dön
                    </button>
                    <h2 className="text-2xl font-bold">Liderlik Tablosu</h2>
                </div>
                <button 
                    onClick={() => setIsVisible(!isVisible)}
                    className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                    {isVisible ? 'Gizle' : 'Göster'}
                </button>
            </div>
            {isVisible && (
                <div className="overflow-x-auto">
                <table className="min-w-full bg-white border border-gray-300">
                    <thead>
                        <tr className="bg-gray-100">
                            <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sıra</th>
                            <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">İsim</th>
                            <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ortalama Başarı</th>
                            <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Çözülen Quizler</th>
                            <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Başarılı Quizler</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {leaders.map((leader, index) => (
                            <tr key={leader.id} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{index + 1}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                    {leader.name} {leader.surname}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                    {leader.averageScore.toFixed(2)}%
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                    {leader.totalQuizzes}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                    {leader.successfulQuizzes}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            )}
        </div>
    );
}

export default StudentLeadershipTablePage;
