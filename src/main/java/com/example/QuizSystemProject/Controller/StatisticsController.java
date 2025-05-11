package com.example.QuizSystemProject.Controller;


//İstatistik sonuçları için DTO'lar ileride tanımlanabilir (örn: OverallStatsResponse, QuizStatsResponse vb.)
//Şimdilik Entity'leri veya Map'leri döndürelim veya placeholder yanıtlar verelim.
import com.example.QuizSystemProject.Model.AnswerAttempt; // Cevapları döndürmek için (DTO'ya çevrilmeli)
import com.example.QuizSystemProject.Model.QuizSession; // Oturumları döndürmek için (DTO'ya çevrilmeli)

import com.example.QuizSystemProject.Service.*;
import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.web.bind.annotation.*; // Web anotasyonları için (@RestController, @RequestMapping, @GetMapping vb.)

import java.util.List; // List importu
import java.util.Map; // Map importu (genel istatistikler için kullanılabilir)
import java.util.Optional; // Optional importu

@RestController // Bu sınıfın bir REST Controller olduğunu belirtir
@RequestMapping("/api/statistics") // Bu controller altındaki tüm endpoint'lerin "/api/statistics" ile başlayacağını belirtir
public class StatisticsController {

 private final StatisticsService statisticsService; // İstatistik iş mantığı servisi

 // StatisticsService bağımlılığının enjekte edildiği constructor
 @Autowired
 public StatisticsController(StatisticsService statisticsService) {
     this.statisticsService = statisticsService;
 }

 // --- İstatistik Endpoint'leri ---

 // Programın Genel İstatistiklerini Getirme
 // HTTP GET isteği ile "/api/statistics/overall" adresine yapılan istekleri karşılar
 // (Sadece Admin yetkisi gerektirir)
 @GetMapping("/overall")
 public ResponseEntity<Map<String, Object>> getOverallStatistics() {
     System.out.println("StatisticsController: Genel istatistikler isteniyor.");
     // NOT: Gerçek implementasyonda, Service'ten genel istatistikleri çekme
     // ve Map veya özel bir DTO objesi olarak 200 OK yanıtı ile döndürme mantığı olacak.
     // Spring Security ile bu endpoint'in sadece ADMIN rolüne sahip kullanıcılar tarafından çağrılmasını sağlayacağız.

     // Map<String, Object> stats = statisticsService.getOverallProgramStatistics();
     // return ResponseEntity.ok(stats);

     return ResponseEntity.ok(Map.of("message", "Genel istatistikler burada olacak (Simülasyon)")); // Şimdilik simülasyon
 }

 // Belirli Bir Quiz'in İstatistiklerini Getirme
 // HTTP GET isteği ile "/api/statistics/quizzes/{quizId}" adresine yapılan istekleri karşılar
 // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
 @GetMapping("/quizzes/{quizId}")
 public ResponseEntity<Map<String, Object>> getQuizStatistics(@PathVariable("quizId") Long quizId) {
     System.out.println("StatisticsController: Quiz istatistikleri isteniyor - Quiz ID: " + quizId);
     // NOT: Gerçek implementasyonda, quizId ile Service'ten quiz istatistiklerini çekme,
     // eğer varsa Map veya özel bir DTO objesi olarak 200 OK yanıtı döndürme,
     // yoksa 404 Not Found döndürme mantığı olacak. Yetki kontrolü Service veya Controller'da yapılır.

     // try {
     //     Map<String, Object> stats = statisticsService.getQuizStatistics(quizId);
     //     return ResponseEntity.ok(stats);
     // } catch (RuntimeException e) { // Service'ten hata fırlatılmışsa
     //      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
     // }

     return ResponseEntity.ok(Map.of("message", "Quiz " + quizId + " istatistikleri burada olacak (Simülasyon)")); // Şimdilik simülasyon
 }

 // Belirli Bir Quiz'in Cevaplarını Gözden Geçirme
 // HTTP GET isteği ile "/api/statistics/quizzes/{quizId}/answers" adresine yapılan istekleri karşılar
 // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
 @GetMapping("/quizzes/{quizId}/answers")
 public ResponseEntity<List<AnswerAttempt>> reviewQuizAnswers(@PathVariable("quizId") Long quizId) {
     System.out.println("StatisticsController: Quiz cevapları gözden geçirme isteniyor - Quiz ID: " + quizId);
     // NOT: Gerçek implementasyonda, quizId ile Service'ten ilgili AnswerAttempt listesini çekme,
     // bu listeyi DTO listesine dönüştürüp 200 OK yanıtı ile döndürme mantığı olacak.
     // Yetki kontrolü Service veya Controller'da yapılır.

     // List<AnswerAttempt> answerAttempts = statisticsService.getQuizAnswersReview(quizId);
     // return ResponseEntity.ok(answerAttempts); // DTO listesi döndürülmeli

     return ResponseEntity.ok(List.of(new AnswerAttempt())); // Şimdilik simülasyon
 }

