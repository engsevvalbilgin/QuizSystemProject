package com.example.QuizSystemProject.Service;


import com.example.QuizSystemProject.Model.*; // Model katmanındaki Entity'leri import edin (İstatistik verisi çekerken kullanılacak)
import com.example.QuizSystemProject.Repository.*; // Repository katmanındaki Repository'leri import edin (Veriye erişim için)
import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.stereotype.Service; // Service anotasyonunu import edin

import java.util.List; // Liste importu
import java.util.Map; // İstatistikleri Map olarak döndürmek için
import java.util.Optional; // Optional importu

@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir
public class StatisticsService {

    // Bu servisin ihtiyaç duyacağı Repository'ler
    private final QuizSessionRepository quizSessionRepository; // Oturum verisi için
    private final AnswerAttemptRepository answerAttemptRepository; // Cevap verisi için
    private final QuizRepository quizRepository; // Quiz verisi için
    private final UserRepository userRepository; // Kullanıcı verisi için

    // Bağımlılıkların enjekte edildiği constructor
    @Autowired
    public StatisticsService(QuizSessionRepository quizSessionRepository, AnswerAttemptRepository answerAttemptRepository,
                             QuizRepository quizRepository, UserRepository userRepository) {
        this.quizSessionRepository = quizSessionRepository;
        this.answerAttemptRepository = answerAttemptRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
    }

    // --- İstatistik/Raporlama Metotları ---

    // Programın Genel İstatistiklerini Getirme (Admin yetkisi gerektirecek)
    // Sizin Admin.java template'indeki showProgramStatistics metodunun mantığı burada.
    // Toplam kullanıcı sayısı, toplam quiz sayısı, toplam oturum sayısı, genel ortalama puan vb.
    public Map<String, Object> getOverallProgramStatistics() {
        System.out.println("StatisticsService: Genel program istatistikleri getiriliyor.");
        // NOT: Gerçek implementasyonda, Repository'leri kullanarak ilgili verileri çekme
        // (örn: userRepository.count(), quizRepository.count(), quizSessionRepository.count()),
        // bu veriler üzerinde hesaplamalar yapma ve bunları Map veya özel bir DTO objesi olarak döndürme mantığı olacak.

        // Placeholder veriler
        return Map.of(
                "totalUsers", userRepository.count(), // Repository metotlarını kullanabiliriz
                "totalQuizzes", quizRepository.count(),
                "totalQuizSessions", quizSessionRepository.count(),
                "averageScore", 75.5 // Örnek hesaplama
        );
    }

    // Belirli Bir Quiz'in İstatistiklerini Getirme (Teacher/Admin yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki showQuizStatistics metodunun mantığı burada.
    // Kaç kere çözüldü, ortalama puanı, en yüksek/en düşük puanlar vb.
    public Map<String, Object> getQuizStatistics(int quizId) {
        System.out.println("StatisticsService: Quiz istatistikleri getiriliyor - Quiz ID: " + quizId);
        // NOT: Gerçek implementasyonda, quizId ile Quizi bulma, o quize ait tüm QuizSession'ları çekme,
        // oturum sayısı, ortalama puan (session score alanlarından), min/max puanları hesaplama gibi mantıklar olacak.

        // Placeholder
        Optional<Quiz> quizOptional = quizRepository.findById(quizId);
        if (quizOptional.isPresent()) {
            Quiz quiz = quizOptional.get();
            // List<QuizSession> sessions = quizSessionRepository.findAllByQuiz(quiz);
            // Hesaplamalar...
            return Map.of(
                    "quizName", quiz.getName(),
                    "totalAttempts", 50, // Simülasyon
                    "averageScore", 80.0, // Simülasyon
                    "highestScore", 100, // Simülasyon
                    "lowestScore", 30 // Simülasyon
            );
        }
        //throw new RuntimeException("Quiz bulunamadi!"); // Hata yönetimi
        return Map.of("error", "Quiz bulunamadı"); // Hata durumunda boş veya hata mesajı döndürme
    }

    // Belirli Bir Quiz'in Cevaplarını/Sonuçlarını Gözden Geçirme (Teacher/Admin yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki showQuizAnswers metodunun mantığı burada.
    // Öğrencilerin hangi sorulara ne cevap verdiğini, hangilerinin doğru/yanlış olduğunu gösterme.
    public List<AnswerAttempt> getQuizAnswersReview(Long quizId) {
        System.out.println("StatisticsService: Quiz cevapları gözden geçiriliyor - Quiz ID: " + quizId);
        // NOT: Gerçek implementasyonda, quizId ile Quizi bulma, o quize ait tüm QuizSession'ları çekme,
        // bu oturumlardaki tüm AnswerAttempt'ları çekme (ilişkiler üzerinden),
        // AnswerAttempt objelerini döndürme gibi mantıklar olacak.

        // Placeholder
        // return answerAttemptRepository.findAllByQuizSessionQuizId(quizId); // Repository'de özel sorgu gerekebilir
         return List.of(new AnswerAttempt()); // Simülasyon
    }

