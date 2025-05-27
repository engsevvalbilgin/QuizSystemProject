package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.*;

import lombok.Data;
import java.time.Duration; // Süre hesaplama için
import java.time.LocalDateTime; // Tarih/saat için

/**
 * Bu DTO, API yanıtlarında temel quiz oturumu bilgilerini taşır (örn: öğrenci oturum listesi).
 * Tüm cevap listesi gibi detayları içermez.
 */
@Data
public class QuizSessionResponse {

    private int id;
    private int quizId; // Hangi quize ait olduğu
    private String quizName; // Quizin adı
    private LocalDateTime startTime; // Başlangıç zamanı
    private LocalDateTime endTime; // Bitiş zamanı
    private int score; // Oturumda alınan puan

    // Oturum süresi Entity'de tutulmasa bile burada hesaplanıp döndürülebilir
    private int durationMinutes; // Oturum süresi (dakika cinsinden)

    // Öğrenci bilgisi bu DTO'da genellikle olmaz (listelenen oturumlar zaten tek bir öğrenciye aittir)
    // Ama gerekirse eklenebilir.


    // JPA için argümansız constructor
    public QuizSessionResponse() {
    }

    // QuizSession Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public QuizSessionResponse(QuizSession session) {
        this.id = (int) session.getId();

        // İlişkili Quiz'den bilgileri alalım (null kontrolü önemli)
        if (session.getQuiz() != null) {
            this.quizId = (int) session.getQuiz().getId();
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
             this.durationMinutes = (int) duration.toMinutes();
        } else if (session.getStartTime() != null && session.getQuiz() != null && session.getQuiz().getDuration() != -1) {
             // Eğer oturum bitmemiş ama quizin süresi belliyse, kalan süreyi göstermek gibi daha gelişmiş senaryolar olabilir.
             // Şimdilik sadece tamamlanmış oturumun süresini veya quizin toplam süresini (eğer bitmemişse) düşünebiliriz.
             // Basit tutalım, sadece tamamlandıysa süreyi hesaplayalım veya 0 olsun.
             this.durationMinutes = 0; // Tamamlanmamışsa 0
        } else {
             this.durationMinutes = 0;
        }
    }

    // Getter ve Setter'lar @Data anotasyonu ile otomatik olarak oluşturuluyor


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}