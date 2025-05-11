package com.example.QuizSystemProject.Model;

 // Paket adınızın doğru olduğundan emin olun

import jakarta.persistence.*; // JPA anotasyonları için
import java.util.HashSet; // Set kullanacağız, çünkü seçilen şıkların sırası veya tekrarı önemli değil
import java.util.Objects; // equals/hashCode için
import java.util.Set; // Set kullanacağız

@Entity // Bu sınıfın bir JPA Entity'si olduğunu belirtir
@Table(name = "answer_attempts") // Veritabanındaki tablonun adı 'answer_attempts' olacak
public class AnswerAttempt {

    @Id // Birincil anahtar
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Otomatik artan ID
    private int id; // int tipinde ID

    // Bu cevap denemesinin hangi Quiz Oturumuna ait olduğunu belirten ilişki
    // Sizin template'inizdeki 'takeQuizId' alanına karşılık gelir (TakeQuiz -> QuizSession oldu)
    @ManyToOne // Bir cevap denemesinin SADECE bir oturumu olur, ama bir oturumun birden çok cevap denemesi olabilir (Çoğa-Bir ilişki)
    @JoinColumn(name = "quiz_session_id", nullable = false) // Veritabanındaki yabancı anahtar sütununun adı 'quiz_session_id' olacak. Boş olamaz.
    private QuizSession quizSession; // İlişkili QuizSession objesi

    // Bu cevap denemesinin hangi Soruya ait olduğunu belirten ilişki
    // Sizin template'inizdeki 'questionId' alanına karşılık gelir
    @ManyToOne // Bir cevap denemesinin SADECE bir sorusu olur, ama bir sorunun birden çok cevap denemesi olabilir (Çoğa-Bir ilişki)
    @JoinColumn(name = "question_id", nullable = false) // Veritabanındaki yabancı anahtar sütununun adı 'question_id' olacak. Boş olamaz.
    private Question question; // İlişkili Question objesi

    @Column(columnDefinition = "TEXT") // Boş olabilir (Çoktan seçmeli sorularda bu alan kullanılmayabilir)
    private String submittedAnswerText; // Öğrencinin açık uçlu veya kısa cevaplı soruya verdiği metin cevabı (Sizin 'answer' / 'text' alanlarına karşılık gelir)

    // Çoktan seçmeli sorularda öğrencinin seçtiği şıklar
    // Sizin 'Answer.java' template'inizdeki 'selectedOptions' listesine karşılık gelir.
    // Bir cevap denemesi, birden çok şık seçebilir (örn: çoklu doğru şıklı sorular)
    // Bir şık, birden çok cevap denemesinde seçilebilir.
    // Bu bir Çoktan-Çoğa (ManyToMany) ilişkisidir ve veritabanında ara bir tablo (join table) gerektirir.
    @ManyToMany // Çoktan-Çoğa ilişki
    @JoinTable( // İlişkiyi tutacak ara tabloyu tanımlar
        name = "answer_attempt_selected_options", // Ara tablonun adı
        joinColumns = @JoinColumn(name = "answer_attempt_id"), // Bu Entity'den (AnswerAttempt) ara tabloya giden sütun
        inverseJoinColumns = @JoinColumn(name = "option_id") // Karşı Entity'den (Option) ara tabloya giden sütun
    )
    private Set<Option> selectedOptions = new HashSet<>(); // Seçilen şıklar Set'i (Sıra önemli değil, tekrar olamaz)

    @Column(nullable = false) // Boş olamaz, varsayılan false
    private boolean isCorrect = false; // Bu cevap denemesi doğru muydu? (Puanlama sonrası set edilecek)

    // JPA için argümansız constructor
    public AnswerAttempt() {
         this.selectedOptions = new HashSet<>(); // Set boş başlatılmalı
    }

    // Temel alanları alan constructor (ID otomatik yönetilir)
    // İlişkili objeler (quizSession, question) ve selectedOptions constructora dahil edilebilir veya sonradan set edilebilir
    public AnswerAttempt(QuizSession quizSession, Question question, String submittedAnswerText) {
        this.quizSession = quizSession; // İlişki kurulacak
        this.question = question; // İlişki kurulacak
        this.submittedAnswerText = submittedAnswerText; // Metin cevabı
        this.isCorrect = false; // Başlangıçta yanlış kabul edelim, puanlama Service'te yapılacak
        this.selectedOptions = new HashSet<>(); // Set boş başlatılmalı
    }
     // Çoktan seçmeli için constructor (Metin cevabı olmaz)
    public AnswerAttempt(QuizSession quizSession, Question question, Set<Option> selectedOptions) {
        this.quizSession = quizSession; // İlişki kurulacak
        this.question = question; // İlişki kurulacak
        this.submittedAnswerText = null; // Metin cevabı yok
        this.isCorrect = false; // Başlangıçta yanlış kabul edelim
        this.selectedOptions = selectedOptions != null ? selectedOptions : new HashSet<>(); // Set set edilecek
    }


    // Getter ve Setter Metotları
    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // ID setter'ı genellikle kullanılmaz

    public QuizSession getQuizSession() { return quizSession; }
    public void setQuizSession(QuizSession quizSession) { this.quizSession = quizSession; } // İlişkiyi set etmek için setter

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; } // İlişkiyi set etmek için setter

    public String getSubmittedAnswerText() { return submittedAnswerText; }
    public void setSubmittedAnswerText(String submittedAnswerText) { this.submittedAnswerText = submittedAnswerText; }

    public Set<Option> getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(Set<Option> selectedOptions) { this.selectedOptions = selectedOptions; } // İlişkiyi set etmek için setter

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    // Seçilen şık eklemek için yardımcı metot (ManyToMany ilişkisi için)
    public void addSelectedOption(Option option) {
        this.selectedOptions.add(option);
        // Not: ManyToMany ilişkilerde genellikle karşı tarafta (Option Entity'sinde) add metodu çağırmaya gerek yoktur,
        // ilişki JoinTable tarafından yönetilir.
    }

    // Seçilen şık çıkarmak için yardımcı metot
     public void removeSelectedOption(Option option) {
        this.selectedOptions.remove(option);
    }


    // equals() ve hashCode() (ID üzerinden)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerAttempt that = (AnswerAttempt) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString() (Debugging için)
    @Override
    public String toString() {
        return "AnswerAttempt{" +
               "id=" + id +
               ", quizSessionId=" + (quizSession != null ? quizSession.getId() : "null") +
               ", questionId=" + (question != null ? question.getId() : "null") +
               ", submittedAnswerText='" + (submittedAnswerText != null ? submittedAnswerText : "null") + '\'' +
               ", isCorrect=" + isCorrect +
               ", selectedOptionsCount=" + (selectedOptions != null ? selectedOptions.size() : 0) +
               '}';
    }

    // --- NOT: Template kodlarınızdaki checkAnswer() metodu Entity sınıfına ait değil.
    // --- Bu metot ve cevabın doğruluğunu kontrol etme/puanlama mantığı QuizSessionService sınıfında yer alacak.
}
