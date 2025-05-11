package com.example.QuizSystemProject.Service;

 // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.*; // Model katmanındaki tüm Entity'leri import edin
import com.example.QuizSystemProject.Repository.*; // Repository katmanındaki tüm Repository'leri import edin
import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.stereotype.Service; // Service anotasyonunu import edin
import org.springframework.transaction.annotation.Transactional; // İşlemleri yönetmek için

import java.time.Duration; // Süre hesaplama için
import java.time.LocalDateTime; // Tarih/saat için
import java.util.List; // List importu
import java.util.Optional; // Optional importu
import java.util.Set; // Set importu (seçilen şıklar için)

@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir
// Bu servisteki metotların çoğu veritabanı işlemi içereceği için @Transactional kullanmak iyi practice'dir.
// Bir metot @Transactional olduğunda, metot içindeki tüm veritabanı işlemleri tek bir işlem (transaction) içinde gerçekleşir.
// Eğer bir hata olursa, işlem geri alınır (rollback).
@Transactional // Bu annotation'ı sınıf seviyesine koyarak tüm public metotlara uygulayabiliriz veya sadece gerekli metotlara koyabiliriz.
public class QuizSessionService {

    // Bu servisin ihtiyaç duyacağı Repository'ler
    private final QuizSessionRepository quizSessionRepository;
    private final AnswerAttemptRepository answerAttemptRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository; // Öğrenciyi bulmak için

    // Bağımlılıkların enjekte edildiği constructor
    @Autowired
    public QuizSessionService(QuizSessionRepository quizSessionRepository, AnswerAttemptRepository answerAttemptRepository,
                              QuizRepository quizRepository, QuestionRepository questionRepository,
                              OptionRepository optionRepository, UserRepository userRepository) {
        this.quizSessionRepository = quizSessionRepository;
        this.answerAttemptRepository = answerAttemptRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
    }

    // --- Quiz Çözme Akışı Metotları ---

    // Quiz Oturumu Başlatma (Student yetkisi gerektirecek)
    // Sizin TakeQuiz template'ine karşılık gelen oturum başlatma kavramı burada.
    public QuizSession startQuizSession(Long studentId, Long quizId) {
        // NOT: Gerçek implementasyonda, studentId ile Öğrenci kullanıcısını bulma,
        // quizId ile Quizi bulma, bu kullanıcının rolünün STUDENT olduğunu kontrol etme,
        // Quizin aktif ve çözülebilir olup olmadığını kontrol etme,
        // yeni QuizSession objesi oluşturma (başlangıç zamanını set ederek),
        // kaydedip döndürme gibi mantıklar olacak.

        System.out.println("QuizSessionService: Quiz oturumu baslatildi - Ogrenci ID: " + studentId + ", Quiz ID: " + quizId);
        // Placeholder
        // Optional<User> studentOptional = userRepository.findById(studentId);
        // Optional<Quiz> quizOptional = quizRepository.findById(quizId);
        //
        // if (studentOptional.isPresent() && quizOptional.isPresent() && "ROLE_STUDENT".equals(studentOptional.get().getRole())) {
        //     User student = studentOptional.get();
        //     Quiz quiz = quizOptional.get();
        //
        //     // Quizin aktif ve çözülebilir olduğunu kontrol et (tarih/saat ve isActive alanları)
        //     if (quiz.isActive() && (quiz.getStartDate() == null || LocalDateTime.now().isAfter(quiz.getStartDate())) &&
        //         (quiz.getEndDate() == null || LocalDateTime.now().isBefore(quiz.getEndDate()))) {
        //
        //         QuizSession newSession = new QuizSession(student, quiz); // Başlangıç zamanı constructor'da set ediliyor
        //         return quizSessionRepository.save(newSession); // Oturumu kaydet
        //     } else {
        //         throw new RuntimeException("Quiz aktif degil veya suresi dolmus!"); // Hata yönetimi
        //     }
        // }
        // throw new RuntimeException("Ogrenci veya Quiz bulunamadi veya ogrenci yetkisi yok!"); // Hata yönetimi


        return new QuizSession(); // Şimdilik placeholder
    }

