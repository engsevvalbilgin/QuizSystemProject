package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.*;

import java.time.Duration; // Süre hesaplama için
import java.time.LocalDateTime; // Tarih/saat için

// Bu DTO, API yanıtlarında temel quiz oturumu bilgilerini taşır (örn: öğrenci oturum listesi).
// Tüm cevap listesi gibi detayları içermez.
public class QuizSessionResponse {

    private int id;
    private int quizId; // Hangi quize ait olduğu
    private String quizName; // Quizin adı
    private LocalDateTime startTime; // Başlangıç zamanı
    private LocalDateTime endTime; // Bitiş zamanı
    private int score; // Oturumda alınan puan

    // Oturum süresi Entity'de tutulmasa bile burada hesaplanıp döndürülebilir
    private Long durationMinutes; // Oturum süresi (dakika cinsinden)

    // Öğrenci bilgisi bu DTO'da genellikle olmaz (listelenen oturumlar zaten tek bir öğrenciye aittir)
    // Ama gerekirse eklenebilir.


    // JPA için argümansız constructor
    public QuizSessionResponse() {
    }

    // QuizSession Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public QuizSessionResponse(QuizSession session) {
        this.id = session.getId();

        // İlişkili Quiz'den bilgileri alalım (null kontrolü önemli)
        if (session.getQuiz() != null) {
            this.quizId = session.getQuiz().getId();
            this.quizName = session.getQuiz().getName();
            // İstenirse quizin durationMinutes da alınabilir: session.getQuiz().getDurationMinutes();
        } else {
            this.quizId = -1;
            this.quizName = "Bilinmeyen Quiz";
        }

        this.startTime = session.getStartTime();
        this.endTime = session.getEndTime();
        this.score = session.getScore();

        // Süreyi hesaplayıp DTO alanına set edelim (Null kontrolleri önemli)
        if (session.getStartTime() != null && session.getEndTime() != null) {
             Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
             this.durationMinutes = duration.toMinutes();
        } else if (session.getStartTime() != null && session.getQuiz() != null && session.getQuiz().getDuration() != -1) {
             // Eğer oturum bitmemiş ama quizin süresi belliyse, kalan süreyi göstermek gibi daha gelişmiş senaryolar olabilir.
             // Şimdilik sadece tamamlanmış oturumun süresini veya quizin toplam süresini (eğer bitmemişse) düşünebiliriz.
             // Basit tutalım, sadece tamamlandıysa süreyi hesaplayalım veya null kalsın.
             this.durationMinutes = null; // Tamamlanmamışsa null
        } else {
             this.durationMinutes = null;
        }
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getQuizName() { return quizName; }
    public void setQuizName(String quizName) { this.quizName = quizName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}