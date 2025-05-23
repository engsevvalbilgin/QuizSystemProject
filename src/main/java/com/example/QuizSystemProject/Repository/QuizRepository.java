package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.Quiz;
import com.example.QuizSystemProject.Model.Teacher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




// User artık kullanılmıyor


import java.util.List; // Liste importu

@Repository // Spring'e bu arayüzün bir Repository olduğunu belirtir
// JpaRepository'den türeyerek temel CRUD metotlarını alır
// <Quiz, Integer>: İlk parametre Entity tipi (Quiz), ikinci parametre Entity'nin Primary Key (ID) tipidir (Integer).
public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    // Find quizzes by teacher ID
    List<Quiz> findQuizzesByTeacherId(int teacherId);

    // Spring Data JPA, JpaRepository'den türediğimiz için
    // save(), findById(), findAll(), delete() gibi temel metotları otomatik sağlar.

    // İhtiyaç duyulursa buraya özel sorgu metotları tanımlanabilir.
    // Örneğin:
    // Belirli bir öğretmenin oluşturduğu tüm Quizleri bulmak için (@ManyToOne Teacher teacher alanı olduğu için)
    List<Quiz> findAllByTeacher(Teacher teacher);

    // Aktif olan Quizleri bulmak için (isActive alanı olduğu için)
    List<Quiz> findAllByIsActiveTrue();
    List<Teacher> findByTeacherId(int teacherId);
    // Başlangıç tarihi belirli bir tarihten sonra olan aktif Quizleri bulmak için (startDate ve isActive alanları olduğu için)
    // List<Quiz> findAllByStartDateAfterAndIsActiveTrue(LocalDateTime date); // Eğer LocalDateTime kullanılıyorsa

    // Quiz adına göre bulmak için (name alanı olduğu için)
    // List<Quiz> findByNameContainingIgnoreCase(String name); // Adında belirli bir metin geçenleri bul (büyük/küçük harf duyarsız)
}