    // Cevap Gönderme/Kaydetme (Student yetkisi ve aktif oturum gerektirecek)
    // Sizin QuestionAnswer template'indeki answer ve isCorrect alanlarının kaydedilmesi burada olacak.
    public AnswerAttempt submitAnswer(Long sessionId, Long questionId, String submittedAnswerText, Set<Long> selectedOptionIds) {
        // NOT: Gerçek implementasyonda, sessionId ile aktif QuizSession'ı bulma,
        // questionId ile Soruyu bulma, AnswerAttempt objesi oluşturma,
        // metin cevabını veya seçilen şıkları set etme, AnswerAttempt'ı QuizSession'a ekleme (QuizSession Entity'deki addAnswerAttempt),
        // AnswerAttempt'ın doğruluğunu kontrol etme (checkAnswerCorrectness metodunu kullanarak),
        // kaydedip döndürme gibi mantıklar olacak.

        System.out.println("QuizSessionService: Cevap gonderildi - Oturum ID: " + sessionId + ", Soru ID: " + questionId);
        // Placeholder
        // Optional<QuizSession> sessionOptional = quizSessionRepository.findById(sessionId);
        // Optional<Question> questionOptional = questionRepository.findById(questionId);
        //
        // if (sessionOptional.isPresent() && questionOptional.isPresent()) {
        //     QuizSession session = sessionOptional.get();
        //     Question question = questionOptional.get();
        //
        //     // Daha önce bu soru için cevap var mı kontrolü yapılabilir
        //     // if (session.getAnswers().stream().anyMatch(att -> att.getQuestion().getId().equals(questionId))) {
        //     //    throw new RuntimeException("Bu soruya zaten cevap verilmis!");
        //     // }
        //
        //     AnswerAttempt newAttempt;
        //
        //     if (question.getType().getTypeName().equals("Açık Uçlu") || question.getType().getTypeName().equals("Kısa Cevap")) {
        //         newAttempt = new AnswerAttempt(session, question, submittedAnswerText); // Metin cevabı ile oluştur
        //     } else if (question.getType().getTypeName().equals("Çoktan Seçmeli")) {
        //         Set<Option> selectedOptions = new HashSet<>();
        //         if (selectedOptionIds != null) {
        //             selectedOptionIds.forEach(optionId -> optionRepository.findById(optionId).ifPresent(selectedOptions::add));
        //         }
        //         newAttempt = new AnswerAttempt(session, question, selectedOptions); // Seçili şıklar ile oluştur
        //     } else {
        //         throw new RuntimeException("Bilinmeyen soru tipi!"); // Hata yönetimi
        //     }
        //
        //     // Cevabın doğruluğunu kontrol et (Bu metot aşağıda tanımlanacak)
        //     boolean isCorrect = checkAnswerCorrectness(question, newAttempt);
        //     newAttempt.setCorrect(isCorrect);
        //
        //     session.addAnswerAttempt(newAttempt); // Cevabı oturuma ekle (QuizSession Entity'deki yardımcı metot)
        //     // return answerAttemptRepository.save(newAttempt); // AnswerAttempt'ı kaydet (Cascade ile de kaydedilebilir)
        //      quizSessionRepository.save(session); // Oturumu kaydet (AnswerAttempt cascade ile kaydedilir)
        //      return newAttempt;
        //
        // }
        // throw new RuntimeException("Quiz Oturumu veya Soru bulunamadi!"); // Hata yönetimi

        return new AnswerAttempt(); // Şimdilik placeholder
    }

    // Quiz Oturumunu Tamamlama (Student yetkisi ve aktif oturum gerektirecek)
    public QuizSession completeQuizSession(Long sessionId) {
        // NOT: Gerçek implementasyonda, sessionId ile aktif QuizSession'ı bulma,
        // bitiş zamanını set etme, tüm cevapları gözden geçirerek puanı hesaplama,
        // QuizSession objesinin score alanını set etme, kaydedip döndürme gibi mantıklar olacak.

        System.out.println("QuizSessionService: Quiz oturumu tamamlaniyor - Oturum ID: " + sessionId);
        // Placeholder
        // Optional<QuizSession> sessionOptional = quizSessionRepository.findById(sessionId);
        //
        // if (sessionOptional.isPresent()) {
        //     QuizSession session = sessionOptional.get();
        //     session.setEndTime(LocalDateTime.now()); // Bitiş zamanını set et
        //
        //     // Puanı hesapla (Aşağıda ayrı bir metod olabilir)
        //     int calculatedScore = calculateScore(session);
        //     session.setScore(calculatedScore); // Puanı set et
        //
        //     return quizSessionRepository.save(session); // Oturumu kaydet
        // }
        // throw new RuntimeException("Quiz Oturumu bulunamadi!"); // Hata yönetimi

        return new QuizSession(); // Şimdilik placeholder
    }

    // --- Yardımcı Metotlar ---

