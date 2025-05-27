package com.example.QuizSystemProject.Service;

import com.example.QuizSystemProject.Model.AnswerAttempt;
import com.example.QuizSystemProject.Model.Quiz;
import com.example.QuizSystemProject.Model.QuizSession;
import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.AnswerAttemptRepository;
import com.example.QuizSystemProject.Repository.QuizRepository;
import com.example.QuizSystemProject.Repository.QuizSessionRepository;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.dto.OverallStatsResponse;
import com.example.QuizSystemProject.dto.QuizStatsResponse;
import com.example.QuizSystemProject.dto.StudentOverallResultsResponse;
import com.example.QuizSystemProject.exception.QuizNotFoundException;
import com.example.QuizSystemProject.exception.QuizSessionNotFoundException;
import com.example.QuizSystemProject.exception.UserNotAuthorizedException;
import com.example.QuizSystemProject.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir
@Transactional // Bu annotation'ı sınıf seviyesine koyarak tüm public metotlara uygulayabiliriz.
public class StatisticsService {

    // Bu servisin ihtiyaç duyacağı Repository'ler
    private final QuizSessionRepository quizSessionRepository; // Oturum verisi için
    private final AnswerAttemptRepository answerAttemptRepository; // Cevap verisi için
    private final QuizRepository quizRepository; // Quiz verisi için
    private final UserRepository userRepository; // Kullanıcı verisi için


    // Bağımlılıkların enjekte edildiği constructor
    
    public StatisticsService(QuizSessionRepository quizSessionRepository, AnswerAttemptRepository answerAttemptRepository,
                             QuizRepository quizRepository, UserRepository userRepository) {
        this.quizSessionRepository = quizSessionRepository;
        this.answerAttemptRepository = answerAttemptRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
    }

    // --- İstatistik/Raporlama Metotları ---


    // --- İstatistik/Raporlama Metotları (StatisticsService'in Asıl Sorumluluğu) ---

