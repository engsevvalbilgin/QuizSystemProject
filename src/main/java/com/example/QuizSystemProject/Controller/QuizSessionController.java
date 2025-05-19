package com.example.QuizSystemProject.Controller;


import com.example.QuizSystemProject.Model.AnswerAttempt;
import com.example.QuizSystemProject.Model.QuizSession;
import com.example.QuizSystemProject.Service.QuizSessionService;
import com.example.QuizSystemProject.dto.AnswerSubmissionRequest;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.web.bind.annotation.*; // Web anotasyonları için (@RestController, @RequestMapping, @PostMapping vb.)

import java.util.List; // List importu
import java.util.Optional; // Optional importu

@RestController // Bu sınıfın bir REST Controller olduğunu belirtir
@RequestMapping("/api/sessions") // Bu controller altındaki tüm endpoint'lerin "/api/sessions" ile başlayacağını belirtir
public class QuizSessionController {

    private final QuizSessionService quizSessionService; // Quiz Oturumu iş mantığı servisi

    // QuizSessionService bağımlılığının enjekte edildiği constructor
    @Autowired
    public QuizSessionController(QuizSessionService quizSessionService) {
        this.quizSessionService = quizSessionService;
    }

    // --- Quiz Oturumu Yönetimi Endpoint'leri (Genellikle Student yetkisi gerektirir) ---

    // Yeni Quiz Oturumu Başlatma
    // HTTP POST isteği ile "/api/sessions/start/{quizId}" adresine yapılan istekleri karşılar
    // (Authenticated Student yetkisi gerektirir)
    @PostMapping("/start/{quizId}")
    public ResponseEntity<QuizSession> startQuizSession(@PathVariable("quizId") Long quizId) {
        System.out.println("QuizSessionController: Quiz oturumu başlatılıyor - Quiz ID: " + quizId);
        // NOT: Gerçek implementasyonda, Spring Security bağlamından geçerli öğrenci ID'sini alıp
        // quizId ile birlikte Service katmanındaki startQuizSession metodunu çağırma,
        // oluşturulan QuizSession'ı (veya DTO'sunu) 201 Created yanıtı ile döndürme mantığı olacak.

        // Long currentStudentId = ... // Güvenlik bağlamından alınacak geçerli öğrenci ID'si
        // QuizSession newSession = quizSessionService.startQuizSession(currentStudentId, quizId);
        // return ResponseEntity.status(HttpStatus.CREATED).body(newSession);

        return ResponseEntity.status(HttpStatus.CREATED).body(new QuizSession()); // Şimdilik simülasyon
    }

    // Cevap Gönderme/Kaydetme (Bir Soru İçin)
    // HTTP POST isteği ile "/api/sessions/{sessionId}/answer" adresine yapılan istekleri karşılar
    // (Authenticated Student yetkisi ve aktif oturum gerektirir)
    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<AnswerAttempt> submitAnswer(@PathVariable("sessionId") Long sessionId,
                                                       @Valid @RequestBody AnswerSubmissionRequest answerSubmissionRequest) {
        System.out.println("QuizSessionController: Cevap gönderildi - Oturum ID: " + sessionId + ", Soru ID: " + answerSubmissionRequest.getQuestionId());
        // NOT: Gerçek implementasyonda, sessionId, istekten gelen DTO'daki questionId,
        // submittedAnswerText ve selectedOptionIds bilgilerini kullanarak
        // Service katmanındaki submitAnswer metodunu çağırma,
        // kaydedilen AnswerAttempt'ı (veya DTO'sunu) 200 OK yanıtı ile döndürme mantığı olacak.
        // Yetki kontrolü (cevap gönderen öğrencinin oturumun sahibi olması) Service veya Controller'da yapılır.

        // AnswerAttempt savedAttempt = quizSessionService.submitAnswer(
        //     sessionId,
        //     answerSubmissionRequest.getQuestionId(),
        //     answerSubmissionRequest.getSubmittedAnswerText(),
        //     answerSubmissionRequest.getSelectedOptionIds()
        // );
        // return ResponseEntity.ok(savedAttempt);

        return ResponseEntity.ok(new AnswerAttempt()); // Şimdilik simülasyon
    }

