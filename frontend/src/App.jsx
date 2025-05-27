import React from 'react';
import { Routes, Route } from 'react-router-dom';

// Layouts
import MainLayout from './components/MainLayout';
import ProtectedRoutes from './components/ProtectedRoutes';
import AdminLayout from './components/AdminLayout';

// Page Components
import MainScreen from './pages/MainScreen.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import StudentRegisterPage from './pages/StudentRegisterPage.jsx';
import TeacherRegisterPage from './pages/TeacherRegisterPage.jsx';
import TeacherPanel from './pages/TeacherPanel.jsx';
import VerifyEmailPage from './pages/VerifyEmailPage.jsx';
import PasswordResetPage from './pages/PasswordResetPage.jsx';
import AdminDashboardPage from './pages/AdminDashboardPage.jsx';
import AdminUserListPage from './pages/AdminUserListPage.jsx';
import TeacherRequestsPage from './pages/TeacherRequestsPage.jsx';
import StudentPanel from './pages/StudentPanel.jsx';
import StudentProfilePage from './pages/StudentProfilePage.jsx';
import TeacherProfilePage from './pages/TeacherProfilePage.jsx';
import TeacherMyQuizzesPage from './pages/TeacherMyQuizzesPage.jsx';
import TeacherCreateQuizPage from './pages/TeacherCreateQuizPage.jsx';
import TeacherEditQuizPage from './pages/TeacherEditQuizPage.jsx';
import TeacherQuizQuestionsPage from './pages/TeacherQuizQuestionsPage.jsx';
import StudentQuizzesPage from './pages/StudentQuizzesPage';
import SolveQuizPage from './pages/SolveQuizPage';
import QuizResultsPage from './pages/QuizResultsPage';
import StudentQuizResultsPage from './pages/StudentQuizResultsPage';
import StudentLeadershipTablePage from './pages/StudentLeadershipTablePage';
import Dashboard from './components/Dashboard';
import AdminPanel from './pages/AdminPanel.jsx';
import AdminAnnouncementsPage from './pages/AdminAnnouncementsPage.jsx';
import TeacherAnnouncementsPage from './pages/TeacherAnnouncementsPage.jsx';
import StudentAnnouncementsPage from './pages/StudentAnnouncementsPage.jsx';

// Styles
import './styles/teacher.css';
import './styles/admin.css';

function App() {
  return (
    <div className="App">
      <Routes>
        <Route path="/" element={<MainLayout />}>
          {/* Public Routes */}
          <Route index element={<MainScreen />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route path="register/student" element={<StudentRegisterPage />} />
          <Route path="register/teacher" element={<TeacherRegisterPage />} />
          <Route path="verify-email" element={<VerifyEmailPage />} />
          <Route path="password-reset" element={<PasswordResetPage />} />
          <Route path="reset-password" element={<PasswordResetPage />} />
          <Route path="logout" element={<LoginPage />} />

          {/* Admin Routes */}
          <Route element={<ProtectedRoutes requiredRoles={['ROLE_ADMIN']} />}>
            <Route path="admin" element={<AdminLayout />}>
              <Route index element={<AdminDashboardPage />} />
              <Route path="users" element={<AdminUserListPage />} />
              <Route path="teacher-requests" element={<TeacherRequestsPage />} />
              <Route path="announcements" element={<AdminAnnouncementsPage />} />
            </Route>
          </Route>

          {/* Student Routes */}
          <Route element={<ProtectedRoutes requiredRoles={['ROLE_STUDENT']} />}>
            <Route path="student" element={<StudentPanel />} />
            <Route path="student/profile" element={<StudentProfilePage />} />
            <Route path="student/announcements" element={<StudentAnnouncementsPage />} />
            <Route path="student/dashboard" element={<Dashboard />} />
            <Route path="student/solve-quiz" element={<StudentQuizzesPage />} />
            <Route path="quiz-results" element={<StudentQuizResultsPage />} />
            <Route path="quiz-results/:attemptId" element={<QuizResultsPage />} />
          </Route>

          {/* Shared Routes */}
          <Route element={<ProtectedRoutes requiredRoles={['ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN']} />}>
            <Route path="leadership-table" element={<StudentLeadershipTablePage />} />
          </Route>

          {/* Quiz Solving Route - Accessible at root level */}
          <Route element={<ProtectedRoutes requiredRoles={['ROLE_STUDENT']} />}>
            <Route path="solve-quiz/:quizId" element={<SolveQuizPage />} />
          </Route>

          {/* Teacher Routes */}
          <Route element={<ProtectedRoutes requiredRoles={['ROLE_TEACHER']} />}>
            <Route path="teacher" element={<TeacherPanel />} />
            <Route path="teacher/profile" element={<TeacherProfilePage />} />
            <Route path="teacher/my-quizzes" element={<TeacherMyQuizzesPage />} />
            <Route path="teacher/create-quiz" element={<TeacherCreateQuizPage />} />
            <Route path="teacher/edit-quiz/:quizId" element={<TeacherEditQuizPage />} />
            <Route path="teacher/quiz/:quizId/questions" element={<TeacherQuizQuestionsPage />} />
            <Route path="teacher/announcements" element={<TeacherAnnouncementsPage />} />
            <Route path="teacher/dashboard" element={<Dashboard />} />
            <Route path="leadership-table" element={<StudentLeadershipTablePage />} />
          </Route>
        </Route>
      </Routes>
    </div>
  );
}

export default App;
