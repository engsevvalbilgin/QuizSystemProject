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
    
    // ===== Basic CRUD operations are provided by JpaRepository =====
    // save(), findById(), findAll(), delete() etc. are available automatically
    
    // ===== Custom Query Methods =====
    
    /**
     * Finds all active quizzes with basic information
     * @return List of active quizzes with basic information
     */
    @EntityGraph(attributePaths = {"teacher"})
    @Query("SELECT DISTINCT q FROM Quiz q WHERE q.isActive = true")
    List<Quiz> findActiveQuizzes();
    
    /**
     * Finds all active quizzes with questions and options
     * @return List of active quizzes with questions and options
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions qs " +
           "LEFT JOIN qs.options " +  // Removed FETCH to avoid MultipleBagFetchException
           "LEFT JOIN FETCH q.teacher t " +
           "WHERE q.isActive = true")
    @EntityGraph(attributePaths = {"questions", "teacher"})
    List<Quiz> findActiveQuizzesWithQuestionsAndOptions();
    
    /**
     * Finds all active quizzes with basic information only (without loading questions and options)
     * More efficient when full question data is not needed
     * @return List of active quizzes with basic information
     */
    @Query("SELECT DISTINCT q FROM Quiz q WHERE q.isActive = true")
    List<Quiz> findActiveQuizzesBasic();
    
    // Get all active quizzes for a specific teacher with their questions
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.teacher = :teacher AND q.isActive = true")
    List<Quiz> findActiveQuizzesByTeacher(@Param("teacher") Teacher teacher);
    
    // Get all active quizzes for a teacher ID
    @Query("SELECT q FROM Quiz q WHERE q.teacher.id = :teacherId AND q.isActive = true")
    List<Quiz> findActiveQuizzesByTeacherId(@Param("teacherId") int teacherId);
    
    // Get all quizzes (active and inactive) for a teacher
    @Query("SELECT q FROM Quiz q WHERE q.teacher = :teacher")
    List<Quiz> findByTeacher(@Param("teacher") Teacher teacher);
    
    // ===== Deprecated methods for backward compatibility =====
    
    /**
     * @deprecated Use {@link #findActiveQuizzes()} instead
     */
    @Deprecated
    default List<Quiz> findByIsActiveTrue() {
        return findActiveQuizzes();
    }
    
    /**
     * @deprecated This method seems incorrect as it returns Teachers instead of Quizzes
     */
    @Deprecated
    List<Teacher> findByTeacherId(int teacherId);
    
    /**
     * Finds a quiz by ID with its questions and options eagerly loaded
     * @param quizId The ID of the quiz to find
     * @return An Optional containing the quiz if found, empty otherwise
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions qs " +
           "LEFT JOIN qs.options " +  // Removed FETCH to avoid MultipleBagFetchException
           "LEFT JOIN FETCH q.teacher t " +
           "WHERE q.id = :quizId")
    @EntityGraph(attributePaths = {"questions", "teacher"})
    Optional<Quiz> findByIdWithQuestions(@Param("quizId") int quizId);
    
    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN q.questions qs " +  // Removed FETCH to be consistent
           "LEFT JOIN FETCH q.teacher t " +
           "WHERE q.id = :quizId")
    @EntityGraph(attributePaths = {"questions", "teacher"})
    Optional<Quiz> findByIdWithQuestionsOnly(@Param("quizId") int quizId);
    
    // Başlangıç tarihi belirli bir tarihten sonra olan aktif Quizleri bulmak için (startDate ve isActive alanları olduğu için)
    // List<Quiz> findAllByStartDateAfterAndIsActiveTrue(LocalDateTime date); // Eğer LocalDateTime kullanılıyorsa

    // Quiz adına göre bulmak için (name alanı olduğu için)
    // List<Quiz> findByNameContainingIgnoreCase(String name); // Adında belirli bir metin geçenleri bul (büyük/küçük harf duyarsız)
}

