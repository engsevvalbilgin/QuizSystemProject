package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Model.AnswerAttempt;
import com.example.QuizSystemProject.Model.QuizSession;
import com.example.QuizSystemProject.dto.StudentOverallResultsResponse;
import com.example.QuizSystemProject.security.CustomUserDetails;
import com.example.QuizSystemProject.dto.OverallStatsResponse;
import com.example.QuizSystemProject.dto.QuizStatsResponse;
import com.example.QuizSystemProject.dto.AnswerAttemptResponse;
import com.example.QuizSystemProject.dto.QuizSessionResponse;
import com.example.QuizSystemProject.dto.QuizSessionDetailsResponse;
import com.example.QuizSystemProject.Service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService; // İstatistik iş mantığı servisi

    // StatisticsService bağımlılığının enjekte edildiği constructor
    
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // --- İstatistik Endpoint'leri ---

    // Tüm öğrencileri liderlik tablosuna göre sırala
    @GetMapping("/student-leaders")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<List<StudentOverallResultsResponse>> getStudentLeaders() {
        System.out.println("StatisticsController: Liderlik tablosu isteniyor");
        List<StudentOverallResultsResponse> leaders = statisticsService.getStudentLeaders();
        System.out.println("StatisticsController: Toplam " + leaders.size());
        return ResponseEntity.ok(leaders);
    }

    // Programın genel istatistiklerini getirme
    // HTTP GET isteği ile "/api/statistics/overall" adresine yapılan istekleri karşılar
    // (Sadece Admin yetkisi gerektirir)

    @GetMapping("/overall")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OverallStatsResponse> getOverallStatistics() {
        System.out.println("StatisticsController: Genel istatistikler isteniyor.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            System.err.println("StatisticsController: getOverallStatistics - Güvenlik bağlamında geçerli CustomUserDetails bulunamadı.");
            throw new IllegalStateException("Kimlik doğrulama başarısız veya kullanıcı detayı beklenmiyor.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        int currentViewerId = userDetails.getId();
        System.out.println("StatisticsController: Genel istatistikleri isteyen kullanıcının ID'si: " + currentViewerId);

        OverallStatsResponse stats = statisticsService.getOverallProgramStatistics(currentViewerId);
        System.out.println("StatisticsController: Genel istatistikler Service tarafından başarıyla getirildi.");
        return ResponseEntity.ok(stats);
    }

 // Belirli Bir Quiz'in İstatistiklerini Getirme
    // HTTP GET isteği ile "/api/statistics/quizzes/{quizId}" adresine yapılan istekleri karşılar
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir - Yetki kontrolü Service'te yapılıyor)
    @GetMapping("/quizzes/{quizId}")
    // Dönüş tipi ResponseEntity<QuizStatsResponse> olarak güncellendi
    public ResponseEntity<QuizStatsResponse> getQuizStatistics(@PathVariable("quizId") int quizId) {
        System.out.println("StatisticsController: Quiz istatistikleri isteniyor - Quiz ID: " + quizId);

        // try-catch bloğu yok, hatalar GlobalExceptionHandler'a gidecek.
        // Service metodu yetki kontrolünü, NotFound hatalarını ve diğer exception'ları fırlatacak.

        // Güvenlik bağlamından (Spring Security) geçerli kullanıcının ID'sini al
        // Bu kullanıcı, quiz istatistiklerini görüntülemek isteyen kişidir (Teacher veya Admin olması beklenir).
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentViewerId; // İstatistikleri görüntüleyen kullanıcının ID'si
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            try {
                 currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); // Varsayım: username String olarak ID'yi içerir ve int'e çevrilebilir
            } catch (NumberFormatException e) {
                 System.err.println("StatisticsController: getQuizStatistics - Principal username sayısal değil, ID olarak kullanılamaz.");
                 throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
            }
        } else {
             System.err.println("StatisticsController: getQuizStatistics - Principal beklenmeyen tipte: " + principal.getClass().getName());
             throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
        }


        // Service katmanındaki getQuizStatistics metodunu çağır (quizId ve görüntüleyen kullanıcı ID'si ile birlikte)
        // Service QuizStatsResponse DTO'su döndürür veya exception fırlatır (Quiz yok, Yetkisiz, Kullanıcı yok gibi)
        QuizStatsResponse stats = statisticsService.getQuizStatistics(quizId, currentViewerId);


        System.out.println("StatisticsController: Quiz istatistikleri başarıyla getirildi - Quiz ID: " + quizId);
        // Başarılı durumda 200 OK yanıtı ve DTO'yu döndür
        return ResponseEntity.ok(stats);
    }

 // Belirli Bir Quiz'in Cevaplarını Gözden Geçirme
    // HTTP GET isteği ile "/api/statistics/quizzes/{quizId}/answers" adresine yapılan istekleri karşılar
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir - Yetki kontrolü Service'te yapılıyor)
    @GetMapping("/quizzes/{quizId}/answers")
    // Dönüş tipi ResponseEntity<List<AnswerAttemptResponse>> olarak güncellendi
    public ResponseEntity<List<AnswerAttemptResponse>> reviewQuizAnswers(@PathVariable("quizId") int quizId) {
        System.out.println("StatisticsController: Quiz cevapları gözden geçirme isteniyor - Quiz ID: " + quizId);

        // try-catch bloğu yok, hatalar GlobalExceptionHandler'a gidecek.
        // Service metodu yetki kontrolünü, NotFound hatalarını ve diğer exception'ları fırlatacak.

        // Güvenlik bağlamından (Spring Security) geçerli kullanıcının ID'sini al
        // Bu kullanıcı, quiz cevaplarını görüntülemek isteyen kişidir (Teacher veya Admin olması beklenir).
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentViewerId; // Cevapları görüntüleyen kullanıcının ID'si
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            try {
                 currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); // Varsayım: username String olarak ID'yi içerir ve int'e çevrilebilir
            } catch (NumberFormatException e) {
                 System.err.println("StatisticsController: reviewQuizAnswers - Principal username sayısal değil, ID olarak kullanılamaz.");
                 throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
            }
        } else {
             System.err.println("StatisticsController: reviewQuizAnswers - Principal beklenmeyen tipte: " + principal.getClass().getName());
             throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
        }


        // Service katmanındaki getQuizAnswersReview metodunu çağır (quizId ve görüntüleyen kullanıcı ID'si ile birlikte)
        // Service List<AnswerAttempt> Entity listesi döndürür veya exception fırlatır (Quiz yok, Yetkisiz, Kullanıcı yok gibi)
        List<AnswerAttempt> answerAttempts = statisticsService.getQuizAnswersReview(quizId, currentViewerId);

        // Entity listesini AnswerAttemptResponse DTO listesine dönüştür
        // AnswerAttemptResponse constructor'ını kullanarak dönüşümü yapıyoruz.
        List<AnswerAttemptResponse> answerAttemptResponses = answerAttempts.stream()
                                                                       .map(AnswerAttemptResponse::new) // Her AnswerAttempt Entity'sini AnswerAttemptResponse DTO'suna dönüştür
                                                                       // İsteğe bağlı: Belirli bir sıraya göre sıralama (örn: Soru numarasına göre)
                                                                       // .sorted(Comparator.comparingInt(AnswerAttemptResponse::getQuestionNumber))
                                                                       .collect(Collectors.toList()); // Sonucu List olarak topla


        System.out.println("StatisticsController: Quiz ID " + quizId + " icin " + answerAttemptResponses.size() + " adet cevap denemesi DTO'su oluşturuldu.");
        // Başarılı durumda 200 OK yanıtı ve DTO listesini döndür
        return ResponseEntity.ok(answerAttemptResponses);
    }

 // Belirli Bir Öğrencinin Tüm Quiz Sonuçlarını Getirme
    // HTTP GET isteği ile "/api/statistics/students/{studentId}/overall" adresine yapılan istekleri karşılar
    // (Öğrenci kendi sonucunu, Teacher kendi öğrencilerinin, Admin hepsini görebilir - Yetki kontrolü Service'te yapılıyor)
    @GetMapping("/students/{studentId}/overall")
    // Dönüş tipi ResponseEntity<List<QuizSessionResponse>> olarak güncellendi
    public ResponseEntity<List<QuizSessionResponse>> getStudentOverallResults(@PathVariable("studentId") int studentId) {
        System.out.println("StatisticsController: Öğrenci genel sonuçları isteniyor - Öğrenci ID: " + studentId);

        // try-catch bloğu yok, hatalar GlobalExceptionHandler'a gidecek.
        // Service metodu yetki kontrolünü, NotFound hatalarını ve diğer exception'ları fırlatacak.

        // Güvenlik bağlamından (Spring Security) geçerli kullanıcının ID'sini al
        // Bu kullanıcı, sonuçları görüntülemek isteyen kişidir (Kendisi, Teacher veya Admin olması beklenir).
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentViewerId; // Sonuçları görüntüleyen kullanıcının ID'si
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            try {
                 currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); // Varsayım: username String olarak ID'yi içerir ve int'e çevrilebilir
            } catch (NumberFormatException e) {
                 System.err.println("StatisticsController: getStudentOverallResults - Principal username sayısal değil, ID olarak kullanılamaz.");
                 throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
            }
        } else {
             System.err.println("StatisticsController: getStudentOverallResults - Principal beklenmeyen tipte: " + principal.getClass().getName());
             throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
        }


        // Service katmanındaki getStudentOverallResults metodunu çağır (studentId ve görüntüleyen kullanıcı ID'si ile birlikte)
        // Service List<QuizSession> Entity listesi döndürür veya exception fırlatır (Öğrenci yok, Yetkisiz, Kullanıcı yok gibi)
        List<QuizSession> sessions = statisticsService.getStudentOverallResults(studentId, currentViewerId);

        // Entity listesini QuizSessionResponse DTO listesine dönüştür
        // QuizSessionResponse constructor'ını kullanarak dönüşümü yapıyoruz.
        List<QuizSessionResponse> sessionResponses = sessions.stream()
                                                              .map(QuizSessionResponse::new) // Her QuizSession Entity'sini QuizSessionResponse DTO'suna dönüştür
                                                              // İsteğe bağlı: Oturumları başlangıç zamanına göre sıralama
                                                              // .sorted(Comparator.comparing(QuizSessionResponse::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                                                              .collect(Collectors.toList()); // Sonucu List olarak topla


        System.out.println("StatisticsController: Öğrenci ID " + studentId + " icin " + sessionResponses.size() + " adet oturum DTO'su oluşturuldu.");
        // Başarılı durumda 200 OK yanıtı ve DTO listesini döndür
        return ResponseEntity.ok(sessionResponses);
    }

 // Belirli Bir Öğrencinin Belirli Bir Quizdeki Sonucunu Getirme
    // HTTP GET isteği ile "/api/statistics/students/{studentId}/quizzes/{quizId}" adresine yapılan istekleri karşılar
    // (Öğrenci kendi sonucunu, Teacher kendi öğrencilerinin, Admin hepsini görebilir - Yetki kontrolü Service'te yapılıyor)
    @GetMapping("/students/{studentId}/quizzes/{quizId}")
    // Dönüş tipi ResponseEntity<QuizSessionDetailsResponse> olarak güncellendi
    public ResponseEntity<QuizSessionDetailsResponse> getStudentQuizResult(@PathVariable("studentId") int studentId,
                                                                          @PathVariable("quizId") int quizId) {
        System.out.println("StatisticsController: Öğrencinin quiz sonucu isteniyor - Öğrenci ID: " + studentId + ", Quiz ID: " + quizId);

        // try-catch bloğu yok, hatalar GlobalExceptionHandler'a gidecek.
        // Service metodu yetki kontrolünü, NotFound hatalarını ve diğer exception'ları fırlatacak.

        // Güvenlik bağlamından (Spring Security) geçerli kullanıcının ID'sini al
        // Bu kullanıcı, sonuçları görüntülemek isteyen kişidir (Kendisi, Teacher veya Admin olması beklenir).
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentViewerId; // Sonuçları görüntüleyen kullanıcının ID'si
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            try {
                 currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); // Varsayım: username String olarak ID'yi içerir ve int'e çevrilebilir
            } catch (NumberFormatException e) {
                 System.err.println("StatisticsController: getStudentQuizResult - Principal username sayısal değil, ID olarak kullanılamaz.");
                 throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
            }
        } else {
             System.err.println("StatisticsController: getStudentQuizResult - Principal beklenmeyen tipte: " + principal.getClass().getName());
             throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
        }


        // Service katmanındaki getStudentQuizResult metodunu çağır (studentId, quizId ve görüntüleyen kullanıcı ID'si ile birlikte)
        // Service QuizSession Entity'si döndürür veya exception fırlatır (Öğrenci yok, Quiz yok, Oturum yok, Yetkisiz gibi)
        QuizSession session = statisticsService.getStudentQuizResult(studentId, quizId, currentViewerId);

        // QuizSession Entity'sini QuizSessionDetailsResponse DTO'suna dönüştür
        // Bu DTO, cevap listesi dahil tüm detayları içerir.
        QuizSessionDetailsResponse sessionDetailsResponse = new QuizSessionDetailsResponse(session);


        System.out.println("StatisticsController: Öğrenci ID " + studentId + " ve Quiz ID " + quizId + " icin sonuç detayları başarıyla getirildi. Oturum ID: " + session.getId());
        // Başarılı durumda 200 OK yanıtı ve DTO'yu döndür
        return ResponseEntity.ok(sessionDetailsResponse);
    }

 // Belirli Bir Öğrencinin Ortalama Puanını Hesaplama ve Getirme
    // HTTP GET isteği ile "/api/statistics/students/{studentId}/average-score" adresine yapılan istekleri karşılar
    // (Öğrenci kendi sonucunu, Teacher kendi öğrencilerinin, Admin hepsini görebilir - Yetki kontrolü Service'te yapılıyor)
    @GetMapping("/students/{studentId}/average-score")
    // Dönüş tipi ResponseEntity<Double> olarak kalacak
    public ResponseEntity<Double> getStudentAverageScore(@PathVariable("studentId") int studentId) {
        System.out.println("StatisticsController: Öğrenci ortalama puanı isteniyor - Öğrenci ID: " + studentId);

        // try-catch bloğu yok, hatalar GlobalExceptionHandler'a gidecek.
        // Service metodu yetki kontrolünü, NotFound hatalarını ve diğer exception'ları fırlatacak.

        // Güvenlik bağlamından (Spring Security) geçerli kullanıcının ID'sini al
        // Bu kullanıcı, ortalama puanı görüntülemek isteyen kişidir (Kendisi, Teacher veya Admin olması beklenir).
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentViewerId; // Ortalamayı görüntüleyen kullanıcının ID'si
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            try {
                 currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); // Varsayım: username String olarak ID'yi içerir ve int'e çevrilebilir
            } catch (NumberFormatException e) {
                 System.err.println("StatisticsController: getStudentAverageScore - Principal username sayısal değil, ID olarak kullanılamaz.");
                 throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
            }
        } else {
             System.err.println("StatisticsController: getStudentAverageScore - Principal beklenmeyen tipte: " + principal.getClass().getName());
             throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
        }


        // Service katmanındaki calculateAverageScoreForStudent metodunu çağır (studentId ve görüntüleyen kullanıcı ID'si ile birlikte)
        // Service double değer döndürür veya exception fırlatır (Öğrenci yok, Yetkisiz, Kullanıcı yok gibi)
        double averageScore = statisticsService.calculateAverageScoreForStudent(studentId, currentViewerId);


        System.out.println("StatisticsController: Öğrenci ID " + studentId + " icin ortalama puan başarıyla getirildi: " + averageScore);
        // Başarılı durumda 200 OK yanıtı ve double değeri döndür
        return ResponseEntity.ok(averageScore);
    }


    // --- Diğer Endpoint'ler ---
    // Öğretmen istekleri (Admin) UserService'te olacak.
    // AI ile puanlama veya AI'dan cevap isteme (askAiToAnswerQuestion/Quiz) ayrı bir Controller'da olabilir (örn: AIController).
}