package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



import com.example.QuizSystemProject.Model.Quiz; // Quiz Entity'sini import edin
import com.example.QuizSystemProject.Model.User; // User Entity'sini import edin (Custom sorgu için gerekebilir)
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import edin
import org.springframework.stereotype.Repository; // Repository anotasyonunu import edin

import java.util.List; // Liste importu

@Repository // Spring'e bu arayüzün bir Repository olduğunu belirtir
// JpaRepository'den türeyerek temel CRUD metotlarını alır
// <Quiz, Long>: İlk parametre Entity tipi (Quiz), ikinci parametre Entity'nin Primary Key (ID) tipidir (Long).
public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    // Spring Data JPA, JpaRepository'den türediğimiz için
    // save(), findById(), findAll(), delete() gibi temel metotları otomatik sağlar.

    // İhtiyaç duyulursa buraya özel sorgu metotları tanımlanabilir.
    // Örneğin:
    // Belirli bir öğretmenin (User) oluşturduğu tüm Quizleri bulmak için (@ManyToOne User teacher alanı olduğu için)
    List<Quiz> findAllByTeacher(User teacher);

    // Aktif olan Quizleri bulmak için (isActive alanı olduğu için)
    List<Quiz> findAllByIsActiveTrue();

    // Başlangıç tarihi belirli bir tarihten sonra olan aktif Quizleri bulmak için (startDate ve isActive alanları olduğu için)
    // List<Quiz> findAllByStartDateAfterAndIsActiveTrue(LocalDateTime date); // Eğer LocalDateTime kullanılıyorsa

    // Quiz adına göre bulmak için (name alanı olduğu için)
    // List<Quiz> findByNameContainingIgnoreCase(String name); // Adında belirli bir metin geçenleri bul (büyük/küçük harf duyarsız)
}

