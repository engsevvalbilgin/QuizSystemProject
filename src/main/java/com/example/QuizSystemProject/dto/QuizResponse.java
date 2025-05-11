package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.Quiz; // Entity'den dönüşüm için Quiz Entity'sini import edin

import java.time.LocalDateTime; // Tarih/saat için

// Bu DTO, API yanıtlarında temel quiz bilgilerini taşır (örn: quiz listeleri).
// Tüm soru ve oturum listeleri gibi detayları içermez.
public class QuizResponse {

    private int id;
    private String name; // Quiz adı
    private String description; // Açıklama
    private String teacherName; // Quizi oluşturan öğretmenin adı/kullanıcı adı
    private Integer durationMinutes; // Quiz süresi (dakika)
    private boolean isActive; // Quizin aktif olup olmadığı

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
        this.teacherName = quiz.getTeacherId() != -1 ? quiz.getTeacherId().getName() + " " + quiz.getTeacherId().getSurname() : "Bilinmiyor";
        this.durationMinutes = quiz.getDuration();
        this.isActive = quiz.isActive();
        // this.startDate = quiz.getStartDate();
        // this.endDate = quiz.getEndDate();
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Eğer startDate/endDate eklediyseniz:
    // public LocalDateTime getStartDate() { return startDate; }
    // public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    // public LocalDateTime getEndDate() { return endDate; }
    // public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}