    // Programın Genel İstatistiklerini Getirme (Admin yetkisi gerektirecek)
    // Sizin Admin.java template'indeki showProgramStatistics metodunun mantığı burada.
    // Toplam kullanıcı sayısı, toplam quiz sayısı, toplam oturum sayısı, genel ortalama puan vb.
    // Dönüş tipi OverallStatsResponse olarak değişti
    // Yetki kontrolü için viewerUserId parametresi eklendi
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public OverallStatsResponse getOverallProgramStatistics(int viewerUserId) {
        System.out.println("StatisticsService: Genel program istatistikleri getiriliyor - Izleyen ID: " + viewerUserId);

        // viewerUserId ile Kullanıcıyı bulma (Admin olup olmadığını kontrol edeceğiz)
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> { // <-- Lambda sözdizimi düzeltildi, Import UserNotFoundException
                    System.err.println("StatisticsService: Genel istatistikleri getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        // Yetki Kontrolü: Sadece Adminler bu istatistikleri görebilir
        if (!"ROLE_ADMIN".equals(viewerUser.getRole())) { // <-- getRole() null kontrolü eklendi
            System.err.println("StatisticsService: Genel istatistikleri getirirken - Kullanici Admin degil - ID: " + viewerUserId + ", Rol: " + viewerUser.getRole());
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının genel program istatistiklerini görme yetkisi yok."); // <-- Import UserNotAuthorizedException
        }

        // Repository'leri kullanarak ilgili verileri çekme
        int totalUsers = (int) userRepository.count(); // Toplam kullanıcı sayısı
        int totalQuizzes = (int) quizRepository.count(); // Toplam quiz sayısı
        int totalQuizSessions = (int) quizSessionRepository.count(); // Toplam oturum sayısı

        // Genel ortalama puanı hesaplama
        // Tüm oturumları çekip tamamlanmış olanları filtreleyerek ortalama hesaplayalım.
        List<QuizSession> allSessions = quizSessionRepository.findAll(); // Tüm oturumları çek

        double averageScoreOverall = 0.0; // Default ortalama 0.0
        int completedSessionCount = 0; // Tamamlanmış oturum sayısı

        if (allSessions != null && !allSessions.isEmpty()) {
             // Score'u null olmayan (yani tamamlanmış) oturumları filtrele
             List<QuizSession> completedSessions = allSessions.stream()
                                                               .filter(session -> session.getScore() > 0) // score null olmayanları al <-- BURASI Integer != null KONTROLÜ YAPIYOR VE DOĞRU
                                                               .collect(Collectors.toList()); // Sonucu listeye topla

             completedSessionCount = completedSessions.size(); // Tamamlanmış oturum sayısı

             if (completedSessionCount > 0) {
                  // Tamamlanmış oturumların puanlarını topla
                  int totalScoreSum = completedSessions.stream()
                                                        .mapToInt(QuizSession::getScore) // Integer puanları int stream'e dönüştürür (burada NPE olmaz çünkü filtreledik)
                                                        .sum(); // Toplamı al

                  // Ortalama hesapla (double cast ederek kayan noktalı bölme yap)
                  averageScoreOverall = (double) totalScoreSum / completedSessionCount;
             }
        }

        // OverallStatsResponse objesi oluşturma ve döndürme
        OverallStatsResponse statsResponse = new OverallStatsResponse( // OverallStatsResponse DTO'nuzun alanları alan constructor'ını kullan
            totalUsers,
            totalQuizzes,
            totalQuizSessions,
            averageScoreOverall
        );

        // Opsiyonel olarak Teacher/Student sayılarını ekleyebiliriz (eğer OverallStatsResponse DTO'nuzda bu alanlar varsa ve Repository metotları tanımlıysa)
        // statsResponse.setTotalTeachers(userRepository.countByRole("ROLE_TEACHER")); // Repository metodu gerekebilir
        // statsResponse.setTotalStudents(userRepository.countByRole("ROLE_STUDENT")); // Repository metodu gerekebilir


        System.out.println("StatisticsService: Genel program istatistikleri başarıyla getirildi.");
        return statsResponse; // OverallStatsResponse objesini döndür
    }


 // Belirli Bir Quiz'in İstatistiklerini Getirme (Teacher/Admin yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki showQuizStatistics metodunun mantığı burada.
    // Kaç kere çözüldü, ortalama puanı, en yüksek/en düşük puanlar vb.
    // Dönüş tipi Map<String, Object> yerine QuizStatsResponse olarak değişti
    // Yetki kontrolü için viewerUserId parametresi eklendi
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public QuizStatsResponse getQuizStatistics(int quizId, int viewerUserId) {
        System.out.println("StatisticsService: Quiz istatistikleri getiriliyor - Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);

        // viewerUserId ile Kullanıcıyı bulma (Yetki kontrolü için)
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Quiz istatistiklerini getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        // quizId ile Quizi bulma (Bulunamazsa QuizNotFoundException fırlat)
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Quiz istatistiklerini getirirken - Quiz bulunamadi - ID: " + quizId);
                    return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı.");
                });

        // Yetki Kontrolü: Kullanıcı bu quizin öğretmeni mi, yoksa Admin mi?
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(viewerUser.getRole());
        boolean isQuizTeacher = isTeacher && quiz.getTeacher() != null && quiz.getTeacher().getId() == viewerUserId;

        if (!isQuizTeacher && !isAdmin) {
            System.err.println("StatisticsService: Quiz istatistiklerini getirirken - Kullanici yetkisiz - Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("Bu quizin istatistiklerini görme yetkiniz yok. Sadece quizi oluşturan öğretmen veya admin görebilir.");
        }

        // Bu quize ait tüm QuizSession'ları çekme
        List<QuizSession> quizSessions = quizSessionRepository.findAllByQuiz(quiz); // Yeni eklediğimiz Repository metodunu kullan

        // Tamamlanmış (score'u null olmayan) oturumları filtrele
        List<QuizSession> completedSessions = quizSessions.stream()
                                                         .filter(session -> session.getScore() > 0)
                                                         .collect(Collectors.toList());

        // İstatistikleri hesaplama
        long totalAttempts = completedSessions.size(); // Tamamlanmış deneme sayısı

        Double averageScore = 0.0;
        Integer highestScore = null; // Başlangıçta null
        Integer lowestScore = null; // Başlangıçta null

        if (totalAttempts > 0) {
             // Tamamlanmış oturumlar üzerinden puan istatistiklerini Stream ile hesapla
             IntSummaryStatistics scoreStats = completedSessions.stream()
                                                               .mapToInt(QuizSession::getScore) // Integer score'ları int stream'e dönüştür (Null olmayanları filtreledik)
                                                               .summaryStatistics(); // Özet istatistikleri (count, sum, min, max, average) al

             averageScore = scoreStats.getAverage(); // Ortalama
             highestScore = scoreStats.getMax(); // En yüksek (int döner)
             lowestScore = scoreStats.getMin(); // En düşük (int döner)

             // Eğer 0 deneme varsa getMax/getMin int'in varsayılan değerini (0) döndürebilir,
             // bu yüzden eğer totalAttempts > 0 ise bunları kullanıyoruz.
             // IntSummaryStatistics'in getMax/getMin metotları, stream boşsa 0 döndürür.
             // Ancak biz zaten totalAttempts > 0 kontrolü yaptığımız için sorun yok.
        } else {
             // Hiç tamamlanmış oturum yoksa, en yüksek/düşük puan null olarak kalır, ortalama 0.0
             System.out.println("StatisticsService: Quiz ID " + quizId + " icin tamamlanmis oturum bulunamadi.");
        }


        // Quizin toplam soru sayısını al
        long totalQuestions = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0; // Varsayım: Quiz entity'sinde List/Set<Question> questions alanı var.


        // QuizStatsResponse objesi oluşturma ve döndürme
        QuizStatsResponse statsResponse = new QuizStatsResponse( // Sizin QuizStatsResponse DTO'nuzun güncellenmiş constructor'ını kullan
                quiz.getId(), // Long
                quiz.getName(), // String
                totalAttempts, // <-- Şimdi bu Long (List.size() long döner veya cast edilecekse buradan yapılır)
                averageScore, // Double
                highestScore, // Integer (IntSummaryStatistics.getMax() int döner, auto-boxed)
                lowestScore,  // Integer (IntSummaryStatistics.getMin() int döner, auto-boxed)
                totalQuestions // <-- Yeni parametre (Long)
            );

        // Not: DTO'nuzda totalAttempts, highestScore, lowestScore Integer olarak tanımlıydı.
        // totalAttempts long olduğu için cast ettim. highestScore/lowestScore zaten IntSummaryStatistics'ten int dönüyor.


        System.out.println("StatisticsService: Quiz istatistikleri başarıyla getirildi - Quiz ID: " + quizId);
        return statsResponse; // QuizStatsResponse objesini döndür
    }

 // Belirli Bir Quiz'in Cevaplarını/Sonuçlarını Gözden Geçirme (Teacher/Admin yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki showQuizAnswers metodunun mantığı burada.
    // Öğrencilerin hangi sorulara ne cevap verdiğini, hangilerinin doğru/yanlış olduğunu gösterme.
    // Dönüş tipi List<AnswerAttempt> (Controller DTO'ya çevirecek)
    // Yetki kontrolü için viewerUserId parametresi eklendi
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public List<AnswerAttempt> getQuizAnswersReview(int quizId, int viewerUserId) {
        System.out.println("StatisticsService: Quiz cevapları gözden geçiriliyor - Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);

        // viewerUserId ile Kullanıcıyı bulma (Yetki kontrolü için)
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Quiz cevaplarını gözden geçirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        // quizId ile Quizi bulma (Bulunamazsa QuizNotFoundException fırlat)
        // Bu quizin öğretmeni kontrolü için Quiz Entity'sine ihtiyacımız var.
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Quiz cevaplarını gözden geçirirken - Quiz bulunamadi - ID: " + quizId);
                    return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı.");
                });

        // Yetki Kontrolü: Kullanıcı bu quizin öğretmeni mi, yoksa Admin mi?
        boolean isQuizTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == viewerUserId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole()); // Varsayım: User Entity'de getRole() metodu var

        if (!isQuizTeacher && !isAdmin) {
            System.err.println("StatisticsService: Quiz cevaplarını gözden geçirirken - Kullanici yetkisiz - Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının bu quize ait cevapları gözden geçirme yetkisi yok.");
        }

        // Belirli bir Quize ait tüm AnswerAttempt'leri Repository metodu ile çekme
        // Bu metot, tüm oturumlardaki tüm cevapları getirecektir.
        List<AnswerAttempt> answerAttempts = answerAttemptRepository.findByQuizSession_Quiz_Id(quizId); // Yeni eklediğimiz Repository metodunu kullan

        System.out.println("StatisticsService: Quiz ID " + quizId + " icin " + answerAttempts.size() + " adet cevap denemesi bulundu.");
        return answerAttempts; // Entity listesini döndür (Controller DTO'ya çevirecek)
    }