    // Belirli Bir Öğrencinin Tüm Quiz Sonuçlarını/Oturumlarını Getirme (Student kendi sonucunu, Teacher kendi öğrencilerinin, Admin hepsini görebilir)
    public List<QuizSession> getStudentOverallResults(Long studentId) {
        System.out.println("StatisticsService: Öğrenci genel sonuçları getiriliyor - Öğrenci ID: " + studentId);
        // NOT: Gerçek implementasyonda, studentId ile Öğrenciyi bulma, o öğrenciye ait tüm QuizSession'ları çekme,
        // oturum listesini döndürme gibi mantıklar olacak.

        // Placeholder
        // Optional<User> studentOptional = userRepository.findById(studentId);
        // if (studentOptional.isPresent() && "ROLE_STUDENT".equals(studentOptional.get().getRole())) {
        //    return quizSessionRepository.findAllByStudent(studentOptional.get());
        // }
        // throw new RuntimeException("Ogrenci bulunamadi veya yetkisi yok!"); // Hata yönetimi

        return List.of(new QuizSession()); // Simülasyon
    }

    // Belirli Bir Öğrencinin Belirli Bir Quizdeki Sonucunu/Oturumunu Getirme
    public Optional<QuizSession> getStudentQuizResult(Long studentId, Long quizId) {
        System.out.println("StatisticsService: Öğrencinin quiz sonucu getiriliyor - Öğrenci ID: " + studentId + ", Quiz ID: " + quizId);
        // NOT: Gerçek implementasyonda, studentId ve quizId ile ilgili oturumu/oturumlari bulma (örn: en son oturum),
        // QuizSession objesini döndürme gibi mantıklar olacak.

        // Placeholder
        // Optional<User> studentOptional = userRepository.findById(studentId);
        // Optional<Quiz> quizOptional = quizRepository.findById(quizId);
        // if (studentOptional.isPresent() && quizOptional.isPresent()) {
        //     return quizSessionRepository.findTopByStudentAndQuizOrderByStartTimeDesc(studentOptional.get(), quizOptional.get()); // En son oturumu bul
        // }
        // return Optional.empty(); // Bulunamadi

        return Optional.empty(); // Simülasyon
    }


    // Bir Öğrencinin Tüm Quizlerinin Ortalama Puanını Hesaplama
    // Sizin Student.java template'indeki CalculateAverage metodunun mantığı burada.
    public double calculateAverageScoreForStudent(Long studentId) {
        System.out.println("StatisticsService: Öğrenci ortalama puanı hesaplanıyor - Öğrenci ID: " + studentId);
        // NOT: Gerçek implementasyonda, studentId ile Öğrenciyi bulma, o öğrenciye ait tüm QuizSession'ları çekme,
        // oturumların score alanlarını toplayıp oturum sayısına bölme gibi mantıklar olacak.

        // Placeholder
        // List<QuizSession> sessions = getStudentOverallResults(studentId); // Kendi metodumuzu kullanalım
        // if (sessions.isEmpty()) return 0.0;
        // int totalScore = sessions.stream().mapToInt(QuizSession::getScore).sum();
        // return (double) totalScore / sessions.size();

        return 85.0; // Simülasyon
    }

    // AI ile Quiz Puanlama (Eğer AI puanlaması ayrı bir iş akışıysa)
    // Sizin Student.java template'indeki AskAItoGradeQuiz metodunun mantığı burada veya ayrı bir AIService'te.
    // Bu işlev genellikle QuizSessionService'in completeQuizSession metodunda veya SubmitAnswer içinde çağrılır.
    // public int gradeQuizSessionWithAI(QuizSession session) {
    //     System.out.println("StatisticsService: AI ile puanlama yapılıyor - Oturum ID: " + session.getId());
    //     // Placeholder: AI servisi çağrılır, cevaplar gönderilir, puan alınır.
    //     // return aiService.gradeAnswers(session.getAnswers());
    //     return 90; // Simülasyon
    // }


    // --- Diğer İstatistik İhtiyaçları ---
    // En başarılı öğrenciler, en zor quizler, en çok yanlış yapılan sorular vb. ileride eklenebilir.
}

