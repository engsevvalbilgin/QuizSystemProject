package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.Quiz; // Entity'den dönüşüm için Quiz Entity'sini import edin

import java.time.LocalDateTime; // Tarih/saat için
import java.util.List; // Soru listesi için
import java.util.stream.Collectors; // Akış işlemleri için

// Bu DTO, API yanıtlarında bir quizin tüm detaylarını (soru listesi dahil) taşır.
public class QuizDetailsResponse {

    private int id;
    private String name; // Quiz adı
    private String description; // Açıklama
    private String teacherName; // Quizi oluşturan öğretmenin adı/kullanıcı adı
    private Integer durationMinutes; // Quiz süresi (dakika)
    private boolean isActive; // Quizin aktif olup olmadığı

    private LocalDateTime startDate; // Başlangıç tarihi
    private LocalDateTime endDate; // Bitiş tarihi

    private List<QuestionResponse> questions; // Quize ait sorular listesi (QuestionResponse DTO'su)

    // JPA için argümansız constructor
    public QuizDetailsResponse() {
    }

    // Quiz Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public QuizDetailsResponse(Quiz quiz) {
        this.id = quiz.getId();
        this.name = quiz.getName();
        this.description = quiz.getDescription();
        this.teacherName = quiz.getTeacher() != null ? quiz.getTeacher().getName() + " " + quiz.getTeacher().getSurname() : "Bilinmiyor";
        this.durationMinutes = quiz.getDurationMinutes();
        this.isActive = quiz.isActive();
        this.startDate = quiz.getStartDate();
        this.endDate = quiz.getEndDate();

        // Quiz'e ait soruları Question Entity'lerinden QuestionResponse DTO'larına dönüştür
        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
             this.questions = quiz.getQuestions().stream()
                                   .map(QuestionResponse::new) // Her Question Entity'sini QuestionResponse DTO'suna dönüştür
                                   .collect(Collectors.toList());
        } else {
             this.questions = List.of(); // Soru yoksa boş liste döndür
        }
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public List<QuestionResponse> getQuestions() { return questions; }
    public void setQuestions(List<QuestionResponse> questions) { this.questions = questions; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}