import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import axios from 'axios';
import axiosInstance, { setupAxiosInterceptors } from '../api/axiosInstance';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const initialToken = localStorage.getItem('token');
    const initialUserString = localStorage.getItem('user');
    const initialUser = initialUserString ? JSON.parse(initialUserString) : null;

    const [token, setToken] = useState(initialToken);
    const [user, setUser] = useState(initialUser);

    useEffect(() => {
        if (token && user) {
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(user));
        } else {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            localStorage.removeItem('refreshToken');
        }
    }, [token, user]);

    const login = useCallback((newToken, refreshToken, userData) => {
        if (!newToken) {
            console.error('Login requires a token');
            return;
        }

        const userWithRoles = userData ? {
            ...userData,
            roles: userData.roles || (userData.role ? [userData.role] : ['ROLE_USER'])
        } : null;

      
        localStorage.setItem('token', newToken);
        if (refreshToken) {
            localStorage.setItem('refreshToken', refreshToken);
        }
        if (userWithRoles) {
            localStorage.setItem('user', JSON.stringify(userWithRoles));
        }

       
        setToken(newToken);
        setUser(userWithRoles);

        console.log('Login successful', { 
            hasUserData: !!userData,
            roles: userWithRoles?.roles 
        });
    }, []);

    const logout = useCallback(async () => {
        try {
            await axiosInstance.post('/auth/logout');
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            setToken(null);
            setUser(null);
            localStorage.clear();
            window.location.href = '/login';
        }
    }, []);

    const refreshToken = useCallback(async () => {
        const currentRefreshToken = localStorage.getItem('refreshToken');
        if (!currentRefreshToken) {
            console.log('No refresh token available');
            throw new Error('No refresh token available');
        }

        try {
            console.log('Attempting to refresh token...');
            
            const response = await axios.post(
                'http://localhost:8080/api/auth/refresh-token',
                {},
                {
                    headers: {
                        'Authorization': `Bearer ${currentRefreshToken}`,
                        'Content-Type': 'application/json'
                    },
                    withCredentials: true
                }
            );

            const { token: newAccessToken, refreshToken: newRefreshToken, user: userData } = response.data;
            
            if (!newAccessToken) {
                throw new Error('Invalid token response from server');
            }
            
            localStorage.setItem('token', newAccessToken);
            
            if (newRefreshToken) {
                localStorage.setItem('refreshToken', newRefreshToken);
            } else {
                localStorage.setItem('refreshToken', currentRefreshToken);
            }
            
            let userWithRoles;
            if (userData) {
                console.log('Updating user data from refresh token response:', userData);
                userWithRoles = {
                    ...userData,
                    roles: userData.roles || (userData.role ? [userData.role] : ['ROLE_USER'])
                };
            } else {
                console.log('No user data in refresh token response, using existing data');
                const existingUser = JSON.parse(localStorage.getItem('user') || '{}');
                userWithRoles = {
                    ...existingUser,
                    roles: existingUser?.roles || ['ROLE_USER']
                };
            }
            
            localStorage.setItem('user', JSON.stringify(userWithRoles));
            setUser(userWithRoles);
            setToken(newAccessToken);
            
            console.log('Token refresh successful');
            return newAccessToken;
            
        } catch (error) {
            console.error('Token refresh failed:', error);
            
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            
            if (error.response?.status === 401 || error.response?.status === 403) {
                console.log('Refresh token expired or invalid, logging out...');
                if (logout) {
                    logout();
                } else {
                    setUser(null);
                    setToken(null);
                    localStorage.removeItem('user');
                }
            }
            
            throw error;
        }
    }, [logout]);

    useEffect(() => {
        console.log("Setting up axios interceptors...");
        const interceptorId = setupAxiosInterceptors(refreshToken, logout);
        
        return () => {
            console.log("Cleaning up axios interceptors...");
            if (interceptorId !== undefined) {
                axiosInstance.interceptors.response.eject(interceptorId);
            }
        };
    }, [refreshToken, logout]);

    const contextValue = {
        token,
        user,
        login,
        logout,
        refreshToken,
        isAuthenticated: !!token && !!user,
        isAdmin: user?.roles?.includes('ROLE_ADMIN'),
        isStudent: user?.roles?.includes('ROLE_STUDENT'),
        isTeacher: user?.roles?.includes('ROLE_TEACHER'),
    };

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
