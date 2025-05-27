package com.example.QuizSystemProject.Controller; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.dto.AnswerSubmissionRequest; // İleride oluşturulacak DTO'yu import edin
import org.springframework.security.core.context.SecurityContextHolder; // <-- EKLENECEK
import org.springframework.security.core.Authentication; // <-- EKLENECEK
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails; // <-- EKLENECEK
// import com.quizland.QuizSystemProject.model.QuizSession; // <-- KALDIRILABİLİR (DTO döndüreceğiz)
// import com.quizland.QuizSystemProject.model.AnswerAttempt; // <-- KALDIRILABİLİR (DTO döndüreceğiz)
import com.example.QuizSystemProject.dto.QuizSessionResponse; // <-- EKLENECEK

// Diğer importlar (Valid, Autowired, HttpStatus, ResponseEntity, RestController, RequestMapping, PostMapping, PathVariable, List, Optional) mevcut olmalı.
import com.example.QuizSystemProject.Model.QuizSession; // QuizSession Entity'sini döndürmek için (DTO kullanmak daha iyi olabilir)
import com.example.QuizSystemProject.Model.AnswerAttempt; // AnswerAttempt Entity'sini döndürmek için (DTO kullanmak daha iyi olabilir)
import com.example.QuizSystemProject.Service.QuizSessionService; // QuizSessionService'i import edin
import com.example.QuizSystemProject.Service.StudentQuizService; // StudentQuizService'i import edin
import com.example.QuizSystemProject.dto.QuizSessionDetailsResponse;
import jakarta.validation.Valid; // Girdi doğrulama için

import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.web.bind.annotation.*; // Web anotasyonları için (@RestController, @RequestMapping, @PostMapping vb.)
import com.example.QuizSystemProject.dto.AnswerAttemptResponse;
import java.util.List; // List importu

import java.util.*;
import com.example.QuizSystemProject.dto.QuizWithQuestionsDto;

@RestController // Bu sınıfın bir REST Controller olduğunu belirtir
@RequestMapping("/api/sessions") // Bu controller altındaki tüm endpoint'lerin "/api/sessions" ile başlayacağını
                                 // belirtir
public class QuizSessionController {

    private final QuizSessionService quizSessionService; // Quiz Oturumu iş mantığı servisi
    private final StudentQuizService studentQuizService; // Quiz bilgilerini getirmek için

    // Bağımlılıkların enjekte edildiği constructor
    public QuizSessionController(QuizSessionService quizSessionService, StudentQuizService studentQuizService) {
        this.quizSessionService = quizSessionService;
        this.studentQuizService = studentQuizService;
    }

    // --- Quiz Oturumu Yönetimi Endpoint'leri (Genellikle Student yetkisi
    // gerektirir) ---

