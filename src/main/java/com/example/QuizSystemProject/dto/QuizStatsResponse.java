package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import java.util.Map; // Map'ten dönüşüm için (Service'ten Map dönüyorsa)

// Bu DTO, API yanıtlarında belirli bir quizin istatistiklerini taşır.
public class QuizStatsResponse {

    private Long quizId; // Hangi quize ait olduğu
    private String quizName; // Quizin adı
    private Integer totalAttempts; // Toplam çözülme sayısı (oturumu tamamlayanlar)
    private Double averageScore; // Quizdeki ortalama puan
    private Integer highestScore; // En yüksek puan
    private Integer lowestScore; // En düşük puan

    // Quizdeki soruların doğru/yanlış cevap oranları gibi daha detaylı istatistikler eklenebilir.


    // JPA için argümansız constructor
    public QuizStatsResponse() {
    }

    // Alanları alan constructor
    public QuizStatsResponse(Long quizId, String quizName, Integer totalAttempts, Double averageScore, Integer highestScore, Integer lowestScore) {
        this.quizId = quizId;
        this.quizName = quizName;
        this.totalAttempts = totalAttempts;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
    }

    // Service'ten Map dönüyorsa, Map'ten dönüşüm yapmayı kolaylaştıran constructor (isteğe bağlı)
     public QuizStatsResponse(Map<String, Object> statsMap) {
         this.quizId = (Long) statsMap.get("quizId");
         this.quizName = (String) statsMap.get("quizName");
         this.totalAttempts = (Integer) statsMap.get("totalAttempts");
         this.averageScore = (Double) statsMap.get("averageScore");
         this.highestScore = (Integer) statsMap.get("highestScore");
         this.lowestScore = (Integer) statsMap.get("lowestScore");
     }


    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getQuizName() { return quizName; }
    public void setQuizName(String quizName) { this.quizName = quizName; }

    public Integer getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(Integer totalAttempts) { this.totalAttempts = totalAttempts; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public Integer getHighestScore() { return highestScore; }
    public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }

    public Integer getLowestScore() { return lowestScore; }
    public void setLowestScore(Integer lowestScore) { this.lowestScore = lowestScore; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}