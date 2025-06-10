package com.example.QuizSystemProject.Repository;
 
import com.example.QuizSystemProject.Model.AnswerAttempt; 
import com.example.QuizSystemProject.Model.Question;
import com.example.QuizSystemProject.Model.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.List; 
import java.util.Optional;
import org.springframework.stereotype.Repository; 

@Repository 
public interface AnswerAttemptRepository extends JpaRepository<AnswerAttempt,Integer  > {

    

    
    List<AnswerAttempt> findByQuizSessionId(int sessionId);
    
    
    
    Optional<AnswerAttempt> findByQuizSessionAndQuestion(QuizSession quizSession, Question question);

    List<AnswerAttempt> findByQuizSession_Quiz_Id(int quizId); 
}
