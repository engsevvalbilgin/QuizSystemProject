package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import java.util.Map; // Map'ten dönüşüm için (Service'ten Map dönüyorsa)

// Bu DTO, API yanıtlarında programın genel istatistiklerini taşır.
public class OverallStatsResponse {

    private Long totalUsers; // Toplam kullanıcı sayısı
    private Long totalQuizzes; // Toplam quiz sayısı
    private Long totalQuizSessions; // Toplam quiz oturumu sayısı
    private Double averageScore; // Tüm oturumların genel ortalama puanı

    // İstenirse Adminler için aktif/pasif kullanıcı sayıları gibi ek alanlar eklenebilir.


    // JPA için argümansız constructor
    public OverallStatsResponse() {
    }

    // Alanları alan constructor
    public OverallStatsResponse(Long totalUsers, Long totalQuizzes, Long totalQuizSessions, Double averageScore) {
        this.totalUsers = totalUsers;
        this.totalQuizzes = totalQuizzes;
        this.totalQuizSessions = totalQuizSessions;
        this.averageScore = averageScore;
    }

    // Service'ten Map dönüyorsa, Map'ten dönüşüm yapmayı kolaylaştıran constructor (isteğe bağlı)
    public OverallStatsResponse(Map<String, Object> statsMap) {
        this.totalUsers = (Long) statsMap.get("totalUsers");
        this.totalQuizzes = (Long) statsMap.get("totalQuizzes");
        this.totalQuizSessions = (Long) statsMap.get("totalQuizSessions");
        this.averageScore = (Double) statsMap.get("averageScore");
    }


    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(Long totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public Long getTotalQuizSessions() { return totalQuizSessions; }
    public void setTotalQuizSessions(Long totalQuizSessions) { this.totalQuizSessions = totalQuizSessions; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}