package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.Teacher;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
	Optional<Teacher> findById(int id);
	
	// User tablosundan Teacher tipindeki kayıtları getirmek için özel bir sorgu
	// Teacher doğrudan User'sınıfını genişletiyor, o yüzden doğrudan id kullanıyoruz
	@Query("SELECT t FROM Teacher t WHERE t.id = :userId")
	Optional<Teacher> findTeacherByUserId(@Param("userId") int userId);
}