    // Yeni Quiz Oturumu Başlatma
    // HTTP POST isteği ile "/api/sessions/start/{quizId}" adresine yapılan
    // istekleri karşılar
    // (Authenticated Student yetkisi gerektirir - Bu yetki kontrolü genellikle
    // Spring Security config ile endpoint seviyesinde yapılır)
    @PostMapping("/start/{quizId}")
    public ResponseEntity<Map<String, Object>> startQuizSession(@PathVariable("quizId") int quizId) {
        System.out.println("QuizSessionController: Quiz oturumu başlatılıyor - Quiz ID: " + quizId);

        try {
            // Güvenlik bağlamından geçerli kullanıcının (öğrencinin) ID'sini al
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            int currentStudentId;
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                try {
                    currentStudentId = Integer.parseInt(((UserDetails) principal).getUsername());
                } catch (NumberFormatException e) {
                    System.err.println(
                            "QuizSessionController: startQuizSession - Principal username sayısal değil, ID olarak kullanılamaz.");
                    throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
                }
            } else {
                System.err.println("QuizSessionController: startQuizSession - Principal beklenmeyen tipte: "
                        + principal.getClass().getName());
                throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
            }

            // 1. Önce oturumu başlat
            QuizSession newSession = quizSessionService.startQuizSession(currentStudentId, quizId);

            // 2. Quiz içeriğini (sorular ve seçeneklerle birlikte) getir
            QuizWithQuestionsDto quizWithQuestions = studentQuizService.getQuizWithQuestions(quizId, currentStudentId);

            // 3. Oturum bilgilerini ve quiz içeriğini birleştir
            Map<String, Object> response = new HashMap<>();
            response.put("session", new QuizSessionResponse(newSession));
            response.put("quiz", quizWithQuestions);

            System.out.println(
                    "QuizSessionController: Quiz oturumu başarıyla başlatıldı - Oturum ID: " + newSession.getId());

            // Başarılı durumda 201 Created yanıtı ve yanıtı döndür
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("Quiz oturumu başlatılırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Cevap Gönderme/Kaydetme (Bir Soru İçin)
    // HTTP POST isteği ile "/api/sessions/{sessionId}/answer" adresine yapılan
    // istekleri karşılar
    // (Authenticated Student yetkisi ve aktif oturum gerektirir. Oturumun sahibi
    // olma kontrolü Service'te yapılır.)
    @PostMapping("/{sessionId}/answer")
    // Dönüş tipi ResponseEntity<AnswerAttemptResponse> olarak güncellendi
    public ResponseEntity<AnswerAttemptResponse> submitAnswer(@PathVariable("sessionId") int sessionId,
            @Valid @RequestBody AnswerSubmissionRequest answerSubmissionRequest) {
        System.out.println("QuizSessionController: Cevap gönderildi - Oturum ID: " + sessionId + ", Soru ID: "
                + answerSubmissionRequest.getQuestionId());

        // try-catch bloğu yok, hatalar GlobalExceptionHandler'a gidecek.

        // Güvenlik bağlamından (Spring Security) geçerli kullanıcının (öğrencinin)
        // ID'sini al
        // Bu kullanıcı, cevabı gönderen ve oturumun sahibi olması beklenen öğrencidir.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentStudentId; // Cevabı gönderen öğrencinin ID'si
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            try {
                currentStudentId = Integer.parseInt(((UserDetails) principal).getUsername());
            } catch (NumberFormatException e) {
                System.err.println(
                        "QuizSessionController: submitAnswer - Principal username sayısal değil, ID olarak kullanılamaz.");
                throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
            }
        } else {
            System.err.println("QuizSessionController: submitAnswer - Principal beklenmeyen tipte: "
                    + principal.getClass().getName());
            throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
        }

        // AnswerSubmissionRequest DTO'sundaki bilgileri Service metoduna ilet
        int questionId = answerSubmissionRequest.getQuestionId();
        String submittedAnswerText = answerSubmissionRequest.getSubmittedAnswerText();
        Set<Integer> selectedOptionIds = answerSubmissionRequest.getSelectedOptionIds();

        // Service katmanındaki submitAnswer metodunu çağır
        // NOT: Service metodunun, gelen currentStudentId'yi kullanarak oturumun sahibi
        // olup olmadığını kontrol etmesi beklenir.
        // Service AnswerAttempt Entity'si döndürür.
        AnswerAttempt savedAttempt = quizSessionService.submitAnswer(
                sessionId,
                questionId,
                submittedAnswerText,
                selectedOptionIds,
                currentStudentId // Cevabı gönderen öğrencinin ID'si Service'e iletiliyor
        );

        // Kaydedilen AnswerAttempt Entity'sini AnswerAttemptResponse DTO'suna dönüştür
        AnswerAttemptResponse attemptResponse = new AnswerAttemptResponse(savedAttempt);

        System.out.println("QuizSessionController: Cevap başarıyla kaydedildi - Attempt ID: " + savedAttempt.getId());
        // Başarılı durumda 200 OK yanıtı ve DTO'yu döndür
        return ResponseEntity.ok(attemptResponse);
    }

    // Bu metod StudentQuizSessionController'a taşındı
    // Lütfen /api/student/quiz-sessions/complete endpoint'ini kullanın

    // Belirli Bir Oturumun Detaylarını Getirme
    // HTTP GET isteği ile "/api/sessions/{sessionId}" adresine yapılan istekleri
    // karşılar
    // (Oturumun sahibi Öğrenci, Quizin Öğretmeni/Admini veya Admin yetkisi
    // gerektirir - yetki kontrolü Service'te yapılır)
    @GetMapping("/{sessionId}")
    // Dönüş tipi ResponseEntity<QuizSessionDetailsResponse> olarak güncellendi
    public ResponseEntity<QuizSessionDetailsResponse> getQuizSessionDetails(@PathVariable("sessionId") int sessionId) {
        System.out.println("QuizSessionController: Oturum detayları getiriliyor - Oturum ID: " + sessionId);

        // try-catch bloğu yok, hatalar GlobalExceptionHandler'a gidecek.
        // Service metodu artık Optional dönmüyor, NotFound veya Yetki hatası
        // fırlatıyor.

        // Güvenlik bağlamından (Spring Security) geçerli kullanıcının ID'sini al
        // Bu kullanıcı, oturum detaylarını görüntülemek isteyen kişidir.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentViewerId; // Oturumu görüntüleyen kullanıcının ID'si
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            try {
                currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername());
            } catch (NumberFormatException e) {
                System.err.println(
                        "QuizSessionController: getQuizSessionDetails - Principal username sayısal değil, ID olarak kullanılamaz.");
                throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
            }
        } else {
            System.err.println("QuizSessionController: getQuizSessionDetails - Principal beklenmeyen tipte: "
                    + principal.getClass().getName());
            throw new IllegalStateException("Güvenlik bağlamından kullanıcı ID'si alınamadı.");
        }

