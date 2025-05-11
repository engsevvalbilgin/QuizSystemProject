package com.example.QuizSystemProject.Repository;
import com.example.QuizSystemProject.Model.Question; // Question Entity'sini import edin
import com.example.QuizSystemProject.Model.TakeQuiz;
import com.example.QuizSystemProject.Model.Quiz; // Quiz Entity'sini import edin (Custom sorgu için gerekebilir)
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import edin
import org.springframework.stereotype.Repository; // Repository anotasyonunu import edin

import java.util.List; // Liste importu

@Repository // Spring'e bu arayüzün bir Repository olduğunu belirtir
// JpaRepository'den türeyerek temel CRUD metotlarını alır
// <Question, Long>: İlk parametre Entity tipi (Question), ikinci parametre Entity'nin Primary Key (ID) tipidir (Long).
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    // Spring Data JPA, JpaRepository'den türediğimiz için
    // save(), findById(), findAll(), delete() gibi temel metotları otomatik sağlar.

    // İhtiyaç duyulursa buraya özel sorgu metotları tanımlanabilir.
    // Örneğin:
    // Bir Quize ait tüm soruları getirmek için (Question Entity'sinde @ManyToOne Quiz quiz alanı olduğu için)
    List<Question> findAllByQuiz(TakeQuiz quiz);
    // veya Quiz ID'sine göre
    List<Question> findAllByQuizId(int quizId);

    // Quiz içindeki soru numarasına göre bulmak için (quiz ve number alanları olduğu için)
    // Optional<Question> findByQuizAndNumber(Quiz quiz, int number); // Eğer soru numarası benzersizse quiz içinde

}
