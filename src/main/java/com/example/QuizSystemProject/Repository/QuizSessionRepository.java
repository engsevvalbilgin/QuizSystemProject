package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.QuizSession; 
import com.example.QuizSystemProject.Model.User; 
import com.example.QuizSystemProject.Model.Quiz; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param; 
import org.springframework.stereotype.Repository; 

import java.util.List; 
import java.util.Optional; 

@Repository 
public interface QuizSessionRepository extends JpaRepository<QuizSession, Integer> {

    

    
    List<QuizSession> findAllByStudent(User student);

    
    List<QuizSession> findAllByQuiz(Quiz quiz);

    
    Optional<QuizSession> findByStudentAndQuiz(User student, Quiz quiz);

    
    Optional<QuizSession> findTopByStudentAndQuizOrderByStartTimeDesc(User student, Quiz quiz);
    
    
    List<QuizSession> findByQuizIdAndStudentId(int quizId, int studentId);
    
    
    @Query("SELECT qs FROM QuizSession qs WHERE qs.student.id = :studentId")
    List<QuizSession> findByStudentId(@Param("studentId") int studentId);
   
    @Query("SELECT qs FROM QuizSession qs WHERE qs.student.id = :studentId AND qs.endTime IS NOT NULL")
    List<QuizSession> findSubmittedByStudentId(@Param("studentId") int studentId);
    
   
    @Query("SELECT DISTINCT s FROM QuizSession s " +
       "LEFT JOIN FETCH s.quiz q " +
       "LEFT JOIN FETCH q.teacher t " +
       "LEFT JOIN FETCH q.questions qu " +
       "WHERE s.student.id = :studentId")
List<QuizSession> findByStudentIdWithDetails(@Param("studentId") int studentId);
   
    @Query("SELECT qs FROM QuizSession qs WHERE qs.student.id = :studentId AND qs.quiz.id = :quizId AND qs.endTime IS NULL")
    Optional<QuizSession> findActiveSessionByStudentAndQuiz(@Param("studentId") int studentId, @Param("quizId") int quizId);
}

