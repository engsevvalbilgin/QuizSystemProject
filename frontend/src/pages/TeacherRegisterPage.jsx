import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function TeacherRegisterPage() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        name: '',
        surname: '',
        age: '',
        subject: '',
        graduateSchool: '',
        diplomaNo: ''
    });
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);
        setSuccess(false);

        // Validate password match
        if (formData.password !== formData.confirmPassword) {
            setError('Şifreler eşleşmiyor!');
            setLoading(false);
            return;
        }

        // Prepare request data
        const requestData = {
            username: formData.username,
            email: formData.email,
            password: formData.password,
            name: formData.name,
            surname: formData.surname,
            age: parseInt(formData.age),
            subject: formData.subject,
            graduateSchool: formData.graduateSchool,
            diplomaNumber: formData.diplomaNo,
            role: "ROLE_TEACHER" // Add role for teacher registration
        };

        try {
            const response = await axiosInstance.post('/teachers/register', requestData);
            console.log('Teacher registration successful:', response.data);
            setSuccess(true);
            // Clear form
            setFormData({
                username: '',
                email: '',
                password: '',
                confirmPassword: '',
                name: '',
                surname: '',
                age: '',
                subject: '',
                graduateSchool: '',
                diplomaNo: ''
            });
            // Show success message for 3 seconds then redirect
            setTimeout(() => {
                navigate('/login');
            }, 3000);
        } catch (err) {
            console.error('Teacher registration error:', err);
            if (err.response?.data?.message) {
                setError(err.response.data.message);
            } else {
                setError('Kayıt işlemi sırasında bir hata oluştu. Lütfen tekrar deneyin.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="register-page">
            <div className="register-container">
                <h2>Öğretmen Kaydı</h2>
                {error && <div className="error-message">{error}</div>}
                {success && (
                    <div className="success-message">
                        Kayıt başarıyla tamamlandı! Başvurunuz admin onayına gönderildi.
                        Email adresinize bilgilendirme maili gönderilecektir.
                        Giriş sayfasına yönlendiriliyorsunuz...
                    </div>
                )}
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="username">Kullanıcı Adı:</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="email">Email:</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">Şifre:</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="confirmPassword">Şifre Tekrar:</label>
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="name">Ad:</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="surname">Soyad:</label>
                        <input
                            type="text"
                            id="surname"
                            name="surname"
                            value={formData.surname}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="age">Yaş:</label>
                        <input
                            type="number"
                            id="age"
                            name="age"
                            value={formData.age}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="subject">Ders Alanı:</label>
                        <input
                            type="text"
                            id="subject"
                            name="subject"
                            value={formData.subject}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="graduateSchool">Mezun Olduğunuz Okul:</label>
                        <input
                            type="text"
                            id="graduateSchool"
                            name="graduateSchool"
                            value={formData.graduateSchool}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="diplomaNo">Diploma Numarası:</label>
                        <input
                            type="text"
                            id="diplomaNo"
                            name="diplomaNo"
                            value={formData.diplomaNo}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <button type="submit" disabled={loading}>
                        {loading ? 'Kaydediliyor...' : 'Kayıt Ol'}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default TeacherRegisterPage;
