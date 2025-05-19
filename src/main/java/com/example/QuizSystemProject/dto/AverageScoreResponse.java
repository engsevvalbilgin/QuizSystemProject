package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

// Bu DTO, API yanıtlarında tek bir ortalama puan değerini taşır.
public class AverageScoreResponse {

    private int studentId; // Hangi öğrenciye ait olduğu (opsiyonel, bağlama göre değişir)
    private Double averageScore; // Ortalama puan değeri

    // JPA için argümansız constructor
    public AverageScoreResponse() {
    }

    // Alanları alan constructor
    public AverageScoreResponse(int studentId, Double averageScore) {
        this.studentId = studentId;
        this.averageScore = averageScore;
    }
     // Sadece puan alan constructor
     public AverageScoreResponse(Double averageScore) {
         this.averageScore = averageScore;
     }


    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}