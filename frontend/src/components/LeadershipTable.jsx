import React, { useState, useEffect, useMemo } from 'react';
import axiosInstance from '../api/axiosInstance';
import { CheckCircleIcon, XCircleIcon } from '@heroicons/react/24/outline';

function LeadershipTable({ isVisible }) {
    const [leaders, setLeaders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedStudent, setSelectedStudent] = useState(null);

    const visible = isVisible;

    const fetchLeaders = async () => {
        try {
            setLoading(true);
            setError(null);
            
            const response = await axiosInstance.get('/api/statistics/student-leaders');
            const leaders = response.data;
            
            const topStudents = leaders.slice(0, 10);
            
            const studentDetails = topStudents.map(student => ({
                id: student.id,
                name: student.name,
                surname: student.surname,
                averageScore: student.averageScore,
                totalQuizzes: student.totalQuizzes,
                successfulQuizzes: student.successfulQuizzes
            }));

            setLeaders(studentDetails);
        } catch (err) {
            setError('Liderlik tablosu yüklenirken bir hata oluştu.');
            console.error('Error fetching leadership table:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (visible) {
            fetchLeaders();
        }
    }, [visible]);
    useEffect(() => {
        return () => {
            setVisible(false);
        };
    }, []);

    const memoizedLeaders = useMemo(() => leaders, [leaders]);

    if (loading) {
        return <div className="text-center py-4">Veriler yükleniyor...</div>;
    }

    if (error) {
        return <div className="text-center py-4 text-red-600">{error}</div>;
    }

    return (
        <div className="flex"> 
            <div className="flex-1">
                <div className="bg-white shadow rounded-lg p-6">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4">Liderlik Tablosu</h2>
                    <div className="overflow-x-auto">
                        <table className="min-w-full bg-white border border-gray-300">
                            <thead>
                                <tr className="bg-gray-100">
                                    <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sıra</th>
                                    <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">İsim</th>
                                    <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Soyad</th> 
                                    <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ortalama Başarı</th>
                                    <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Çözülen Quizler</th>
                                    <th className="px-6 py-3 border-b border-gray-300 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Başarılı Quizler</th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {memoizedLeaders.map((leader, index) => (
                                    <tr key={leader.id} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{index + 1}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 cursor-pointer hover:bg-gray-50" 
                                        onClick={() => {
                                            setSelectedStudent(leader);
                                        }}>
                                            {leader.name}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{leader.surname}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{(leader.averageScore * 100).toFixed(2)}%</td> 
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{leader.totalQuizzes}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{leader.successfulQuizzes}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            {selectedStudent && (
                <div className="w-96 ml-4 bg-white shadow-lg rounded-lg p-6">
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Öğrenci Detayları</h3>
                    <div className="space-y-4">
                        <div>
                            <h4 className="font-medium text-gray-700">Ad:</h4>
                            <p className="text-gray-900">{selectedStudent.name}</p>
                        </div>
                        <div>
                            <h4 className="font-medium text-gray-700">Soyad:</h4>
                            <p className="text-gray-900">{selectedStudent.surname}</p>
                        </div>
                        <div>
                            <h4 className="font-medium text-gray-700">Ortalama Puan:</h4>
                            <p className="text-gray-900">{(selectedStudent.averageScore * 100).toFixed(2)}%</p> 
                        </div>
                        <div>
                            <h4 className="font-medium text-gray-700">Toplam Quiz:</h4>
                            <p className="text-gray-900">{selectedStudent.totalQuizzes}</p>
                        </div>
                        <div>
                            <h4 className="font-medium text-gray-700">Başarılı Quiz:</h4>
                            <p className="text-gray-900">{selectedStudent.successfulQuizzes}</p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default function EnhancedLeadershipTable({ isVisible }) {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        setVisible(isVisible);
    }, [isVisible]);

    return <LeadershipTable visible={visible} />;
}