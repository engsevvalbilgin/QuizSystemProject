package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.QuestionType; // QuestionType Entity'sini import edin
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import edin
import org.springframework.stereotype.Repository; // Repository anotasyonunu import edin

import java.util.Optional; // Optional importu (Bulunamama durumuna karşı)

@Repository // Spring'e bu arayüzün bir Repository olduğunu belirtir
// JpaRepository'den türeyerek temel CRUD metotlarını alır

public interface QuestionTypeRepository extends JpaRepository<QuestionType, Integer> {

    // Spring Data JPA, JpaRepository'den türediğimiz için
    // save(), findById(), findAll(), delete() gibi temel metotları otomatik sağlar.

    // İhtiyaç duyulursa buraya özel sorgu metotları tanımlanabilir.
    // Örneğin:
    // Type adına göre QuestionType'ı bulmak için (typeName alanı benzersiz olduğu için)
    Optional<QuestionType> findByTypeName(String typeName);

}

