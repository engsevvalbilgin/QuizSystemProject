package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import java.util.Map; // Map'ten dönüşüm için (Service'ten Map dönüyorsa)

// Bu DTO, API yanıtlarında belirli bir quizin istatistiklerini taşır.
public class QuizStatsResponse {

    private int quizId; // Hangi quize ait olduğu
    private String quizName; // Quizin adı
    private long totalAttempts; // Toplam çözülme sayısı (oturumu tamamlayanlar)
    private Double averageScore; // Quizdeki ortalama puan
    private Integer highestScore; // En yüksek puan
    private Integer lowestScore; // En düşük puan
    private long totalQuestions; // Quizdeki toplam soru sayısı

    // Quizdeki soruların doğru/yanlış cevap oranları gibi daha detaylı istatistikler eklenebilir.


    // JPA için argümansız constructor
    public QuizStatsResponse() {
    }

    // Alanları alan constructor
    public QuizStatsResponse(int quizId, String quizName, long totalAttempts, Double averageScore, Integer highestScore, Integer lowestScore, long totalQuestions) {
        this.quizId = quizId;
        this.quizName = quizName;
        this.totalAttempts = totalAttempts;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.totalQuestions = totalQuestions;
    }

    // Service'ten Map dönüyorsa, Map'ten dönüşüm yapmayı kolaylaştıran constructor (isteğe bağlı)
     public QuizStatsResponse(Map<String, Object> statsMap) {
         this.quizId = ((Number) statsMap.get("quizId")).intValue();
         this.quizName = (String) statsMap.get("quizName");
         this.totalAttempts = ((Number) statsMap.get("totalAttempts")).longValue();
         this.averageScore = (Double) statsMap.get("averageScore");
         this.highestScore = (Integer) statsMap.get("highestScore");
         this.lowestScore = (Integer) statsMap.get("lowestScore");
         this.totalQuestions = ((Number) statsMap.get("totalQuestions")).longValue(); // For Map constructor
     }


    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getQuizName() { return quizName; }
    public void setQuizName(String quizName) { this.quizName = quizName; }

    public long getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(long totalAttempts) { this.totalAttempts = totalAttempts; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public Integer getHighestScore() { return highestScore; }
    public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }

    public Integer getLowestScore() { return lowestScore; }
    public void setLowestScore(Integer lowestScore) { this.lowestScore = lowestScore; }

    public long getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(long totalQuestions) { this.totalQuestions = totalQuestions; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}