        // Service katmanındaki getQuizSessionDetails metodunu çağır (görüntüleyen
        // kullanıcı ID'si ile birlikte)
        // Service QuizSession Entity'si döndürür veya exception fırlatır.
        QuizSession session = quizSessionService.getQuizSessionDetails(sessionId, currentViewerId); // <-- viewerUserId
                                                                                                    // eklendi

        // QuizSession Entity'sini QuizSessionDetailsResponse DTO'suna dönüştür
        // Bu DTO, cevap listesini de içerir.
        QuizSessionDetailsResponse sessionDetailsResponse = new QuizSessionDetailsResponse(session);

        System.out
                .println("QuizSessionController: Oturum detayları başarıyla getirildi - Oturum ID: " + session.getId());
        // Başarılı durumda 200 OK yanıtı ve DTO'yu döndür
        return ResponseEntity.ok(sessionDetailsResponse);
    }

    // Bir Öğrencinin Tüm Oturumlarını Getirme
    // HTTP GET isteği ile "/api/sessions/student/{studentId}" adresine yapılan
    // istekleri karşılar
    // (Öğrenci kendi oturumlarını, Öğretmenler veya Adminler görebilir - yetki
    // kontrolü @PreAuthorize ile)
    @GetMapping("/student/{studentId}")
    // Dönüş tipi ResponseEntity<List<QuizSessionResponse>> olarak güncellendi

    // Yetkilendirme:
    // #studentId == authentication.principal.username : İsteği yapan kullanıcının
    // ID'si (username olarak tutuluyorsa) URL'deki studentId ile aynı mı?
    // OR hasRole('TEACHER') : Kullanıcının ROLE_TEACHER rolü var mı?
    // OR hasRole('ADMIN') : Kullanıcının ROLE_ADMIN rolü var mı?
    // SpEL (Spring Expression Language) kullanılarak yetki kontrolü yapılır.
    @PreAuthorize("#studentId == authentication.principal.username or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<QuizSessionResponse>> getStudentQuizSessions(@PathVariable("studentId") int studentId) {
        System.out.println("QuizSessionController: Öğrenci oturumları getiriliyor - Öğrenci ID: " + studentId);

        // try-catch bloğu yok, Service'ten fırlatılan hatalar GlobalExceptionHandler'a
        // gidecek.
        // @PreAuthorize anotasyonu sayesinde yetki kontrolü buraya gelmeden yapılmış
        // olur.

        // Service katmanındaki getStudentQuizSessions metodunu çağır
        // Service List<QuizSession> döndürür.
        List<QuizSession> sessions = quizSessionService.getStudentQuizSessions(studentId);

        // Entity listesini DTO listesine dönüştür
        // Burada sizin QuizSessionResponse DTO'sunu kullanıyoruz.
        List<QuizSessionResponse> sessionResponses = sessions.stream()
                .map(QuizSessionResponse::new) // Her QuizSession Entity'sini QuizSessionResponse DTO'suna dönüştür
                // Oturumları başlangıç zamanına göre sıralamak iyi olabilir
                .sorted((s1, s2) -> {
                    if (s1.getStartTime() != null && s2.getStartTime() != null) {
                        return s1.getStartTime().compareTo(s2.getStartTime());
                    }
                    return 0; // Null başlangıç zamanları için sıralama yapma
                })
                .collect(Collectors.toList()); // Sonucu List olarak topla

        System.out.println("QuizSessionController: Öğrenci ID " + studentId + " icin " + sessionResponses.size()
                + " adet oturum DTO'su oluşturuldu.");
        // Başarılı durumda 200 OK yanıtı ve DTO listesini döndür
        return ResponseEntity.ok(sessionResponses);
    }

    // --- Diğer Endpoint'ler ---
    // İstatistikler (ortalama puan vb.) StatisticsController'da olacak.
    // Duration hesaplama Service'te bir metod olarak kalır.
}