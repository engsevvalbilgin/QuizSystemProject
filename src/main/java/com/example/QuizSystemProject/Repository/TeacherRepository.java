package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.Teacher;
import com.example.QuizSystemProject.Model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
	Optional<Teacher> findById(int Id);
	
	// Gerekirse özel sorgular burada tanımlanabilir
}