    // Quiz Oturumunu Tamamlama
    // HTTP POST isteği ile "/api/sessions/{sessionId}/complete" adresine yapılan istekleri karşılar
    // (Authenticated Student yetkisi ve aktif oturum gerektirir)
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<QuizSession> completeQuizSession(@PathVariable("sessionId") Long sessionId) {
        System.out.println("QuizSessionController: Quiz oturumu tamamlanıyor - Oturum ID: " + sessionId);
        // NOT: Gerçek implementasyonda, sessionId kullanarak Service katmanındaki completeQuizSession metodunu çağırma,
        // tamamlanan ve puanlanan QuizSession'ı (veya DTO'sunu) 200 OK yanıtı ile döndürme mantığı olacak.
        // Yetki kontrolü (oturumun sahibi olma) Service veya Controller'da yapılır.

        // QuizSession completedSession = quizSessionService.completeQuizSession(sessionId);
        // return ResponseEntity.ok(completedSession);

        return ResponseEntity.ok(new QuizSession()); // Şimdilik simülasyon
    }

    // Belirli Bir Oturumun Detaylarını Getirme
    // HTTP GET isteği ile "/api/sessions/{sessionId}" adresine yapılan istekleri karşılar
    // (Öğrenci kendi oturumunu, Teacher kendi öğrencilerinin oturumlarını, Admin hepsini görebilir - yetki kontrolü Service/Controller/Security Config'te)
    @GetMapping("/{sessionId}")
    public ResponseEntity<QuizSession> getQuizSessionDetails(@PathVariable("sessionId") Long sessionId) {
        System.out.println("QuizSessionController: Oturum detayları getiriliyor - Oturum ID: " + sessionId);
        // NOT: Gerçek implementasyonda, sessionId kullanarak Service katmanındaki getQuizSessionDetails metodunu çağırma,
        // eğer oturum varsa DTO'ya dönüştürüp 200 OK döndürme, yoksa 404 Not Found döndürme mantığı olacak.
        // Yetki kontrolü burada veya Service'te çok önemlidir.

        // Optional<QuizSession> sessionOptional = quizSessionService.getQuizSessionDetails(sessionId);
        // return sessionOptional.map(session -> ResponseEntity.ok(session))
        //                      .orElseGet(() -> ResponseEntity.notFound().build());

        return ResponseEntity.ok(new QuizSession()); // Şimdilik simülasyon
    }


    // Bir Öğrencinin Tüm Oturumlarını Getirme
    // HTTP GET isteği ile "/api/sessions/student/{studentId}" adresine yapılan istekleri karşılar
    // (Admin veya Teacher kendi öğrencilerinin oturumlarını görebilir - yetki kontrolü Security Config'te)
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<QuizSession>> getStudentQuizSessions(@PathVariable("studentId") Long studentId) {
        System.out.println("QuizSessionController: Öğrenci oturumları getiriliyor - Öğrenci ID: " + studentId);
        // NOT: Gerçek implementasyonda, studentId kullanarak Service katmanındaki getStudentQuizSessions metodunu çağırma,
        // oturum listesini DTO listesine dönüştürüp döndürme mantığı olacak.
        // Yetki kontrolü burada çok önemlidir.

        // List<QuizSession> sessions = quizSessionService.getStudentQuizSessions(studentId);
        // return ResponseEntity.ok(sessions);

        return ResponseEntity.ok(List.of(new QuizSession())); // Şimdilik simülasyon
    }

    // --- Diğer Endpoint'ler ---
    // İstatistikler (ortalama puan vb.) StatisticsController'da olacak.
    // Duration hesaplama Service'te bir metod olarak kalır.
}

