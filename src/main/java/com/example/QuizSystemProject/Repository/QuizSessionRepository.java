package com.example.QuizSystemProject.Repository;


import com.example.QuizSystemProject.Model.QuizSession; // QuizSession Entity'sini import edin
import com.example.QuizSystemProject.Model.User; // User Entity'sini import edin (Custom sorgu için gerekebilir)
import com.example.QuizSystemProject.Model.Quiz; // Quiz Entity'sini import edin (Custom sorgu için gerekebilir)
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import edin
import org.springframework.stereotype.Repository; // Repository anotasyonunu import edin

import java.util.List; // Liste importu
import java.util.Optional; // Optional importu

@Repository // Spring'e bu arayüzün bir Repository olduğunu belirtir
// JpaRepository'den türeyerek temel CRUD metotlarını alır
// <QuizSession, Integer>: İlk parametre Entity tipi (QuizSession), ikinci parametre Entity'nin Primary Key (ID) tipidir (Integer).
public interface QuizSessionRepository extends JpaRepository<QuizSession, Integer> {

    // Spring Data JPA, JpaRepository'den türediğimiz için
    // save(), findById(), findAll(), delete() gibi temel metotları otomatik sağlar.

    // İhtiyaç duyulursa buraya özel sorgu metotları tanımlanabilir.
    // Örneğin:
    // Belirli bir öğrencinin (User) tüm Quiz Oturumlarını bulmak için (@ManyToOne User student alanı olduğu için)
    List<QuizSession> findAllByStudent(User student);

    // Belirli bir Quize ait tüm Quiz Oturumlarını bulmak için (@ManyToOne Quiz quiz alanı olduğu için)
    List<QuizSession> findAllByQuiz(Quiz quiz);

    // Belirli bir öğrencinin, belirli bir Quize ait oturumu bulmak için (varsa)
    Optional<QuizSession> findByStudentAndQuiz(User student, Quiz quiz);

    // Belirli bir öğrencinin, belirli bir Quize ait en son oturumunu bulmak için (varsa)
    Optional<QuizSession> findTopByStudentAndQuizOrderByStartTimeDesc(User student, Quiz quiz);


}

