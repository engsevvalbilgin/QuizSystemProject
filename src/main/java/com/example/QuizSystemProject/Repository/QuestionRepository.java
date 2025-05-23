package com.example.QuizSystemProject.Repository;
import com.example.QuizSystemProject.Model.Question;
import com.example.QuizSystemProject.Model.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    // Bir quiz'e ait tüm soruları getir
    List<Question> findAllByQuiz(Quiz quiz);
    
    // Quiz ID'sine göre soruları getir
    List<Question> findAllByQuizId(Integer quizId);
    
    // Quiz ID'sine göre soruları numara sırasına göre getir
    List<Question> findAllByQuizIdOrderByNumberAsc(Integer quizId);

    // Bir quiz'in soru sayısını native SQL sorgusu ile hesapla
    @Query(value = "SELECT COUNT(*) FROM questions WHERE quiz_id = :quizId", nativeQuery = true)
    int countByQuizId(@Param("quizId") int quizId);
}
