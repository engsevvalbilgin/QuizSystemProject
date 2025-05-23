import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function StudentRegisterPage() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        name: '',
        surname: '',
        age: '',
        schoolName: ''
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);
    const [formErrors, setFormErrors] = useState({});

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
        if (formErrors[name]) {
            setFormErrors(prev => ({
                ...prev,
                [name]: null
            }));
        }
    };

    const validateForm = () => {
        const errors = {};
        if (!formData.username) errors.username = 'Kullanıcı adı zorunludur.';
        if (!formData.email) {
            errors.email = 'Email zorunludur.';
        } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
            errors.email = 'Geçerli bir email adresi girin.';
        }
        if (!formData.password) {
            errors.password = 'Şifre zorunludur.';
        } else if (formData.password.length < 6) {
            errors.password = 'Şifre en az 6 karakter olmalıdır.';
        }
        if (!formData.confirmPassword) {
            errors.confirmPassword = 'Şifre onayı zorunludur.';
        } else if (formData.confirmPassword !== formData.password) {
            errors.confirmPassword = 'Şifreler eşleşmiyor.';
        }
        if (!formData.name) errors.name = 'İsim zorunludur.';
        if (!formData.surname) errors.surname = 'Soyisim zorunludur.';
        if (!formData.schoolName) errors.schoolName = 'Okul ismi zorunludur.';
        if (!formData.age) {
            errors.age = 'Yaş zorunludur.';
        } else if (isNaN(formData.age) || parseInt(formData.age) <= 0) {
            errors.age = 'Geçerli bir yaş girin.';
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccessMessage(null);

        if (!validateForm()) {
            return;
        }

        setLoading(true);

        try {
            const requestData = {
                username: formData.username,
                email: formData.email,
                password: formData.password,
                name: formData.name,
                surname: formData.surname,
                age: parseInt(formData.age),
                schoolName: formData.schoolName
            };

            const response = await axiosInstance.post('/auth/register', requestData);
            setSuccessMessage(response.data.message || 'Kayıt başarılı! Lütfen email adresinizi kontrol edin.');
            setTimeout(() => {
                navigate('/login');
            }, 3000);
        } catch (err) {
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
                <h2>Öğrenci Kaydı</h2>
                {error && <div className="error-message">{error}</div>}
                {successMessage && <div className="success-message">{successMessage}</div>}
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="username">Kullanıcı Adı:</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleInputChange}
                            disabled={loading}
                        />
                        {formErrors.username && <p className="error-text">{formErrors.username}</p>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email:</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            disabled={loading}
                        />
                        {formErrors.email && <p className="error-text">{formErrors.email}</p>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Şifre:</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            disabled={loading}
                        />
                        {formErrors.password && <p className="error-text">{formErrors.password}</p>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirmPassword">Şifre Onayı:</label>
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleInputChange}
                            disabled={loading}
                        />
                        {formErrors.confirmPassword && <p className="error-text">{formErrors.confirmPassword}</p>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="name">Ad:</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleInputChange}
                            disabled={loading}
                        />
                        {formErrors.name && <p className="error-text">{formErrors.name}</p>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="surname">Soyad:</label>
                        <input
                            type="text"
                            id="surname"
                            name="surname"
                            value={formData.surname}
                            onChange={handleInputChange}
                            disabled={loading}
                        />
                        {formErrors.surname && <p className="error-text">{formErrors.surname}</p>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="age">Yaş:</label>
                        <input
                            type="number"
                            id="age"
                            name="age"
                            value={formData.age}
                            onChange={handleInputChange}
                            disabled={loading}
                            min="1"
                        />
                        {formErrors.age && <p className="error-text">{formErrors.age}</p>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="schoolName">Okul İsmi:</label>
                        <input
                            type="text"
                            id="schoolName"
                            name="schoolName"
                            value={formData.schoolName}
                            onChange={handleInputChange}
                            disabled={loading}
                        />
                        {formErrors.schoolName && <p className="error-text">{formErrors.schoolName}</p>}
                    </div>

                    <button type="submit" disabled={loading}>
                        {loading ? 'Kaydediliyor...' : 'Kayıt Ol'}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default StudentRegisterPage;
