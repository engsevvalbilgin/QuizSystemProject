package com.example.QuizSystemProject.Model;


import jakarta.persistence.*; // JPA anotasyonları için
import java.time.LocalDateTime; // Tarih/saat için
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // equals/hashCode için

@Entity // Bu sınıfın bir JPA Entity'si olduğunu belirtir
@Table(name = "quiz_sessions") // Veritabanındaki tablonun adı 'quiz_sessions' olacak
public class QuizSession {

    @Id // Birincil anahtar
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Otomatik artan ID
    private int id; // int tipinde ID

    // Quiz oturumunu alan öğrenciyi belirten ilişki
    // Sizin template'inizdeki 'studentId' alanına karşılık gelir
    @ManyToOne // Bir oturumun SADECE bir öğrencisi olur, ama bir öğrenci birden çok oturum açabilir (Çoğa-Bir ilişki)
    @JoinColumn(name = "student_id", nullable = false) // Veritabanındaki yabancı anahtar sütununun adı 'student_id' olacak. Boş olamaz.
    private User student; // İlişkili User objesi (Rolü 'STUDENT' olmalı)

    // Quiz oturumunun hangi quize ait olduğunu belirten ilişki
    // Sizin template'inizdeki 'quizId' alanına karşılık gelir
    @ManyToOne // Bir oturumun SADECE bir quizi olur, ama bir quizin birden çok oturumu olabilir (Çoğa-Bir ilişki)
    @JoinColumn(name = "quiz_id", nullable = false) // Veritabanındaki yabancı anahtar sütununun adı 'quiz_id' olacak. Boş olamaz.
    private Quiz quiz; // İlişkili Quiz objesi

    @Column(nullable = false) // Boş olamaz
    private LocalDateTime startTime; // Quiz'e başlangıç tarihi/saati

    @Column // Boş olabilir (eğer quiz tamamlanmadıysa)
    private LocalDateTime endTime; // Quiz'in bitiş tarihi/saati

    @Column(nullable = false) // Boş olamaz, varsayılan 0
    private int score = 0; // Öğrencinin bu oturumda aldığı puan (Analiz sonucunda eklendi)

    // Bir quiz oturumunda, öğrencinin birden çok soruya verdiği cevap olabilir
    // Sizin 'QuestionAnswer' template'inize karşılık gelen 'AnswerAttempt' Entity'si ile ilişki
    @OneToMany(mappedBy = "quizSession", cascade = CascadeType.ALL, orphanRemoval = true) // Bir Oturumun ÇOĞU bir AnswerAttempt'i vardır.
    // mappedBy = "quizSession": İlişkinin AnswerAttempt Entity'sindeki 'quizSession' alanı tarafından yönetildiğini belirtir.
    // cascade = CascadeType.ALL: Oturum silindiğinde ilgili cevapları da siler.
    // orphanRemoval = true: Bir cevap listeden çıkarılırsa veritabanından da silinir.
    private List<AnswerAttempt> answers = new ArrayList<>(); // Bu oturumdaki öğrenci cevapları listesi

    // JPA için argümansız constructor
    public QuizSession() {
         this.answers = new ArrayList<>(); // Liste boş başlatılmalı
    }

    // Temel alanları alan constructor (ID, tarihler, score otomatik yönetilir)
    // İlişkili objeler (student, quiz) constructora dahil edilebilir veya sonradan set edilebilir
    public QuizSession(User student, Quiz quiz) {
        this.student = student;
        this.quiz = quiz;
        this.startTime = LocalDateTime.now(); // Oturum başladığında otomatik set edelim
        this.score = 0; // Başlangıç puanı 0
        this.answers = new ArrayList<>(); // Liste boş başlatılmalı
    }

    // Getter ve Setter Metotları
    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // ID setter'ı genellikle kullanılmaz

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public List<AnswerAttempt> getAnswers() { return answers; }
    public void setAnswers(List<AnswerAttempt> answers) { this.answers = answers; }

    // İlişkiye cevap eklemek için yardımcı metot
    // Bu metot, ilişkinin her iki tarafını da doğru kurar (QuizSession -> AnswerAttempt ve AnswerAttempt -> QuizSession)
    public void addAnswerAttempt(AnswerAttempt answerAttempt) {
        answers.add(answerAttempt);
        answerAttempt.setQuizSession(this); // AnswerAttempt'in ait olduğu oturumu da set etmeyi unutmayın!
    }

    // İlişkiden cevap çıkarmak için yardımcı metot
    public void removeAnswerAttempt(AnswerAttempt answerAttempt) {
        answers.remove(answerAttempt);
        answerAttempt.setQuizSession(null); // AnswerAttempt'in oturum ilişkisini kaldırın
    }


    // equals() ve hashCode() (ID üzerinden)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizSession that = (QuizSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString() (Debugging için)
    @Override
    public String toString() {
        return "QuizSession{" +
               "id=" + id +
               ", student=" + (student != null ? student.getUsername() : "null") +
               ", quiz=" + (quiz != null ? quiz.getName() : "null") +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", score=" + score +
               '}';
    }

    // --- NOT: Template kodunuzdaki calculateDuration() metodu Entity sınıfına ait değil.
    // --- Bu metot ve quiz çözme akışındaki diğer iş mantığı QuizSessionService sınıfında yer alacak.
}

