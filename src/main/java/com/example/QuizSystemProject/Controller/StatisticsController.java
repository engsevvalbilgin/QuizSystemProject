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

     private final StatisticsService statisticsService;

     public StatisticsController(StatisticsService statisticsService) {
          this.statisticsService = statisticsService;
     }

     @GetMapping("/student-leaders")
     @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
     public ResponseEntity<List<StudentOverallResultsResponse>> getStudentLeaders() {
          System.out.println("StatisticsController: Liderlik tablosu isteniyor");
          List<StudentOverallResultsResponse> leaders = statisticsService.getStudentLeaders();
          System.out.println("StatisticsController: Toplam " + leaders.size());
          return ResponseEntity.ok(leaders);
     }

     @GetMapping("/overall")
     @PreAuthorize("hasRole('ROLE_ADMIN')")
     public ResponseEntity<OverallStatsResponse> getOverallStatistics() {
          System.out.println("StatisticsController: Genel istatistikler isteniyor.");
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

          if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
               System.err.println(
                         "StatisticsController: getOverallStatistics - Güvenlik bağlamında geçerli CustomUserDetails bulunamadı.");
               throw new IllegalStateException("Kimlik doğrulama başarısız veya kullanıcı detayı beklenmiyor.");
          }

          CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
          int currentViewerId = userDetails.getId();
          System.out.println(
                    "StatisticsController: Genel istatistikleri isteyen kullanıcının ID'si: " + currentViewerId);

          OverallStatsResponse stats = statisticsService.getOverallProgramStatistics(currentViewerId);
          System.out.println("StatisticsController: Genel istatistikler Service tarafından başarıyla getirildi.");
          return ResponseEntity.ok(stats);
     }

     @GetMapping("/quizzes/{quizId}")
     public ResponseEntity<QuizStatsResponse> getQuizStatistics(@PathVariable("quizId") int quizId) {
          System.out.println("StatisticsController: Quiz istatistikleri isteniyor - Quiz ID: " + quizId);

          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          int currentViewerId; 
          Object principal = authentication.getPrincipal();

          if (principal instanceof UserDetails) {
               try {
                    currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); 
                                                                                                
               } catch (NumberFormatException e) {
                    System.err.println(
                              "StatisticsController: getQuizStatistics - Principal username sayısal değil, ID olarak kullanılamaz.");
                    throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
               }
          } else {
               System.err.println("StatisticsController: getQuizStatistics - Principal beklenmeyen tipte: "
                         + principal.getClass().getName());
               throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
          }

          QuizStatsResponse stats = statisticsService.getQuizStatistics(quizId, currentViewerId);

          System.out.println("StatisticsController: Quiz istatistikleri başarıyla getirildi - Quiz ID: " + quizId);
          return ResponseEntity.ok(stats);
     }

     @GetMapping("/quizzes/{quizId}/answers")
     public ResponseEntity<List<AnswerAttemptResponse>> reviewQuizAnswers(@PathVariable("quizId") int quizId) {
          System.out.println("StatisticsController: Quiz cevapları gözden geçirme isteniyor - Quiz ID: " + quizId);
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          int currentViewerId; 
          Object principal = authentication.getPrincipal();

          if (principal instanceof UserDetails) {
               try {
                    currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); 
                                                                                                
               } catch (NumberFormatException e) {
                    System.err.println(
                              "StatisticsController: reviewQuizAnswers - Principal username sayısal değil, ID olarak kullanılamaz.");
                    throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
               }
          } else {
               System.err.println("StatisticsController: reviewQuizAnswers - Principal beklenmeyen tipte: "
                         + principal.getClass().getName());
               throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
          }

          List<AnswerAttempt> answerAttempts = statisticsService.getQuizAnswersReview(quizId, currentViewerId);

          List<AnswerAttemptResponse> answerAttemptResponses = answerAttempts.stream()
                    .map(AnswerAttemptResponse::new) 
                    .collect(Collectors.toList()); 

          System.out.println("StatisticsController: Quiz ID " + quizId + " icin " + answerAttemptResponses.size()
                    + " adet cevap denemesi DTO'su oluşturuldu.");
          return ResponseEntity.ok(answerAttemptResponses);
     }

     @GetMapping("/students/{studentId}/overall")
     public ResponseEntity<List<QuizSessionResponse>> getStudentOverallResults(
               @PathVariable("studentId") int studentId) {
          System.out.println("StatisticsController: Öğrenci genel sonuçları isteniyor - Öğrenci ID: " + studentId);
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          int currentViewerId; 
          Object principal = authentication.getPrincipal();

          if (principal instanceof UserDetails) {
               try {
                    currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); 
                                                                                                 
                                                                                                 
               } catch (NumberFormatException e) {
                    System.err.println(
                              "StatisticsController: getStudentOverallResults - Principal username sayısal değil, ID olarak kullanılamaz.");
                    throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
               }
          } else {
               System.err.println("StatisticsController: getStudentOverallResults - Principal beklenmeyen tipte: "
                         + principal.getClass().getName());
               throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
          }

          List<QuizSession> sessions = statisticsService.getStudentOverallResults(studentId, currentViewerId);

          List<QuizSessionResponse> sessionResponses = sessions.stream()
                    .map(QuizSessionResponse::new) 
                    .collect(Collectors.toList()); 

          System.out.println("StatisticsController: Öğrenci ID " + studentId + " icin " + sessionResponses.size()
                    + " adet oturum DTO'su oluşturuldu.");
          return ResponseEntity.ok(sessionResponses);
     }

     @GetMapping("/students/{studentId}/quizzes/{quizId}")
     public ResponseEntity<QuizSessionDetailsResponse> getStudentQuizResult(@PathVariable("studentId") int studentId,
               @PathVariable("quizId") int quizId) {
          System.out.println("StatisticsController: Öğrencinin quiz sonucu isteniyor - Öğrenci ID: " + studentId
                    + ", Quiz ID: " + quizId);

          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          int currentViewerId; 
          Object principal = authentication.getPrincipal();

          if (principal instanceof UserDetails) {
               try {
                    currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); 
                                                                                                 
                                                                                                 
               } catch (NumberFormatException e) {
                    System.err.println(
                              "StatisticsController: getStudentQuizResult - Principal username sayısal değil, ID olarak kullanılamaz.");
                    throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
               }
          } else {
               System.err.println("StatisticsController: getStudentQuizResult - Principal beklenmeyen tipte: "
                         + principal.getClass().getName());
               throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
          }

          QuizSession session = statisticsService.getStudentQuizResult(studentId, quizId, currentViewerId);

          QuizSessionDetailsResponse sessionDetailsResponse = new QuizSessionDetailsResponse(session);

          System.out.println("StatisticsController: Öğrenci ID " + studentId + " ve Quiz ID " + quizId
                    + " icin sonuç detayları başarıyla getirildi. Oturum ID: " + session.getId());
          return ResponseEntity.ok(sessionDetailsResponse);
     }

     @GetMapping("/students/{studentId}/average-score")
     public ResponseEntity<Double> getStudentAverageScore(@PathVariable("studentId") int studentId) {
          System.out.println("StatisticsController: Öğrenci ortalama puanı isteniyor - Öğrenci ID: " + studentId);

          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          int currentViewerId; 
          Object principal = authentication.getPrincipal();

          if (principal instanceof UserDetails) {
               try {
                    currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername()); 
                                                                                                 
                                                                                                 
               } catch (NumberFormatException e) {
                    System.err.println(
                              "StatisticsController: getStudentAverageScore - Principal username sayısal değil, ID olarak kullanılamaz.");
                    throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
               }
          } else {
               System.err.println("StatisticsController: getStudentAverageScore - Principal beklenmeyen tipte: "
                         + principal.getClass().getName());
               throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
          }

          double averageScore = statisticsService.calculateAverageScoreForStudent(studentId, currentViewerId);

          System.out.println("StatisticsController: Öğrenci ID " + studentId
                    + " icin ortalama puan başarıyla getirildi: " + averageScore);
          return ResponseEntity.ok(averageScore);
     }

}