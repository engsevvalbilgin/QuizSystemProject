package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.Quiz; // Entity'den dönüşüm için Quiz Entity'sini import edin
import com.example.QuizSystemProject.Model.Teacher;

// Removed unnecessary TeacherRepository import

import lombok.Getter;
import lombok.Setter;

// Bu DTO, API yanıtlarında temel quiz bilgilerini taşır (örn: quiz listeleri).
// Tüm soru ve oturum listeleri gibi detayları içermez.
@Getter
@Setter
public class QuizResponse {

    private int id;
    private int teacherId;
    private String name; // Quiz adı
    private String description; // Açıklama
    private String topic; // Konu
    private String teacherName; // Quizi oluşturan öğretmenin adı/kullanıcı adı
    private int durationMinutes; // Quiz süresi (dakika)
    private boolean isActive; // Quizin aktif olup olmadığı
    private int questionCount; // Soru sayısı
    // Removed TeacherRepository field which was causing null reference issue
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
        if (quiz.getTeacher() != null) { // Add null check
            this.teacherId = quiz.getTeacher().getId();
        } else {
            this.teacherId = 0; // Or handle as appropriate
        }
        this.name = quiz.getName();
        this.description = quiz.getDescription();
        this.topic = quiz.getTopic();
        
        // Öğretmen bilgilerini doğrudan Quiz entity'sinden alalım (null kontrolü önemli)
        if (quiz.getTeacher() != null) {
            Teacher teacher = quiz.getTeacher();
            this.teacherName = teacher.getName() != null && teacher.getSurname() != null 
                ? teacher.getName() + " " + teacher.getSurname() 
                : teacher.getUsername();
        } else {
            this.teacherName = "Bilinmiyor";
        }

        this.durationMinutes = quiz.getDuration();
        this.isActive = quiz.isActive();
        // Soru sayısını quiz nesnesi üzerinden al
        this.questionCount = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        // this.startDate = quiz.getStartDate();
        // this.endDate = quiz.getEndDate();
    }

    // Getter ve Setterlar
    // lombok ile otomatik oluşturuldu

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
}