    // Cevabın doğruluğunu kontrol etme (Internal yardımcı metot)
    // Sizin QuestionAnswer template'indeki checkAnswer() metodunun mantığı burada.
    // Bu metodun iş mantığı, soru tipine (QuestionType) ve Question Entity'sindeki doğru cevaba göre değişir.
    private boolean checkAnswerCorrectness(Question question, AnswerAttempt answerAttempt) {
        System.out.println("QuizSessionService: Cevap dogrulugu kontrol ediliyor - Soru ID: " + question.getId());
        // Placeholder
        // Gerçek implementasyonda:
        // if (question.getType().getTypeName().equals("Açık Uçlu") || question.getType().getTypeName().equals("Kısa Cevap")) {
        //    // submittedAnswerText'i question.getCorrectAnswerText() ile karşılaştır (büyük/küçük harf, boşluk vb. dikkate alarak)
        //    return answerAttempt.getSubmittedAnswerText() != null &&
        //           answerAttempt.getSubmittedAnswerText().equalsIgnoreCase(question.getCorrectAnswerText());
        // } else if (question.getType().getTypeName().equals("Çoktan Seçmeli")) {
        //    // Öğrencinin seçtiği şıkları (answerAttempt.getSelectedOptions()) Question Entity'sindeki doğru şıklarla karşılaştır
        //    // Önce Question'ın doğru şıklarını bul: List<Option> correctOptions = question.getOptions().stream().filter(Option::isCorrect).collect(Collectors.toList());
        //    // Sonra studentAttempt'teki selectedOptions'ın correctOptions ile eşleşip eşleşmediğini kontrol et (set karşılaştırması vb.)
        //     Set<Option> correctOptions = new HashSet<>();
        //     if (question.getOptions() != null) {
        //         question.getOptions().stream()
        //                  .filter(Option::isCorrect)
        //                  .forEach(correctOptions::add);
        //     }
        //     // Seçilen şık seti ile doğru şık setinin birebir aynı olup olmadığını kontrol et
        //     return answerAttempt.getSelectedOptions().equals(correctOptions);
        // }
        // return false; // Bilinmeyen tip veya hata durumu

        return true; // Simülasyon (Her cevabı doğru kabul edelim şimdilik)
    }

    // Quiz Oturumunun Puanını Hesaplama (Internal yardımcı metot veya public olabilir)
    private int calculateScore(QuizSession session) {
        System.out.println("QuizSessionService: Puan hesaplaniyor - Oturum ID: " + session.getId());
        // Placeholder
        // Gerçek implementasyonda:
        // session.getAnswers() listesindeki her AnswerAttempt için
        // if (attempt.isCorrect()) { puan += attempt.getQuestion().getPoints(); } // Sorunun puanını Question Entity'sinde tutabiliriz
        // Veya her doğru cevap için sabit bir puan verebiliriz.
        // int totalScore = 0;
        // for (AnswerAttempt attempt : session.getAnswers()) {
        //     if (attempt.isCorrect()) {
        //         // Varsayım: Her doğru soru 10 puan
        //         totalScore += 10; // Veya sorunun kendi puanı: attempt.getQuestion().getPoints();
        //     }
        // }
        // return totalScore;

        return session.getAnswers().size() * 10; // Simülasyon (Her cevaplanan soruyu doğru kabul edip 10 puan verelim)
    }

     // Quiz Oturum Süresini Hesaplama (Sizin TakeQuiz template'indeki calculateDuration)
     public Duration calculateDuration(QuizSession session) {
         System.out.println("QuizSessionService: Sure hesaplaniyor - Oturum ID: " + session.getId());
          // Placeholder
         if (session.getStartTime() == null) {
             return Duration.ZERO;
         }
         LocalDateTime endTime = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now(); // Eğer bitiş zamanı yoksa şu anki zamanı al
         return Duration.between(session.getStartTime(), endTime);

          // return Duration.ofMinutes(session.getDurationMinutes()); // Eğer durationMinutes alanı QuizSession'da tutuluyorsa
     }

     // Bir Oturumun Detaylarını Getirme
     public Optional<QuizSession> getQuizSessionDetails(Long sessionId) {
         System.out.println("QuizSessionService: Oturum detayları getiriliyor - Oturum ID: " + sessionId);
          // Placeholder
         // return quizSessionRepository.findById(sessionId);
          return Optional.empty(); // Simülasyon
     }

     // Bir Öğrencinin Tüm Oturumlarını Getirme
     public List<QuizSession> getStudentQuizSessions(Long studentId) {
         System.out.println("QuizSessionService: Öğrenci oturumları getiriliyor - Öğrenci ID: " + studentId);
          // Placeholder
          // Optional<User> studentOptional = userRepository.findById(studentId);
          // if (studentOptional.isPresent() && "ROLE_STUDENT".equals(studentOptional.get().getRole())) {
          //     return quizSessionRepository.findAllByStudent(studentOptional.get());
          // }
          // return List.of(); // Öğrenci bulunamazsa boş liste

          return List.of(new QuizSession()); // Simülasyon
     }

    // --- Template'teki diğer TakeQuiz/QuestionAnswer metotları ---
    // checkAnswer() mantığı checkAnswerCorrectness metoduna taşındı.

}

