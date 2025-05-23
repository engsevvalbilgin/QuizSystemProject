import React from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

const LogoutButton = () => {
    const navigate = useNavigate();
    const isAuthenticated = !!localStorage.getItem('token');

    const handleLogout = async () => {
        try {
            // Show confirmation dialog
            if (!window.confirm('Çıkış yapmak istediğinizden emin misiniz?')) {
                return;
            }

            // Make API call to logout
            const response = await axiosInstance.post('/auth/logout');
            
            if (response.status === 200) {
                // Clear token from localStorage
                window.localStorage.removeItem('token');
                // Redirect to login page
                navigate('/login');
            } else {
                throw new Error('Çıkış işlemi başarısız oldu');
            }
        } catch (error) {
            console.error('Çıkış başarısız:', error);
            alert('Çıkış yaparken bir hata oluştu. Lütfen tekrar deneyin.');
        }
    };

    if (!isAuthenticated) {
        return null;
    }

    return (
        <button
            onClick={handleLogout}
            className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
        >
            <svg className="-ml-1 mr-2 h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M3 3a1 1 0 011-1h12a1 1 0 011 1v3a1 1 0 01-.293.707L12 11.414V15a1 1 0 01-.293.707l-2 2A1 1 0 018 17v-5.586L3.293 6.707A1 1 0 013 6V3z" clipRule="evenodd" />
            </svg>
            Çıkış Yap
        </button>
    );
};

export default LogoutButton;
