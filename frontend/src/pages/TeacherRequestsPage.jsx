import React, { useState, useEffect } from 'react';
import axiosInstance from '../api/axiosInstance';

function TeacherRequestsPage() {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);

    // Fetch pending teacher requests
    const fetchRequests = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await axiosInstance.get('/users/teachers/pending');
            setRequests(response.data);
        } catch (err) {
            console.error('Error fetching teacher requests:', err);
            setError('Öğretmen başvuruları yüklenirken bir hata oluştu.');
        } finally {
            setLoading(false);
        }
    };

    // Load requests when component mounts
    useEffect(() => {
        fetchRequests();
    }, []);

    // Handle request approval/rejection
    const handleRequestAction = async (userId, approve) => {
        setActionLoading(true);
        try {
            // Send approve as a URL parameter instead of request body
            await axiosInstance.post(`/users/teachers/${userId}/review?approve=${approve}`);
            // Refresh the list after action
            await fetchRequests();
        } catch (err) {
            console.error('Error processing teacher request:', err);
            setError('İşlem sırasında bir hata oluştu.');
        } finally {
            setActionLoading(false);
        }
    };

    if (loading) {
        return <div>Yükleniyor...</div>;
    }

    return (
        <div className="teacher-requests-page">
            <h2>Öğretmen Başvuruları</h2>
            {error && <div className="error-message">{error}</div>}
            
            {requests.length === 0 ? (
                <p>Bekleyen öğretmen başvurusu bulunmamaktadır.</p>
            ) : (
                <div className="requests-list">
                    {requests.map(request => (
                        <div key={request.id} className="request-card">
                            <div className="request-info">
                                <h3>{request.name} {request.surname}</h3>
                                <p><strong>Kullanıcı Adı:</strong> {request.username}</p>
                                <p><strong>Email:</strong> {request.email}</p>
                                <p><strong>Rol:</strong> {request.role}</p>
                                <p><strong>Hesap Durumu:</strong> {request.isActive ? 'Aktif' : 'Pasif'}</p>
                                <p><strong>Doğrulama Durumu:</strong> {request.enabled ? 'Doğrulandı' : 'Doğrulanmadı'}</p>
                                <p><strong>Kayıt Tarihi:</strong> {new Date(request.createdAt).toLocaleDateString('tr-TR')}</p>
                                <p><strong>Mezun olduğu Okul:</strong> {request.graduateSchool}</p>
                                <p><strong>Diploma Numarası:</strong> {request.diplomaNumber}</p>
                                
                            </div>
                            <div className="request-actions">
                                <button
                                    onClick={() => handleRequestAction(request.id, true)}
                                    disabled={actionLoading}
                                    className="approve-button"
                                >
                                    Onayla
                                </button>
                                <button
                                    onClick={() => handleRequestAction(request.id, false)}
                                    disabled={actionLoading}
                                    className="reject-button"
                                >
                                    Reddet
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default TeacherRequestsPage;
