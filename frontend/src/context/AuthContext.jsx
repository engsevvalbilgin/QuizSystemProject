// AuthContext.jsx
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

    // Update localStorage when token or user changes
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

    // Login function
    const login = useCallback((newToken, refreshToken, userData) => {
        if (!newToken) {
            console.error('Login requires a token');
            return;
        }

        // Prepare user data with roles
        const userWithRoles = userData ? {
            ...userData,
            // Ensure roles is always an array
            roles: userData.roles || (userData.role ? [userData.role] : ['ROLE_USER'])
        } : null;

        // Store tokens and user info
        localStorage.setItem('token', newToken);
        if (refreshToken) {
            localStorage.setItem('refreshToken', refreshToken);
        }
        if (userWithRoles) {
            localStorage.setItem('user', JSON.stringify(userWithRoles));
        }

        // Update state
        setToken(newToken);
        setUser(userWithRoles);

        console.log('Login successful', { 
            hasUserData: !!userData,
            roles: userWithRoles?.roles 
        });
    }, []);

    // Logout function
    const logout = useCallback(async () => {
        try {
            // Call the backend logout endpoint
            await axiosInstance.post('/auth/logout');
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // Clear state and storage
            setToken(null);
            setUser(null);
            localStorage.clear();
            // Redirect to login page
            window.location.href = '/login';
        }
    }, []);

    // Token refresh function
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
            
            // Update tokens in storage
            localStorage.setItem('token', newAccessToken);
            
            // Update refresh token if a new one was provided
            if (newRefreshToken) {
                localStorage.setItem('refreshToken', newRefreshToken);
            } else {
                // If no new refresh token, keep the current one
                localStorage.setItem('refreshToken', currentRefreshToken);
            }
            
            // Prepare user data
            let userWithRoles;
            if (userData) {
                console.log('Updating user data from refresh token response:', userData);
                userWithRoles = {
                    ...userData,
                    // Ensure roles is always an array
                    roles: userData.roles || (userData.role ? [userData.role] : ['ROLE_USER'])
                };
            } else {
                // If no user data in response, use existing data
                console.log('No user data in refresh token response, using existing data');
                const existingUser = JSON.parse(localStorage.getItem('user') || '{}');
                userWithRoles = {
                    ...existingUser,
                    roles: existingUser?.roles || ['ROLE_USER']
                };
            }
            
            // Update user data in state and storage
            localStorage.setItem('user', JSON.stringify(userWithRoles));
            setUser(userWithRoles);
            setToken(newAccessToken);
            
            console.log('Token refresh successful');
            return newAccessToken;
            
        } catch (error) {
            console.error('Token refresh failed:', error);
            
            // Clear invalid tokens
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            
            // If refresh token is invalid or expired, logout the user
            if (error.response?.status === 401 || error.response?.status === 403) {
                console.log('Refresh token expired or invalid, logging out...');
                if (logout) {
                    logout();
                } else {
                    // If logout is not available yet, clear user data
                    setUser(null);
                    setToken(null);
                    localStorage.removeItem('user');
                }
            }
            
            throw error; // Re-throw to allow the interceptor to handle the error
        }
    }, [logout]);

    // Set up axios interceptors
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

    // Context value
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