 // Belirli Bir Öğrencinin Tüm Quiz Sonuçlarını Getirme
 // HTTP GET isteği ile "/api/statistics/students/{studentId}/overall" adresine yapılan istekleri karşılar
 // (Öğrenci kendi sonucunu, Teacher kendi öğrencilerinin, Admin hepsini görebilir - yetki kontrolü Service/Controller/Security Config'te)
 @GetMapping("/students/{studentId}/overall")
 public ResponseEntity<List<QuizSession>> getStudentOverallResults(@PathVariable("studentId") Long studentId) {
     System.out.println("StatisticsController: Öğrenci genel sonuçları isteniyor - Öğrenci ID: " + studentId);
      // NOT: Gerçek implementasyonda, studentId ile Service'ten ilgili QuizSession listesini çekme,
      // bu listeyi DTO listesine dönüştürüp 200 OK yanıtı ile döndürme mantığı olacak.
      // Yetki kontrolü burada çok önemlidir (örn: studentId path değişkeni ile istek yapan kullanıcının ID'si aynı mı? veya istek yapan Admin/Teacher mı?).

      // List<QuizSession> sessions = statisticsService.getStudentOverallResults(studentId);
      // return ResponseEntity.ok(sessions); // DTO listesi döndürülmeli

      return ResponseEntity.ok(List.of(new QuizSession())); // Şimdilik simülasyon
 }

 // Belirli Bir Öğrencinin Belirli Bir Quizdeki Sonucunu Getirme
 // HTTP GET isteği ile "/api/statistics/students/{studentId}/quizzes/{quizId}" adresine yapılan istekleri karşılar
 // (Öğrenci kendi sonucunu, Teacher kendi öğrencilerinin, Admin hepsini görebilir - yetki kontrolü Security Config'te)
  @GetMapping("/students/{studentId}/quizzes/{quizId}")
  public ResponseEntity<QuizSession> getStudentQuizResult(@PathVariable("studentId") Long studentId,
                                                          @PathVariable("quizId") Long quizId) {
      System.out.println("StatisticsController: Öğrencinin quiz sonucu isteniyor - Öğrenci ID: " + studentId + ", Quiz ID: " + quizId);
       // NOT: Gerçek implementasyonda, studentId ve quizId kullanarak Service'ten ilgili oturumu çekme,
       // eğer varsa DTO'ya dönüştürüp 200 OK döndürme, yoksa 404 Not Found döndürme mantığı olacak.
       // Yetki kontrolü burada çok önemlidir.

       // Optional<QuizSession> sessionOptional = statisticsService.getStudentQuizResult(studentId, quizId);
       // return sessionOptional.map(session -> ResponseEntity.ok(session)) // Eğer oturum varsa 200 OK
       //                       .orElseGet(() -> ResponseEntity.notFound().build()); // Yoksa 404 Not Found

       return ResponseEntity.ok(new QuizSession()); // Şimdilik simülasyon
  }

 // Belirli Bir Öğrencinin Ortalama Puanını Hesaplama ve Getirme
 // HTTP GET isteği ile "/api/statistics/students/{studentId}/average-score" adresine yapılan istekleri karşılar
 // (Öğrenci kendi sonucunu, Teacher kendi öğrencilerinin, Admin hepsini görebilir - yetki kontrolü Security Config'te)
  @GetMapping("/students/{studentId}/average-score")
  public ResponseEntity<Double> getStudentAverageScore(@PathVariable("studentId") Long studentId) {
      System.out.println("StatisticsController: Öğrenci ortalama puanı isteniyor - Öğrenci ID: " + studentId);
      // NOT: Gerçek implementasyonda, studentId kullanarak Service'ten ortalama puanı hesaplatma ve döndürme mantığı olacak.
      // Yetki kontrolü burada çok önemlidir.

      // double averageScore = statisticsService.calculateAverageScoreForStudent(studentId);
      // return ResponseEntity.ok(averageScore);

       return ResponseEntity.ok(85.0); // Şimdilik simülasyon
  }


 // --- Diğer Endpoint'ler ---
 // Öğretmen istekleri (Admin) UserService'te olacak.
 // AI ile puanlama veya AI'dan cevap isteme (askAiToAnswerQuestion/Quiz) ayrı bir Controller'da olabilir (örn: AIController).
}

