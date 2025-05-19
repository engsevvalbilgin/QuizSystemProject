package com.example.QuizSystemProject.Repository;


import com.example.QuizSystemProject.Model.Announcement; // Announcement Entity'sini import edin
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import edin
import org.springframework.stereotype.Repository; // Repository anotasyonunu import edin

@Repository // Spring'e bu arayüzün bir Repository (veri erişim bileşeni) olduğunu belirtir
// JpaRepository'den türeyerek temel CRUD metotlarını alırız
// <Announcement, Long>: İlk parametre Entity tipi (Announcement), ikinci parametre Entity'nin Primary Key (ID) tipidir (Long).
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // Spring Data JPA, JpaRepository'den türediğimiz için
    // save(), findById(), findAll(), delete() gibi temel metotları otomatik sağlar.

    // İhtiyaç duyulursa buraya özel sorgu metotları tanımlanabilir.
    // Örneğin:
    // List<Announcement> findByPublisher(User publisher); // Belirli bir yayıncının duyurularını bul
    // List<Announcement> findByTitleContaining(String title); // Başlığında belirli bir metin geçen duyuruları bul

}