 // Belirli Bir Öğrencinin Tüm Quiz Sonuçlarını/Oturumlarını Getirme (Student kendi sonucunu, Teacher kendi öğrencilerinin, Admin hepsini görebilir)
    // Dönüş tipi List<QuizSession> (Controller DTO'ya çevirecek)
    // Yetki kontrolü için viewerUserId parametresi eklendi
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public List<QuizSession> getStudentOverallResults(int studentId, int viewerUserId) {
        System.out.println("StatisticsService: Öğrenci genel sonuçları getiriliyor - Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);

        // studentId ile sonuçları istenen Öğrenciyi bulma (Bulunamazsa UserNotFoundException fırlat)
        User targetStudent = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Hedef öğrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

        // Hedef kullanıcının gerçekten bir Öğrenci olduğunu kontrol etme (Endpoint student sonuçları için)
        if (!"ROLE_STUDENT".equals(targetStudent.getRole())) {
             System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Hedef kullanici ogrenci degil - ID: " + studentId + ", Rol: " + targetStudent.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil. Sadece öğrencilerin sonuçları bu endpoint'ten getirilebilir.");
        }


        // viewerUserId ile isteği yapan Kullanıcıyı bulma (Yetki kontrolü için)
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        // Yetki Kontrolü: İstek yapan kullanıcı hedef öğrencinin kendisi mi, Admin mi, yoksa ilgili Teacher mı?
        boolean isSelf = viewerUser.getId() == studentId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(viewerUser.getRole());

        // Eğer öğretmense, sadece kendi quizlerine ait sonuçları görebilir
        if (isTeacher) {
            // Öğrencinin bu öğretmenin quizlerine ait herhangi bir oturumu var mı kontrol et
            List<QuizSession> studentSessions = quizSessionRepository.findAllByStudent(targetStudent);
            boolean hasTeacherQuizSession = studentSessions.stream()
                .anyMatch(session -> session.getQuiz().getTeacher() != null && 
                         session.getQuiz().getTeacher().getId() == viewerUserId);

            if (!hasTeacherQuizSession) {
                System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Öğretmen yetkisiz - Öğrenci ID: " + studentId + ", Öğretmen ID: " + viewerUserId);
                throw new UserNotAuthorizedException("Bu öğrenci sizin quizlerinize ait herhangi bir oturum gerçekleştirmemiş.");
            }
        }

        if (!isSelf && !isAdmin && !isTeacher) {
            System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Kullanici yetkisiz - Hedef Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının ID " + studentId + " olan öğrencinin sonuçlarını görme yetkisi yok.");
        }


        // Hedef öğrenciye ait tüm QuizSession'ları Repository metodu ile bulma
        List<QuizSession> sessions = quizSessionRepository.findAllByStudent(targetStudent);

        // Eğer öğretmense, sadece kendi quizlerine ait oturumları filtrele
        if (isTeacher && !isAdmin) {
            sessions = sessions.stream()
                .filter(session -> session.getQuiz().getTeacher() != null && 
                        session.getQuiz().getTeacher().getId() == viewerUserId)
                .collect(Collectors.toList());
        }

        System.out.println("StatisticsService: Öğrenci ID " + studentId + " icin " + sessions.size() + " adet oturum bulundu.");
        return sessions; // Entity listesini döndür (Controller DTO'ya çevirecek)
    }

