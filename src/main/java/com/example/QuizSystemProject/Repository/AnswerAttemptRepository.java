package com.example.QuizSystemProject.Repository;
 // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.AnswerAttempt; // AnswerAttempt Entity'sini import edin
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import edin
import org.springframework.stereotype.Repository; // Repository anotasyonunu import edin

@Repository // Spring'e bu arayüzün bir Repository olduğunu belirtir
// JpaRepository'den türeyerek temel CRUD metotlarını alır
// <AnswerAttempt, Long>: İlk parametre Entity tipi (AnswerAttempt), ikinci parametre Entity'nin Primary Key (ID) tipidir (Long).
public interface AnswerAttemptRepository extends JpaRepository<AnswerAttempt, Long> {

    // Spring Data JPA, JpaRepository'den türediğimiz için
    // save(), findById(), findAll(), delete() gibi temel metotları otomatik sağlar.

    // İhtiyaç duyulursa buraya özel sorgu metotları tanımlanabilir.
    // Örneğin:
    // List<AnswerAttempt> findByQuizSession(QuizSession quizSession); // Belirli bir oturumdaki cevapları bul
    // List<AnswerAttempt> findByQuestionAndQuizSession(Question question, QuizSession quizSession); // Belirli bir soru ve oturumdaki cevabı bul

}
