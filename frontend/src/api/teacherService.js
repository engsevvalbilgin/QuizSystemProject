import axiosInstance from './axiosInstance';

const teacherService = {
    // Register as a teacher
    registerTeacher: async (teacherData) => {
        try {
            const response = await axiosInstance.post('/auth/register/teacher', teacherData);
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    // Get pending teacher requests (admin only)
    getPendingRequests: async () => {
        try {
            const response = await axiosInstance.get('/users/teachers/pending');
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    // Review teacher request (admin only)
    reviewTeacherRequest: async (userId, approve) => {
        try {
            const response = await axiosInstance.post(`/users/teachers/${userId}/review`, { approve });
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    }
};

export default teacherService;
