import axiosInstance from './axiosInstance';

const teacherService = {
    registerTeacher: async (teacherData) => {
        try {
            const response = await axiosInstance.post('/auth/register/teacher', teacherData);
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

    getPendingRequests: async () => {
        try {
            const response = await axiosInstance.get('/users/teachers/pending');
            return response.data;
        } catch (error) {
            throw error.response?.data || error.message;
        }
    },

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
