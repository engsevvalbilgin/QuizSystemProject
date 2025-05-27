import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function TeacherProfilePage() {
    const [teacher, setTeacher] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [passwordForm, setPasswordForm] = useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [emailForm, setEmailForm] = useState({
        newEmail: '',
        password: ''
    });
    const [successMessage, setSuccessMessage] = useState(null);
    const [formErrors, setFormErrors] = useState({});
    const [showPasswordForm, setShowPasswordForm] = useState(false);
    const [showEmailForm, setShowEmailForm] = useState(false);
    
    const navigate = useNavigate();

    useEffect(() => {
        const fetchTeacherProfile = async () => {
            try {
                setLoading(true);
                setError(null);
                const response = await axiosInstance.get('/users/profile');
                
                if (response.data) {
                    if (response.data.role === 'ROLE_TEACHER') {
                        setTeacher(response.data);
                    } else {
                        setError(`Bu sayfaya erişim yetkiniz yok. Rolünüz: ${response.data.role || 'Tanımsız'}`);
                    }
                } else {
                    setError('Profil bilgileri alınamadı.');
                }
            } catch (error) {
                setError('Profil bilgileri yüklenirken bir hata oluştu.');
                console.error('Öğretmen profili yüklenirken hata:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchTeacherProfile();
    }, []);

    const handlePasswordInputChange = (e) => {
        const { name, value } = e.target;
        setPasswordForm(prevState => ({
            ...prevState,
            [name]: value
        }));
        
        // Clear any errors for this field
        if (formErrors[name]) {
            setFormErrors(prev => ({
                ...prev,
                [name]: null
            }));
        }
    };

    const handleEmailInputChange = (e) => {
        const { name, value } = e.target;
        setEmailForm(prevState => ({
            ...prevState,
            [name]: value
        }));
        
        // Clear any errors for this field
        if (formErrors[name]) {
            setFormErrors(prev => ({
                ...prev,
                [name]: null
            }));
        }
    };

    const validatePasswordForm = () => {
        const errors = {};
        if (!passwordForm.oldPassword) {
            errors.oldPassword = 'Mevcut şifrenizi girmelisiniz.';
        }
        
        if (!passwordForm.newPassword) {
            errors.newPassword = 'Yeni şifre zorunludur.';
        } else if (passwordForm.newPassword.length < 6) {
            errors.newPassword = 'Şifre en az 6 karakter olmalıdır.';
        }
        
        if (!passwordForm.confirmPassword) {
            errors.confirmPassword = 'Şifre onayı zorunludur.';
        } else if (passwordForm.confirmPassword !== passwordForm.newPassword) {
            errors.confirmPassword = 'Şifreler eşleşmiyor.';
        }
        
        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const validateEmailForm = () => {
        const errors = {};
        
        if (!emailForm.newEmail) {
            errors.newEmail = 'Yeni email adresi zorunludur.';
        } else if (!/\S+@\S+\.\S+/.test(emailForm.newEmail)) {
            errors.newEmail = 'Geçerli bir email adresi girin.';
        }
        
        if (!emailForm.password) {
            errors.password = 'Şifrenizi girmelisiniz.';
        }
        
        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleChangePassword = async (e) => {
        e.preventDefault();
        setSuccessMessage(null);
        setError(null);
        
        if (!validatePasswordForm()) {
            return;
        }
        
        try {
            console.log('Şifre değiştirme isteği gönderiliyor:', teacher.id);
            
            const response = await axiosInstance.post(`/users/change-password`, {
                currentPassword: passwordForm.oldPassword,
                newPassword: passwordForm.newPassword
            });
            
            console.log('Şifre değiştirme yanıtı:', response.data);
            setSuccessMessage('Şifreniz başarıyla değiştirildi. E-posta adresinize bir bildirim gönderildi.');
            setPasswordForm({
                oldPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
            setShowPasswordForm(false);
        } catch (err) {
            if (err.response?.data?.message) {
                setError(err.response.data.message);
            } else {
                setError('Şifre değiştirme işlemi sırasında bir hata oluştu.');
            }
        }
    };

    const handleChangeEmail = async (e) => {
        e.preventDefault();
        setSuccessMessage(null);
        setError(null);
        
        if (!validateEmailForm()) {
            return;
        }
        
        try {
            console.log('E-posta değiştirme isteği gönderiliyor:', teacher.id);
            
            // Use the verification endpoint to send verification email
            const response = await axiosInstance.post('/users/change-email', {
                newEmail: emailForm.newEmail,
                password: emailForm.password
            });
            
            console.log('E-posta değiştirme yanıtı:', response.data);
            setSuccessMessage('E-posta değiştirme talebiniz alındı. Yeni e-posta adresinize bir doğrulama bağlantısı gönderildi.');
            setEmailForm({
                newEmail: '',
                password: ''
            });
            setShowEmailForm(false);
        } catch (err) {
            if (err.response?.data?.message) {
                setError(err.response.data.message);
            } else {
                setError('E-posta değiştirme işlemi sırasında bir hata oluştu.');
            }
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

    if (!teacher) {
        return (
            <div className="text-center py-10">
                <p className="text-gray-600">Öğretmen bilgileri bulunamadı.</p>
            </div>
        );
    }
    
    return (
        <div style={{ display: 'flex', minHeight: '100vh' }}>
            {/* Sidebar */}
            <div style={{
                width: '250px',
                backgroundColor: '#f8f9fa',
                borderRight: '1px solid #dee2e6',
                padding: '20px 0'
            }}>
                <div style={{ padding: '0 15px 15px 15px', borderBottom: '1px solid #dee2e6' }}>
                    <h3 style={{ margin: '0', color: '#495057' }}>Öğretmen Paneli</h3>
                    <p style={{ margin: '5px 0 0 0', fontSize: '0.9em', color: '#6c757d' }}>
                        {teacher.name} {teacher.surname}
                    </p>
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
                    background: '#e9ecef',
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
                <span>Quiz Oluştur</span>
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
                <h2 style={{ borderBottom: '1px solid #dee2e6', paddingBottom: '10px', marginBottom: '20px' }}>Profil Bilgilerim</h2>
                
                {successMessage && (
                    <div style={{ backgroundColor: '#d4edda', color: '#155724', padding: '10px', borderRadius: '4px', marginBottom: '20px' }}>
                        {successMessage}
                    </div>
                )}
                
                <div style={{ backgroundColor: 'white', borderRadius: '5px', padding: '20px', boxShadow: '0 0 10px rgba(0, 0, 0, 0.1)' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                        <div>
                            <h3 style={{ borderBottom: '1px solid #dee2e6', paddingBottom: '10px', marginBottom: '15px' }}>Kişisel Bilgiler</h3>
                            
                            <div style={{ marginBottom: '15px' }}>
                                <p style={{ margin: '0', color: '#6c757d', fontSize: '0.9em' }}>İsim</p>
                                <p style={{ margin: '5px 0 0 0', fontWeight: 'bold' }}>{teacher.name}</p>
                            </div>
                            
                            <div style={{ marginBottom: '15px' }}>
                                <p style={{ margin: '0', color: '#6c757d', fontSize: '0.9em' }}>Soyisim</p>
                                <p style={{ margin: '5px 0 0 0', fontWeight: 'bold' }}>{teacher.surname}</p>
                            </div>
                            
                            <div style={{ marginBottom: '15px' }}>
                                <p style={{ margin: '0', color: '#6c757d', fontSize: '0.9em' }}>Yaş</p>
                                <p style={{ margin: '5px 0 0 0', fontWeight: 'bold' }}>{teacher.age}</p>
                            </div>
                            
                            <div style={{ marginBottom: '15px' }}>
                                <p style={{ margin: '0', color: '#6c757d', fontSize: '0.9em' }}>Ders</p>
                                <p style={{ margin: '5px 0 0 0', fontWeight: 'bold' }}>{teacher.subject || 'Belirtilmemiş'}</p>
                            </div>
                            
                            <div style={{ marginBottom: '15px' }}>
                                <p style={{ margin: '0', color: '#6c757d', fontSize: '0.9em' }}>Mezun Olduğu Okul</p>
                                <p style={{ margin: '5px 0 0 0', fontWeight: 'bold' }}>{teacher.graduateSchool || 'Belirtilmemiş'}</p>
                            </div>
                            
                            <div style={{ marginBottom: '15px' }}>
                                <p style={{ margin: '0', color: '#6c757d', fontSize: '0.9em' }}>Diploma Numarası</p>
                                <p style={{ margin: '5px 0 0 0', fontWeight: 'bold' }}>{teacher.diplomaNumber || 'Belirtilmemiş'}</p>
                            </div>
                        </div>
                        
                        <div>
                            <h3 style={{ borderBottom: '1px solid #dee2e6', paddingBottom: '10px', marginBottom: '15px' }}>Hesap Bilgileri</h3>
                            
                            <div style={{ marginBottom: '15px' }}>
                                <p style={{ margin: '0', color: '#6c757d', fontSize: '0.9em' }}>Kullanıcı Adı</p>
                                <p style={{ margin: '5px 0 0 0', fontWeight: 'bold' }}>{teacher.username}</p>
                            </div>
                            
                            <div style={{ marginBottom: '15px' }}>
                                <p style={{ margin: '0', color: '#6c757d', fontSize: '0.9em' }}>E-posta</p>
                                <p style={{ margin: '5px 0 0 0', fontWeight: 'bold' }}>{teacher.email}</p>
                            </div>
                            
                            <div style={{ marginTop: '20px' }}>
                                <button 
                                    onClick={() => {
                                        setShowPasswordForm(!showPasswordForm);
                                        setShowEmailForm(false);
                                        setFormErrors({});
                                    }}
                                    style={{
                                        backgroundColor: '#007bff',
                                        color: 'white',
                                        border: 'none',
                                        padding: '8px 15px',
                                        borderRadius: '4px',
                                        marginRight: '10px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    {showPasswordForm ? 'İptal' : 'Şifre Değiştir'}
                                </button>
                                
                                <button 
                                    onClick={() => {
                                        setShowEmailForm(!showEmailForm);
                                        setShowPasswordForm(false);
                                        setFormErrors({});
                                    }}
                                    style={{
                                        backgroundColor: '#6c757d',
                                        color: 'white',
                                        border: 'none',
                                        padding: '8px 15px',
                                        borderRadius: '4px',
                                        marginRight: '10px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    {showEmailForm ? 'İptal' : 'E-posta Değiştir'}
                                </button>
                                
                                <button 
                                    onClick={() => navigate('/password-reset')}
                                    style={{
                                        backgroundColor: '#dc3545',
                                        color: 'white',
                                        border: 'none',
                                        padding: '8px 15px',
                                        borderRadius: '4px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    Şifremi Unuttum
                                </button>
                            </div>
                        </div>
                    </div>
                    
                    {/* Şifre Değiştirme Formu */}
                    {showPasswordForm && (
                        <div style={{ marginTop: '30px', padding: '20px', backgroundColor: '#f8f9fa', borderRadius: '5px' }}>
                            <h3 style={{ marginBottom: '15px' }}>Şifre Değiştir</h3>
                            <p style={{ marginBottom: '15px', color: '#6c757d' }}>
                                Şifrenizi hatırlamıyor musunuz? <a 
                                    onClick={() => navigate('/password-reset')} 
                                    style={{ color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}
                                >
                                    Şifre sıfırlama sayfasına gidin
                                </a>.
                            </p>
                            <form onSubmit={handleChangePassword}>
                                <div style={{ marginBottom: '15px' }}>
                                    <label htmlFor="oldPassword" style={{ display: 'block', marginBottom: '5px' }}>Mevcut Şifre</label>
                                    <input
                                        type="password"
                                        id="oldPassword"
                                        name="oldPassword"
                                        value={passwordForm.oldPassword}
                                        onChange={handlePasswordInputChange}
                                        style={{
                                            width: '100%',
                                            padding: '8px',
                                            border: formErrors.oldPassword ? '1px solid #dc3545' : '1px solid #ced4da',
                                            borderRadius: '4px'
                                        }}
                                    />
                                    {formErrors.oldPassword && <p style={{ color: '#dc3545', marginTop: '5px', fontSize: '0.9em' }}>{formErrors.oldPassword}</p>}
                                </div>
                                
                                <div style={{ marginBottom: '15px' }}>
                                    <label htmlFor="newPassword" style={{ display: 'block', marginBottom: '5px' }}>Yeni Şifre</label>
                                    <input
                                        type="password"
                                        id="newPassword"
                                        name="newPassword"
                                        value={passwordForm.newPassword}
                                        onChange={handlePasswordInputChange}
                                        style={{
                                            width: '100%',
                                            padding: '8px',
                                            border: formErrors.newPassword ? '1px solid #dc3545' : '1px solid #ced4da',
                                            borderRadius: '4px'
                                        }}
                                    />
                                    {formErrors.newPassword && <p style={{ color: '#dc3545', marginTop: '5px', fontSize: '0.9em' }}>{formErrors.newPassword}</p>}
                                </div>
                                
                                <div style={{ marginBottom: '15px' }}>
                                    <label htmlFor="confirmPassword" style={{ display: 'block', marginBottom: '5px' }}>Yeni Şifre (Tekrar)</label>
                                    <input
                                        type="password"
                                        id="confirmPassword"
                                        name="confirmPassword"
                                        value={passwordForm.confirmPassword}
                                        onChange={handlePasswordInputChange}
                                        style={{
                                            width: '100%',
                                            padding: '8px',
                                            border: formErrors.confirmPassword ? '1px solid #dc3545' : '1px solid #ced4da',
                                            borderRadius: '4px'
                                        }}
                                    />
                                    {formErrors.confirmPassword && <p style={{ color: '#dc3545', marginTop: '5px', fontSize: '0.9em' }}>{formErrors.confirmPassword}</p>}
                                </div>
                                
                                <div>
                                    <button
                                        type="submit"
                                        style={{
                                            backgroundColor: '#28a745',
                                            color: 'white',
                                            border: 'none',
                                            padding: '8px 15px',
                                            borderRadius: '4px',
                                            cursor: 'pointer'
                                        }}
                                    >
                                        Şifreyi Değiştir
                                    </button>
                                </div>
                            </form>
                        </div>
                    )}
                    
                    {/* E-posta Değiştirme Formu */}
                    {showEmailForm && (
                        <div style={{ marginTop: '30px', padding: '20px', backgroundColor: '#f8f9fa', borderRadius: '5px' }}>
                            <h3 style={{ marginBottom: '15px' }}>E-posta Değiştir</h3>
                            <p style={{ marginBottom: '15px', color: '#6c757d' }}>
                                Yeni e-posta adresinize bir doğrulama bağlantısı gönderilecektir.
                            </p>
                            <form onSubmit={handleChangeEmail}>
                                <div style={{ marginBottom: '15px' }}>
                                    <label htmlFor="newEmail" style={{ display: 'block', marginBottom: '5px' }}>Yeni E-posta</label>
                                    <input
                                        type="email"
                                        id="newEmail"
                                        name="newEmail"
                                        value={emailForm.newEmail}
                                        onChange={handleEmailInputChange}
                                        style={{
                                            width: '100%',
                                            padding: '8px',
                                            border: formErrors.newEmail ? '1px solid #dc3545' : '1px solid #ced4da',
                                            borderRadius: '4px'
                                        }}
                                    />
                                    {formErrors.newEmail && <p style={{ color: '#dc3545', marginTop: '5px', fontSize: '0.9em' }}>{formErrors.newEmail}</p>}
                                </div>
                                
                                <div style={{ marginBottom: '15px' }}>
                                    <label htmlFor="password" style={{ display: 'block', marginBottom: '5px' }}>Mevcut Şifre</label>
                                    <input
                                        type="password"
                                        id="password"
                                        name="password"
                                        value={emailForm.password}
                                        onChange={handleEmailInputChange}
                                        style={{
                                            width: '100%',
                                            padding: '8px',
                                            border: formErrors.password ? '1px solid #dc3545' : '1px solid #ced4da',
                                            borderRadius: '4px'
                                        }}
                                    />
                                    {formErrors.password && <p style={{ color: '#dc3545', marginTop: '5px', fontSize: '0.9em' }}>{formErrors.password}</p>}
                                </div>
                                
                                <div>
                                    <button
                                        type="submit"
                                        style={{
                                            backgroundColor: '#28a745',
                                            color: 'white',
                                            border: 'none',
                                            padding: '8px 15px',
                                            borderRadius: '4px',
                                            cursor: 'pointer'
                                        }}
                                    >
                                        E-postayı Değiştir
                                    </button>
                                </div>
                            </form>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default TeacherProfilePage;
