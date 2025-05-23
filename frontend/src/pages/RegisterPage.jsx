import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/teacher.css';

function RegisterPage() {
  const navigate = useNavigate();

  const handleRoleSelect = (role) => {
    if (role === 'student') {
      navigate('/register/student');
    } else if (role === 'teacher') {
      navigate('/register/teacher');
    }
  };

  return (
    <div className="register-role-select">
      <h2>Kayıt Türünü Seçin</h2>
      <div className="role-buttons">
        <button 
          className="role-button student-button" 
          onClick={() => handleRoleSelect('student')}
        >
          Öğrenciyim
        </button>
        <button 
          className="role-button teacher-button" 
          onClick={() => handleRoleSelect('teacher')}
        >
          Öğretmen Olmak İçin Başvur
        </button>
      </div>
    </div>
  );
}

export default RegisterPage;
