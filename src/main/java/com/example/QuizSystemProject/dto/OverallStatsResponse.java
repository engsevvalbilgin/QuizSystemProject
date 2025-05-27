package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import java.util.Map; // Map'ten dönüşüm için (Service'ten Map dönüyorsa)

// Bu DTO, API yanıtlarında programın genel istatistiklerini taşır.
public class OverallStatsResponse {

    private int totalUsers; // Toplam kullanıcı sayısı
    private int totalQuizzes; // Toplam quiz sayısı
    private int totalQuizSessions; // Toplam quiz oturumu sayısı
    private Double averageScore; // Tüm oturumların genel ortalama puanı

    // İstenirse Adminler için aktif/pasif kullanıcı sayıları gibi ek alanlar eklenebilir.


    // JPA için argümansız constructor
    public OverallStatsResponse() {
    }

    // Alanları alan constructor
    public OverallStatsResponse(int totalUsers, int totalQuizzes, int totalQuizSessions, Double averageScore) {
        this.totalUsers = totalUsers;
        this.totalQuizzes = totalQuizzes;
        this.totalQuizSessions = totalQuizSessions;
        this.averageScore = averageScore;
    }

    // Service'ten Map dönüyorsa, Map'ten dönüşüm yapmayı kolaylaştıran constructor (isteğe bağlı)
    public OverallStatsResponse(Map<String, Object> statsMap) {
        this.totalUsers = ((Number) statsMap.get("totalUsers")).intValue();
        this.totalQuizzes = ((Number) statsMap.get("totalQuizzes")).intValue();
        this.totalQuizSessions = ((Number) statsMap.get("totalQuizSessions")).intValue();
        this.averageScore = (Double) statsMap.get("averageScore");
    }


    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(int totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public int getTotalQuizSessions() { return totalQuizSessions; }
    public void setTotalQuizSessions(int totalQuizSessions) { this.totalQuizSessions = totalQuizSessions; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}