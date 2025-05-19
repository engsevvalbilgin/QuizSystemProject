package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

// Bu DTO, API yanıtlarında tek bir ortalama puan değerini taşır.
public class AverageScoreResponse {

    private Long studentId; // Hangi öğrenciye ait olduğu (opsiyonel, bağlama göre değişir)
    private Double averageScore; // Ortalama puan değeri

    // JPA için argümansız constructor
    public AverageScoreResponse() {
    }

    // Alanları alan constructor
    public AverageScoreResponse(Long studentId, Double averageScore) {
        this.studentId = studentId;
        this.averageScore = averageScore;
    }
     // Sadece puan alan constructor
     public AverageScoreResponse(Double averageScore) {
         this.averageScore = averageScore;
     }


    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}