package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.Quiz;
import com.example.QuizSystemProject.Model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    
    
    @EntityGraph(attributePaths = {"teacher"})
    @Query("SELECT DISTINCT q FROM Quiz q WHERE q.isActive = true")
    List<Quiz> findActiveQuizzes();
    
    
    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions qs " +
           "LEFT JOIN qs.options " +  
           "LEFT JOIN FETCH q.teacher t " +
           "WHERE q.isActive = true")
    @EntityGraph(attributePaths = {"questions", "teacher"})
    List<Quiz> findActiveQuizzesWithQuestionsAndOptions();
    
   
    @Query("SELECT DISTINCT q FROM Quiz q WHERE q.isActive = true")
    List<Quiz> findActiveQuizzesBasic();
    
    
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.teacher = :teacher AND q.isActive = true")
    List<Quiz> findActiveQuizzesByTeacher(@Param("teacher") Teacher teacher);
    
    
    @Query("SELECT q FROM Quiz q WHERE q.teacher.id = :teacherId AND q.isActive = true")
    List<Quiz> findActiveQuizzesByTeacherId(@Param("teacherId") int teacherId);
    
    
    @Query("SELECT q FROM Quiz q WHERE q.teacher = :teacher")
    List<Quiz> findByTeacher(@Param("teacher") Teacher teacher);
    
   
    @Deprecated
    default List<Quiz> findByIsActiveTrue() {
        return findActiveQuizzes();
    }
    
    
    @Deprecated
    List<Teacher> findByTeacherId(int teacherId);
    
    
    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions qs " +
           "LEFT JOIN qs.options " +  
           "LEFT JOIN FETCH q.teacher t " +
           "WHERE q.id = :quizId")
    @EntityGraph(attributePaths = {"questions", "teacher"})
    Optional<Quiz> findByIdWithQuestions(@Param("quizId") int quizId);
    
    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN q.questions qs " +  
           "LEFT JOIN FETCH q.teacher t " +
           "WHERE q.id = :quizId")
    @EntityGraph(attributePaths = {"questions", "teacher"})
    Optional<Quiz> findByIdWithQuestionsOnly(@Param("quizId") int quizId);
    
    
}

