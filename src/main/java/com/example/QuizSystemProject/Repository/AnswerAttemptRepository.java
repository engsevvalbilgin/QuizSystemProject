package com.example.QuizSystemProject.Repository;
 // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.AnswerAttempt; // AnswerAttempt Entity'sini import edin
import com.example.QuizSystemProject.Model.Question;
import com.example.QuizSystemProject.Model.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import edin
import java.util.List; // Added import for List
import java.util.Optional;
import org.springframework.stereotype.Repository; // Repository anotasyonunu import edin

@Repository // Spring'e bu arayüzün bir Repository olduğunu belirtir
// JpaRepository'den türeyerek temel CRUD metotlarını alır

public interface AnswerAttemptRepository extends JpaRepository<AnswerAttempt,Integer  > {

    // Spring Data JPA, JpaRepository'den türediğimiz için
    // save(), findById(), findAll(), delete() gibi temel metotları otomatik sağlar.

    // Quiz oturumuna göre tüm cevap denemelerini getirir
    List<AnswerAttempt> findByQuizSessionId(int sessionId);
    
    // İhtiyaç duyulursa buraya özel sorgu metotları tanımlanabilir.
    // Örneğin:
    // List<AnswerAttempt> findByQuizSession(QuizSession quizSession); // Belirli bir oturumdaki cevapları bul
    // List<AnswerAttempt> findByQuestionAndQuizSession(Question question, QuizSession quizSession); // Belirli bir soru ve oturumdaki cevabı bul

    Optional<AnswerAttempt> findByQuizSessionAndQuestion(QuizSession quizSession, Question question);

    List<AnswerAttempt> findByQuizSession_Quiz_Id(int quizId); // Method to find attempts by quiz ID via QuizSession
}
