package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.QuizSession; // Entity'den dönüşüm için
import com.example.QuizSystemProject.Model.User; // Öğrenci bilgisi için

import java.util.List; // Liste importu
import java.util.stream.Collectors; // Akış işlemleri için

// Bu DTO, API yanıtlarında bir öğrencinin tüm quiz oturumu sonuçlarını özetler.
public class StudentOverallResultsResponse {

    private int id; // Öğrenci ID'si
    private String name; // Öğrenci adı
    private String surname; // Öğrenci soyadı
    private int totalQuizzes; // Tamamlanan toplam quiz sayısı
    private double averageScore; // Tüm quizlerin ortalama puanı
    private int successfulQuizzes; // Başarılı quiz sayısı

    // JPA için argümansız constructor
    public StudentOverallResultsResponse() {
    }

    // Öğrenci Entity'si ve Oturum Listesi ile dönüşüm yapmayı kolaylaştıran constructor
    public StudentOverallResultsResponse(User student, List<QuizSession> sessions) {
        // Öğrenci bilgileri
        if (student != null) {
            this.id = student.getId();
            this.name = student.getName();
            this.surname = student.getSurname();
        } else {
            this.id = -1;
            this.name = "Bilinmeyen";
            this.surname = "Öğrenci";
        }

        this.totalQuizzes = sessions != null ? sessions.size() : 0;

        // Ortalama puanı hesapla (Service'ten gelen listeyi kullan)
        double calculatedAverageScore = 0.0;
        if (sessions != null && !sessions.isEmpty()) {
            int totalScore = sessions.stream().mapToInt(QuizSession::getScore).sum();
            calculatedAverageScore = (double) totalScore / sessions.size();
        }
        this.averageScore = calculatedAverageScore;

        // Başarılı quiz sayısı hesapla
        int successfulQuizzesCount = 0;
        if (sessions != null) {
            successfulQuizzesCount = (int) sessions.stream()
                    .filter(QuizSession::isCompleted)
                    .count();
        }
        this.successfulQuizzes = successfulQuizzesCount;
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public int getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(int totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public int getSuccessfulQuizzes() { return successfulQuizzes; }
    public void setSuccessfulQuizzes(int successfulQuizzes) { this.successfulQuizzes = successfulQuizzes; }
}