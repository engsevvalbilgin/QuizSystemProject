package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.QuizSession; // Entity'den dönüşüm için QuizSession Entity'sini import edin

import java.time.Duration; // Süre hesaplama için
import java.time.LocalDateTime; // Tarih/saat için
import java.util.List; // Cevap listesi için
import java.util.stream.Collectors; // Akış işlemleri için

// Bu DTO, API yanıtlarında bir quiz oturumunun tüm detaylarını (verilen cevaplar dahil) taşır.
public class QuizSessionDetailsResponse {

    private int id;
    private int quizId; // Hangi quize ait olduğu
    private String quizName; // Quizin adı
    private LocalDateTime startTime; // Başlangıç zamanı
    private LocalDateTime endTime; // Bitiş zamanı
    private int score; // Oturumda alınan puan
    private int durationMinutes; // Oturum süresi (dakika cinsinden)

    // Oturumun sahibi olan öğrencinin temel bilgileri de dahil edilebilir
    private int studentId;
    private String studentUsername;

    private List<AnswerAttemptResponse> answers; // Oturumdaki cevaplar listesi (AnswerAttemptResponse DTO'su)


    // JPA için argümansız constructor
    public QuizSessionDetailsResponse() {
    }

    // QuizSession Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public QuizSessionDetailsResponse(QuizSession session) {
        this.id = session.getId();

        // İlişkili Quiz'den bilgileri alalım
        if (session.getQuiz() != null) {
            this.quizId = session.getQuiz().getId();
            this.quizName = session.getQuiz().getName();
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
         } else {
              this.durationMinutes = 0;
         }


        // İlişkili Öğrenci'den bilgileri alalım
        if (session.getStudent() != null) {
            this.studentId = session.getStudent().getId();
            this.studentUsername = session.getStudent().getUsername();
        } else {
             this.studentId = -1;
             this.studentUsername = "Bilinmeyen Öğrenci";
        }


        // Oturumdaki AnswerAttempt Entity'lerini AnswerAttemptResponse DTO'larına dönüştür
        if (session.getAnswers() != null && !session.getAnswers().isEmpty()) {
             this.answers = session.getAnswers().stream()
                                   .map(AnswerAttemptResponse::new) // Her AnswerAttempt Entity'sini AnswerAttemptResponse DTO'suna dönüştür
                                   .collect(Collectors.toList());
        } else {
             this.answers = List.of(); // Cevap yoksa boş liste döndür
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

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }


    public List<AnswerAttemptResponse> getAnswers() { return answers; }
    public void setAnswers(List<AnswerAttemptResponse> answers) { this.answers = answers; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}