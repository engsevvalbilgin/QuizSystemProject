package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.QuizSession; // Entity'den dönüşüm için
import com.example.QuizSystemProject.Model.User; // Öğrenci bilgisi için

import java.util.List; // Liste importu
import java.util.stream.Collectors; // Akış işlemleri için

// Bu DTO, API yanıtlarında bir öğrencinin tüm quiz oturumu sonuçlarını özetler.
public class StudentOverallResultsResponse {

    private int studentId; // Öğrenci ID'si
    private String studentUsername; // Öğrenci kullanıcı adı
    private int totalSessions; // Tamamlanan toplam oturum sayısı
    private Double overallAverageScore; // Tüm oturumların ortalama puanı

    // Oturum listesini de buraya dahil edebiliriz veya ayrı bir endpointten çekilebilir.
    // Buraya listeyi dahil etmek, tek istekte tüm detayları getirmeyi sağlar.
    private List<QuizSessionResponse> sessions; // Oturumların temel bilgileri (QuizSessionResponse DTO'su)


    // JPA için argümansız constructor
    public StudentOverallResultsResponse() {
    }

    // Öğrenci Entity'si ve Oturum Listesi ile dönüşüm yapmayı kolaylaştıran constructor
    public StudentOverallResultsResponse(User student, List<QuizSession> sessions) {
        // Öğrenci bilgileri
        if (student != null) {
            this.studentId = student.getId();
            this.studentUsername = student.getUsername();
        } else {
            this.studentId = -1;
            this.studentUsername = "Bilinmeyen Öğrenci";
        }

        this.totalSessions = sessions != null ? sessions.size() : 0;

        // Ortalama puanı hesapla (Service'ten gelen listeyi kullan)
        double calculatedAverageScore = 0.0;
        if (sessions != null && !sessions.isEmpty()) {
            int totalScore = sessions.stream().mapToInt(QuizSession::getScore).sum();
            calculatedAverageScore = (double) totalScore / sessions.size();
        }
        this.overallAverageScore = calculatedAverageScore;

        // Oturum Entity'lerini QuizSessionResponse DTO'larına dönüştür
        if (sessions != null) {
            this.sessions = sessions.stream()
                                    .map(QuizSessionResponse::new) // Her QuizSession Entity'sini QuizSessionResponse DTO'suna dönüştür
                                    .collect(Collectors.toList());
        } else {
            this.sessions = List.of(); // Liste boşsa boş liste
        }
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public Double getOverallAverageScore() { return overallAverageScore; }
    public void setOverallAverageScore(Double overallAverageScore) { this.overallAverageScore = overallAverageScore; }

    public List<QuizSessionResponse> getSessions() { return sessions; }
    public void setSessions(List<QuizSessionResponse> sessions) { this.sessions = sessions; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}