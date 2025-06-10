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

    
    List<Question> findAllByQuiz(Quiz quiz);
    
    
    List<Question> findAllByQuizId(Integer quizId);
    
    
    List<Question> findAllByQuizIdOrderByNumberAsc(Integer quizId);

    
    @Query(value = "SELECT COUNT(*) FROM questions WHERE quiz_id = :quizId", nativeQuery = true)
    long countByQuizId(@Param("quizId") int quizId);
    
    
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId ORDER BY q.number ASC")
    List<Question> findQuestionsWithOptionsByQuizId(@Param("quizId") int quizId);
}
