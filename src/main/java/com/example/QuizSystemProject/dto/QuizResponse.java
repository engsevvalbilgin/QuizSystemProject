package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.Quiz; // Entity'den dönüşüm için Quiz Entity'sini import edin
import com.example.QuizSystemProject.Model.Teacher;
import com.example.QuizSystemProject.Repository.QuizRepository;
import com.example.QuizSystemProject.Repository.TeacherRepository;

import lombok.Getter;
import lombok.Setter;

import java.security.PrivateKey;
import java.time.LocalDateTime; // Tarih/saat için

// Bu DTO, API yanıtlarında temel quiz bilgilerini taşır (örn: quiz listeleri).
// Tüm soru ve oturum listeleri gibi detayları içermez.
@Getter
@Setter
public class QuizResponse {

    private int id;
    private String name; // Quiz adı
    private String description; // Açıklama
    private String teacherName; // Quizi oluşturan öğretmenin adı/kullanıcı adı
    private int durationMinutes; // Quiz süresi (dakika)
    private boolean isActive; // Quizin aktif olup olmadığı
    private TeacherRepository teacherRepository;
    // Başlangıç/Bitiş tarihleri gibi alanları liste DTO'suna dahil etmeyebiliriz
    // veya isteğe bağlı olarak ekleyebiliriz. Şimdilik temel alanları alalım.
    // private LocalDateTime startDate;
    // private LocalDateTime endDate;


    // JPA için argümansız constructor
    public QuizResponse() {
    }

    // Quiz Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public QuizResponse(Quiz quiz) {
        this.id = quiz.getId();
        this.name = quiz.getName();
        this.description = quiz.getDescription();
        
		// Öğretmen ilişkisinden adı alalım (null kontrolü önemli)
        Teacher teacher = teacherRepository.findById(quiz.getTeacherId()).orElse(null);
        this.teacherName = teacher != null ? teacher.getName() + " " + teacher.getSurname() : "Bilinmiyor";

        this.durationMinutes = quiz.getDuration();
        this.isActive = quiz.isActive();
        // this.startDate = quiz.getStartDate();
        // this.endDate = quiz.getEndDate();
    }

    // Getter ve Setterlar
    // lombok ile otomatik oluşturuldu

   

    
}