 // Bel belirli Bir Öğrencinin Belirli Bir Quizdeki Sonucunu/Oturumunu Getirme
    // Dönüş tipi Optional<QuizSession> yerine QuizSession olmalı ve bulunamazsa Exception fırlatmalı
    // Controller'da QuizSessionDetailsResponse DTO'sına dönüşüm yapılacak.
    // Yetki kontrolü için viewerUserId parametresi eklendi
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public QuizSession getStudentQuizResult(int studentId, int quizId, int viewerUserId) { // <-- Dönüş tipi QuizSession oldu
        System.out.println("StatisticsService: Öğrencinin quiz sonucu getiriliyor - Öğrenci ID: " + studentId + ", Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);

        // studentId ile sonuçları istenen Öğrenciyi bulma (Bulunamazsa UserNotFoundException fırlat)
        User targetStudent = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Hedef öğrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

         // Hedef kullanıcının gerçekten bir Öğrenci olduğunu kontrol etme (Endpoint student sonuçları için)
        if (!"ROLE_STUDENT".equals(targetStudent.getRole())) {
             System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Hedef kullanici ogrenci degil - ID: " + studentId + ", Rol: " + targetStudent.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil.");
        }


        // quizId ile ilgili Quizi bulma (Bulunamazsa QuizNotFoundException fırlat)
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Quiz bulunamadi - ID: " + quizId);
                    return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı.");
                });

        // viewerUserId ile isteği yapan Kullanıcıyı bulma (Yetki kontrolü için)
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        // Yetki Kontrolü: İstek yapan kullanıcı hedef öğrencinin kendisi mi, Admin mi, yoksa ilgili Teacher mı?
        boolean isSelf = viewerUser.getId() == studentId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(viewerUser.getRole());

        // Eğer öğretmense, sadece kendi quizinin sonucunu görebilir
        if (isTeacher && !isAdmin) {
            boolean isQuizTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == viewerUserId;
            if (!isQuizTeacher) {
                System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Öğretmen yetkisiz - Quiz ID: " + quizId + ", Öğretmen ID: " + viewerUserId);
                throw new UserNotAuthorizedException("Bu quizin sonuçlarını görme yetkiniz yok. Sadece quizi oluşturan öğretmen görebilir.");
            }
        }

        if (!isSelf && !isAdmin && !isTeacher) {
            System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Kullanici yetkisiz - Hedef Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("Bu öğrencinin quiz sonucunu görme yetkiniz yok.");
        }

        // Öğrencinin bu quizdeki oturumunu bul
        QuizSession quizSession = quizSessionRepository.findByStudentAndQuiz(targetStudent, quiz)
            .orElseThrow(() -> {
                System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Oturum bulunamadi - Öğrenci ID: " + studentId + ", Quiz ID: " + quizId);
                return new QuizSessionNotFoundException("ID " + studentId + " olan öğrencinin ID " + quizId + " olan quize ait oturumu bulunamadı.");
            });

        return quizSession;
    }

 // Bir Öğrencinin Tüm Quizlerinin Ortalama Puanını Hesaplama
    // Sizin Student.java template'indeki CalculateAverage metodunun mantığı burada.
    // Dönüş tipi double olarak kalabilir.
    // Yetki kontrolü için viewerUserId parametresi eklendi
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public double calculateAverageScoreForStudent(int studentId, int viewerUserId) { // <-- Dönüş tipi double
        System.out.println("StatisticsService: Öğrenci ortalama puanı hesaplanıyor - Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);

        // studentId ile ortalaması istenen Öğrenciyi bulma (Bulunamazsa UserNotFoundException fırlat)
        User targetStudent = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Hedef öğrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

         // Hedef kullanıcının gerçekten bir Öğrenci olduğunu kontrol etme (Endpoint student ortalaması için)
        if (!"ROLE_STUDENT".equals(targetStudent.getRole())) {
             System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Hedef kullanici ogrenci degil - ID: " + studentId + ", Rol: " + targetStudent.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil. Sadece öğrencilerin ortalama puanı bu endpoint'ten getirilebilir.");
        }


        // viewerUserId ile isteği yapan Kullanıcıyı bulma (Yetki kontrolü için)
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        // Yetki Kontrolü: İstek yapan kullanıcı hedef öğrencinin kendisi mi, Admin mi, yoksa ilgili Teacher mı?
        boolean isSelf = viewerUser.getId() == studentId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(viewerUser.getRole());

        // Eğer öğretmense, sadece kendi quizlerine ait sonuçları görebilir
        if (isTeacher && !isAdmin) {
            // Öğrencinin bu öğretmenin quizlerine ait herhangi bir oturumu var mı kontrol et
            List<QuizSession> studentSessions = quizSessionRepository.findAllByStudent(targetStudent);
            boolean hasTeacherQuizSession = studentSessions.stream()
                .anyMatch(session -> session.getQuiz().getTeacher() != null && 
                         session.getQuiz().getTeacher().getId() == viewerUserId);

            if (!hasTeacherQuizSession) {
                System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Öğretmen yetkisiz - Öğrenci ID: " + studentId + ", Öğretmen ID: " + viewerUserId);
                throw new UserNotAuthorizedException("Bu öğrencinin ortalama puanını görme yetkiniz yok. Sadece kendi quizlerinize ait sonuçları görebilirsiniz.");
            }
        }

        if (!isSelf && !isAdmin && !isTeacher) {
            System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Kullanici yetkisiz - Hedef Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının ID " + studentId + " olan öğrencinin ortalama puanını görme yetkisi yok.");
        }

        // Öğrencinin tüm oturumlarını getir
        List<QuizSession> studentSessions = quizSessionRepository.findAllByStudent(targetStudent);

        // Eğer öğretmense, sadece kendi quizlerine ait oturumları filtrele
        if (isTeacher && !isAdmin) {
            studentSessions = studentSessions.stream()
                .filter(session -> session.getQuiz().getTeacher() != null && 
                        session.getQuiz().getTeacher().getId() == viewerUserId)
                .collect(Collectors.toList());
        }

        // Tamamlanmış oturumları filtrele ve ortalama hesapla
        double averageScore = studentSessions.stream()
            .filter(session -> session.getScore() > 0) // Tamamlanmış oturumlar
            .mapToInt(QuizSession::getScore)
            .average()
            .orElse(0.0); // Hiç tamamlanmış oturum yoksa 0.0 dön

        System.out.println("StatisticsService: Öğrenci ID " + studentId + " için ortalama puan: " + averageScore);
        return averageScore;
    }

    // --- Diğer İstatistik İhtiyaçları ---
    
    /**
     * Tüm öğrencileri ortalama puanlarına göre sıralı olarak getirir
     * @return Öğrenci bilgilerini ve ortalama puanlarını içeren liste
     */

    @Transactional(readOnly = true)
    public List<StudentOverallResultsResponse> getStudentLeaders() {
        System.out.println("StatisticsService: Öğrenci liderlik tablosu hazırlanıyor");
        
        // Tüm öğrencileri al
        List<User> students = userRepository.findAllByRole("ROLE_STUDENT");
        
        // Her öğrenci için oturumları al ve StudentOverallResultsResponse oluştur
        List<StudentOverallResultsResponse> leaders = students.stream()
            .map(student -> {
                try {
                    // Öğrencinin tüm oturumlarını al
                    List<QuizSession> sessions = quizSessionRepository.findByStudentId(student.getId());
                    // Student ve oturum listesi ile yeni bir StudentOverallResultsResponse oluştur
                    return new StudentOverallResultsResponse(student, sessions);
                } catch (Exception e) {
                    System.err.println("Öğrenci puanı hesaplanırken hata - Öğrenci ID: " + student.getId() + ", Hata: " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull) // Hata alınan öğrencileri filtrele
            .sorted(Comparator.comparingDouble(StudentOverallResultsResponse::getAverageScore).reversed()) // Yüksek puandan düşüğe sırala
            .collect(Collectors.toList());
            
        System.out.println("StatisticsService: Toplam " + leaders.size() + " öğrenci listelendi");
        return leaders;
    }
    
    // En başarılı öğrenciler, en zor quizler, en çok yanlış yapılan sorular vb. ileride eklenebilir.
}
