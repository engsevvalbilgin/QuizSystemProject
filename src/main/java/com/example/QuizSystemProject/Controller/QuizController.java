package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.dto.*;
import com.example.QuizSystemProject.exception.*;
import com.example.QuizSystemProject.Model.*;
import com.example.QuizSystemProject.Repository.QuizRepository;
import com.example.QuizSystemProject.Repository.QuestionRepository;
import com.example.QuizSystemProject.Service.QuizService;
import com.example.QuizSystemProject.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.security.core.GrantedAuthority;
import jakarta.validation.Valid;
// Unused import removed
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.lang.Exception;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController 
@RequestMapping("/api/quizzes") 
public class QuizController {

    private final QuizService quizService;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    // QuizService ve QuizRepository bağımlılıklarının enjekte edildiği constructor
    // @Autowired annotation'ı burada gerekli değil, Spring otomatik olarak enjekte eder.
    public QuizController(QuizService quizService, QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizService = quizService;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    // Helper method to get current user ID from Authentication principal
    private int getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            // Eğer principal UserDetails ise (genel Spring Security User objesi gibi),
            // buradan kullanıcı adını alıp veritabanından kullanıcı ID'sini bulmak gerekebilir.
            // Ancak CustomUserDetails kullanılıyorsa, doğrudan cast etmek daha doğru.
            // Bu kısım projenizin kimlik doğrulama yapısına göre değişebilir.
            throw new SecurityException("Principal is not an instance of CustomUserDetails or UserDetails.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        return userDetails.getId();
    }

 // --- Quiz Yönetimi Endpoint'leri (Genellikle Teacher/Admin yetkisi gerektirir) ---

    // Tüm Quizleri Getirme (Opsiyonel olarak filtrelenebilir, herkes görebilir veya yetkiye göre değişir)
    // HTTP GET isteği ile "/api/quizzes" adresine yapılan istekleri karşılar
    @GetMapping // Bu metodun kapatılan (comment-out) kısmını buraya taşıdım.
    public ResponseEntity<List<QuizResponse>> getAllQuizzes() {
        System.out.println("QuizController: Tüm quizler listeleniyor.");

        // Service katmanındaki tüm quizleri getiren metodu çağır
        List<Quiz> quizzes = quizService.getAllQuizzes();

        // Entity listesini DTO listesine dönüştür
        List<QuizResponse> quizResponses = quizzes.stream()
                                            .map(QuizResponse::new) // Her Quiz Entity'sini QuizResponse DTO'suna dönüştür
                                            .collect(Collectors.toList()); // Sonucu List olarak topla

        System.out.println("QuizController: " + quizResponses.size() + " quiz DTO'su oluşturuldu.");
        // Başarılı durumda 200 OK yanıtı ve DTO listesini döndür
        return ResponseEntity.ok(quizResponses);
    }

    // Öğretmenin Quizlerini Getirme (Oturum açmış kullanıcı için)
    // HTTP GET isteği ile "/api/quizzes/teacher-quizzes" adresine yapılan istekleri karşılar
    // (Teacher yetkisi gerektirir)
    @GetMapping("/teacher-quizzes")
    public ResponseEntity<List<QuizResponse>> getTeacherQuizzes() {
        System.out.println("QuizController: Öğretmenin quizleri getiriliyor");

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentTeacherId = getCurrentUserId(authentication);

        // Service katmanındaki getQuizzesByTeacher metodunu çağır
        List<Quiz> teacherQuizzes = quizService.getQuizzesByTeacher(currentTeacherId);

        // Entity listesini DTO listesine dönüştür
        List<QuizResponse> quizResponses = teacherQuizzes.stream()
                .map(QuizResponse::new)
                .collect(Collectors.toList());

        System.out.println("QuizController: " + quizResponses.size() + " quiz DTO'su oluşturuldu.");
        return ResponseEntity.ok(quizResponses);
    }
    
    // Belirli bir öğretmenin tüm quizlerini getirme (Frontend'in kullandığı endpoint)
    // HTTP GET isteği ile "/api/quizzes/teacher/{teacherId}" adresine yapılan istekleri karşılar
    // (Teacher/Admin yetkisi gerektirir)
    
    // Endpoint to activate a specific quiz
    @PutMapping("/{quizId}/activate")
    public ResponseEntity<?> activateQuiz(@PathVariable("quizId") int quizId) {
        System.out.println("QuizController: Activating quiz ID: " + quizId);
        
        // Get current user for authorization
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUserId = getCurrentUserId(authentication);
        
        try {
            // Find the quiz using the repository
            Optional<Quiz> quizOpt = quizRepository.findById(quizId);
            
            if (!quizOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Quiz not found with ID: " + quizId);
            }
            
            Quiz quiz = quizOpt.get();
            
            // Check authorization
            boolean isQuizTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == currentUserId;
            boolean isAdmin = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .map(auth -> "ROLE_ADMIN".equals(auth))
                    .orElse(false);
                    
            if (!isQuizTeacher && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to activate this quiz");
            }
            
            // Set quiz to active and save directly
            quiz.setActive(true);
            quizRepository.save(quiz);
            
            System.out.println("QuizController: Quiz activated successfully - ID: " + quizId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Quiz activated successfully",
                "quizId", quizId,
                "isActive", true
            ));
            
        } catch (Exception e) {
            System.err.println("Error activating quiz: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating quiz: " + e.getMessage());
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<QuizResponse>> getQuizzesByTeacherId(@PathVariable("teacherId") int teacherId) {
        System.out.println("QuizController: TeacherID ile quizler getiriliyor - Teacher ID: " + teacherId);
        
        // Güncel kullanıcı kimliğini al (yetki kontrolü için)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUserId = getCurrentUserId(authentication);
        
        // Kullanıcı kendi quizlerini mi yoksa başka bir öğretmenin quizlerini mi görüntülüyor?
        boolean isViewingOwnQuizzes = currentUserId == teacherId;
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("UNKNOWN");
        boolean isAdmin = "ROLE_ADMIN".equals(role);
        
        System.out.println("QuizController: Kullanıcı kendi quizlerini mi görüntülüyor: " + isViewingOwnQuizzes + ", Admin mi: " + isAdmin);
        
        // İstenen öğretmenin quizlerini getir
        List<Quiz> teacherQuizzes = quizService.getQuizzesByTeacher(teacherId);
        
        // Entity listesini DTO listesine dönüştür ve manuel olarak soru sayısını ayarla
        List<QuizResponse> quizResponses = teacherQuizzes.stream()
                .map(quiz -> {
                    QuizResponse response = new QuizResponse(quiz);
                    // JPA'nın lazy loading sorununu engellemek için soru sayısını manuel olarak ayarla
                    int questionCount = quizService.getQuestionCountForQuiz(quiz.getId());
                    response.setQuestionCount(questionCount);
                    return response;
                })
                .collect(Collectors.toList());
                
        System.out.println("QuizController: Teacher ID " + teacherId + " için " + quizResponses.size() + " quiz bulundu.");
        return ResponseEntity.ok(quizResponses);
    }

 // ID'ye Göre Quiz Getirme
    // HTTP GET isteği ile "/api/quizzes/{id}" adresine yapılan istekleri karşılar
    @GetMapping("/{id}")
    // Dönüş tipi ResponseEntity<QuizDetailsResponse> olarak güncellendi
    public ResponseEntity<QuizDetailsResponse> getQuizById(@PathVariable("id") int id) {

        System.out.println("QuizController: Quiz detayları getiriliyor - ID: " + id);

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUserId = getCurrentUserId(authentication);
        System.out.println("QuizController: Güncel kullanıcı ID: " + currentUserId);

        try {
            // Yetki kontrolü yapan getQuizById metodunu çağır
            Optional<Quiz> quizOptional = quizService.getQuizById(id, currentUserId);

            // Optional boş değilse (quiz bulunduysa)
            return quizOptional.map(quiz -> {
                        System.out.println("QuizController: Quiz bulundu - ID: " + id);
                        // Quizi QuizDetailsResponse DTO'suna dönüştür
                        QuizDetailsResponse quizDetailsResponse = new QuizDetailsResponse(quiz);
                        // 200 OK yanıtı ve DTO'yu döndür
                        return ResponseEntity.ok(quizDetailsResponse);
                    })
                    // Optional boşsa (quiz bulunamadıysa)
                    .orElseGet(() -> {
                        System.err.println("QuizController: Quiz bulunamadi - ID: " + id);
                        // 404 Not Found yanıtı döndür
                        return ResponseEntity.notFound().build();
                    });
        } catch (UserNotAuthorizedException e) {
            // Yetkilendirme hatası durumunda 401 Unauthorized döndür
            System.err.println("QuizController: Quiz görüntüleme yetkisi yok - Kullanıcı ID: " + currentUserId + ", Quiz ID: " + id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

 // Yeni Quiz Oluşturma
    // HTTP POST isteği ile "/api/quizzes" adresine yapılan istekleri karşılar
    // (Teacher/Admin yetkisi gerektirir)
    @PostMapping
    @Transactional
    public ResponseEntity<?> createQuiz(@Valid @RequestBody QuizCreateRequest quizCreateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentTeacherId = getCurrentUserId(authentication);
        
        System.out.println("QuizController: Quiz oluşturma isteği - isActive: " + quizCreateRequest.isActive());

        try {
            Quiz createdQuiz = quizService.createQuiz(
                    currentTeacherId,
                    quizCreateRequest.getName(),
                    quizCreateRequest.getDescription(),
                    quizCreateRequest.getDurationMinutes(),
                    quizCreateRequest.isActive(), // Explicitly pass the isActive flag
                    quizCreateRequest.getTopic() // Pass the topic field
            );
            
            System.out.println("QuizController: Quiz oluşturuldu - ID: " + createdQuiz.getId() + ", isActive: " + createdQuiz.isActive());

            // Log the response before sending
            QuizResponse response = new QuizResponse(createdQuiz);
            System.out.println("QuizController: Yanıt oluşturuldu - isActive: " + response.isActive());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UserNotAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Quiz creation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
 // Mevcut Quiz Güncelleme
    // HTTP PUT isteği ile "/api/quizzes/{id}" adresine yapılan istekleri karşılar
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @PutMapping("/{id}")
    // Dönüş tipi ResponseEntity<QuizResponse> olarak güncellendi
    public ResponseEntity<QuizResponse> updateQuiz(@PathVariable("id") int id, @Valid @RequestBody QuizUpdateRequest quizUpdateRequest) {
        System.out.println("QuizController: Quiz güncelleniyor - ID: " + id);
        // @Valid: DTO validasyonunu etkinleştirir
        // @RequestBody: İstek gövdesindeki JSON'u DTO nesnesine bağlar

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUpdaterId = getCurrentUserId(authentication); // Yardımcı metot kullanıldı

        // Service katmanındaki updateQuiz metodunu çağır
        // Debug mesajları ekleyerek isActive değerini ve topic değerlerini kontrol et
        Boolean isActive = quizUpdateRequest.isActive(); // Boolean getter için isXxx() kullanılıyor
        String topic = quizUpdateRequest.getTopic(); // Topic fieldını al
        System.out.println("QuizController: isActive değeri = " + isActive);
        System.out.println("QuizController: topic değeri = " + topic);
        
        Quiz updatedQuiz = quizService.updateQuiz(
                id, // Güncellenecek quiz ID'si
                quizUpdateRequest.getName(),
                quizUpdateRequest.getDescription(),
                quizUpdateRequest.getDurationMinutes(),
                isActive, // isActive() metodunu kullanıyoruz
                topic,    // Topic bilgisini gönderiyoruz
                currentUpdaterId // Güncelleme yapan kullanıcı ID'si
        );

        // Güncellenen quizi DTO'ya dönüştür
        QuizResponse quizResponse = new QuizResponse(updatedQuiz);

        System.out.println("QuizController: Quiz başarıyla güncellendi - ID: " + updatedQuiz.getId());
        // Başarılı durumda 200 OK yanıtı ve DTO'yu döndür
        return ResponseEntity.ok(quizResponse);
    }

 // Quiz Silme
    // HTTP DELETE isteği ile "/api/quizzes/{id}" adresine yapılan istekleri karşılar
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @DeleteMapping("/{id}")
    // Dönüş tipi ResponseEntity<Void> olarak ayarlandı (yanıt gövdesi boş olacak)
    public ResponseEntity<Void> deleteQuiz(@PathVariable("id") int id) {
        System.out.println("QuizController: Quiz siliniyor - ID: " + id);

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentDeleterId = getCurrentUserId(authentication); // Yardımcı metot kullanıldı

        // Service katmanındaki deleteQuiz metodunu çağır (silen kullanıcı ID'si ile birlikte)
        // Service void döner.
       // quizService.deleteQuiz(id, currentDeleterId);
       quizService.deactivateQuiz(id, currentDeleterId);
        System.out.println("QuizController: Quiz başarıyla silindi - ID: " + id);
        // Başarılı durumda 204 No Content yanıtı döndür
        return ResponseEntity.noContent().build(); // 204 No Content
    }

     // --- Soru Yönetimi Endpoint'leri (Quiz'e bağlı, genellikle Teacher/Admin yetkisi gerektirir) ---
    // URL : /api/quizzes/{quizId}/questions

 // Bir Quize ait tüm soruları getirme
    // HTTP GET isteği ile "/api/quizzes/{quizId}/questions" adresine yapılan istekleri karşılar
    // Kullanıcı rolüne göre farklı DTO döner:
    // - Öğretmen/Admin için TeacherQuestionResponse (isCorrect bilgisiyle)
    // - Öğrenciler için QuestionResponse (isCorrect bilgisi olmadan)
    @GetMapping("/{quizId}/questions") 
    public ResponseEntity<?> getQuestionsByQuiz(@PathVariable("quizId") int quizId) {
        System.out.println("QuizController: Quize ait sorular getiriliyor - Quiz ID: " + quizId);

        // Kullanıcı kimliğini ve bilgilerini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentViewerId = getCurrentUserId(authentication);
        
        // Service katmanındaki getQuestionsByQuiz metodunu çağır
        List<Question> questions = quizService.getQuestionsByQuiz(quizId, currentViewerId);
        
        // Kullanıcı rolünü kontrol et
        String userRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");
        
        boolean isTeacherOrAdmin = userRole.equals("ROLE_TEACHER") || userRole.equals("ROLE_ADMIN");
        
        if (isTeacherOrAdmin) {
            // Öğretmen/Admin için TeacherQuestionResponse kullan (isCorrect bilgisiyle)
            List<TeacherQuestionResponse> teacherResponses = questions.stream()
                    .map(TeacherQuestionResponse::new)
                    .collect(Collectors.toList());
            
            System.out.println("QuizController: " + questions.size() + " soru bulundu. Öğretmen görünümü dönülüyor.");
            return ResponseEntity.ok(teacherResponses);
        } else {
            // Öğrenciler için QuestionResponse kullan (isCorrect bilgisi olmadan)
            List<QuestionResponse> studentResponses = questions.stream()
                    .map(QuestionResponse::new)
                    .collect(Collectors.toList());
            
            System.out.println("QuizController: " + questions.size() + " soru bulundu. Öğrenci görünümü dönülüyor.");
            return ResponseEntity.ok(studentResponses);
        }
    }

 // Bir Quize Yeni Soru Ekleme
    // HTTP POST isteği ile "/api/quizzes/{quizId}/questions" adresine yapılan istekleri karşılar
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @PostMapping("/{quizId}/questions") // Metot tanımlaması düzeltildi
    // Dönüş tipi ResponseEntity<QuestionResponse> olarak güncellendi
    public ResponseEntity<QuestionResponse> addQuestionToQuiz(
            @PathVariable("quizId") int quizId,
            @Valid @RequestBody QuestionCreateRequest questionCreateRequest) {

        System.out.println("QuizController: Quize soru ekleniyor - Quiz ID: " + quizId);

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentCreatorId = getCurrentUserId(authentication); // Yardımcı metot kullanıldı

        // Option listesini hazırla
        List<Option> optionEntities = null; // Default null, service içinde handle edilecek
        if (questionCreateRequest.getOptions() != null) {
            optionEntities = questionCreateRequest.getOptions().stream()
                    .map(optionRequest -> {
                        Option option = new Option();
                        option.setText(optionRequest.getText());
                        option.setCorrect(optionRequest.isCorrect());
                        return option;
                    })
                    .collect(Collectors.toList());
            System.out.println("QuizController: " + optionEntities.size() + " option oluşturuldu.");
        }

        // Debug bilgisi
        System.out.println("QuizController: Soru puanı: " + questionCreateRequest.getPoints());
        
        // Service katmanındaki addQuestionToQuiz metodunu çağır
        Question addedQuestion = quizService.addQuestionToQuiz(
                quizId,
                questionCreateRequest.getNumber(),
                questionCreateRequest.getQuestionSentence(),
                questionCreateRequest.getCorrectAnswerText(),
                questionCreateRequest.getQuestionTypeId(),
                questionCreateRequest.getPoints(), // Puan bilgisini gönderiyoruz
                optionEntities,
                currentCreatorId
        );

        // Eklenen soruyu DTO'ya dönüştür
        QuestionResponse questionResponse = new QuestionResponse(addedQuestion);

        System.out.println("QuizController: Soru başarıyla eklendi - ID: " + addedQuestion.getId());
        // Başarılı durumda 201 Created yanıtı ve DTO'yu döndür
        return ResponseEntity.status(HttpStatus.CREATED).body(questionResponse);
    }

 // Quizdeki Soruyu Güncelleme
    // HTTP PUT isteği ile "/api/quizzes/{quizId}/questions/{questionId}" adresine yapılan istekleri karşılar
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @PutMapping("/{quizId}/questions/{questionId}")
    // Dönüş tipi ResponseEntity<QuestionResponse> olarak güncellendi
    public ResponseEntity<QuestionResponse> updateQuestionInQuiz(@PathVariable("quizId") int quizId,
                                                                 @PathVariable("questionId") int questionId,
                                                                 @Valid @RequestBody QuestionUpdateRequest questionUpdateRequest) {
        System.out.println("QuizController: Quizdeki soru güncelleniyor - Quiz ID: " + quizId + ", Soru ID: " + questionId);

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUpdaterId = getCurrentUserId(authentication); // Yardımcı metot kullanıldı

        // QuestionUpdateRequest DTO'sundaki OptionUpdateRequest listesini Option Entity listesine dönüştür
        List<Option> optionEntities = null; // Default null, service içinde handle edilecek
        if (questionUpdateRequest.getOptions() != null) {
            optionEntities = questionUpdateRequest.getOptions().stream()
                    .map(optionRequest -> {
                        // OptionUpdateRequest'ten Option Entity oluştur
                        Option option = new Option();
                        // ID'yi set et (güncelleme senaryosu için önemli)
                        if (optionRequest.getId() != null) {
                            option.setId(optionRequest.getId()); // ID'yi set et
                        }
                        option.setText(optionRequest.getText());
                        
                        // Debug mesajı ekle
                        System.out.println("Seçenek doğru mu: " + optionRequest.isCorrect());
                        option.setCorrect(optionRequest.isCorrect());
                        // Question ilişkisi service katmanında kurulacak
                        return option;
                    })
                    .collect(Collectors.toList());
            System.out.println("QuizController: " + optionEntities.size() + " option entity oluşturuldu.");
        }


        // Debug bilgisi
        System.out.println("QuizController: Soru puanı: " + questionUpdateRequest.getPoints());
        
        // Question ve Option kodunu daha düzgün bir şekilde işliyoruz
        // Points bilgisini logta görüntüle
        System.out.println("QuizController: Points değeri: " + questionUpdateRequest.getPoints());
        
        // GEÇİCİ ÇÖZÜM: QuizService.java'da points parametresi eklenmemiş görünüyor
        // Daha sonra QuizService'i düzgence güncelleyip points parametresini dahil etmek gerekecek
        // Şu anlık optionEntities içinde points bilgisi taşıyabiliriz
        System.out.println("QuizController: Güncellemek istediğimiz points değeri: " + questionUpdateRequest.getPoints());

        // Eğer points değeri varsa, doğrudan veritabanında güncelleyelim
        int pointsValue = questionUpdateRequest.getPoints();
        
        if (pointsValue > 0) {
            try {
                // Mevcut soruyu repository'den bulup, points değerini güncelleyelim
                Question existingQuestion = questionRepository.findById(questionId).orElse(null);
                if (existingQuestion != null) {
                    existingQuestion.setPoints(pointsValue);
                    questionRepository.save(existingQuestion);
                    System.out.println("QuizController: Soru puanı doğrudan veritabanında güncellendi: " + pointsValue);
                }
                
                // Ayrıca Option nesnelerine de ekleyelim
                if (optionEntities != null && !optionEntities.isEmpty()) {
                    for (Option option : optionEntities) {
                        if (option.getQuestion() == null) {
                            Question tempQuestion = new Question();
                            tempQuestion.setPoints(pointsValue);
                            option.setQuestion(tempQuestion);
                        } else {
                            option.getQuestion().setPoints(pointsValue);
                        }
                    }
                    System.out.println("QuizController: Points değeri option nesnelerine de eklendi: " + pointsValue);
                }
            } catch (Exception e) {
                System.err.println("QuizController: Points işlenirken hata: " + e.getMessage());
                // Hatayı logluyoruz ama işlemi durdurmuyoruz
            }
        } else {
            System.out.println("QuizController: Points değeri 0 veya negatif olduğu için güncellenmedi");
        }

        // Service katmanındaki mevcut updateQuestionInQuiz metodunu çağır
        Question updatedQuestion = quizService.updateQuestionInQuiz(
                quizId, 
                questionId, 
                questionUpdateRequest.getNumber(),
                questionUpdateRequest.getQuestionSentence(),
                questionUpdateRequest.getCorrectAnswerText(),
                questionUpdateRequest.getQuestionTypeId(),
                pointsValue, // points parametresini ekliyoruz
                optionEntities,
                currentUpdaterId
        );

        // Güncellenen soruyu DTO'ya dönüştür
        QuestionResponse questionResponse = new QuestionResponse(updatedQuestion);


        System.out.println("QuizController: Soru başarıyla güncellendi - ID: " + updatedQuestion.getId());
        // Başarılı durumda 200 OK yanıtı ve DTO'yu döndür
        return ResponseEntity.ok(questionResponse);
    }

 // Quizden Soru Silme
    // HTTP DELETE isteği ile "/api/quizzes/{quizId}/questions/{questionId}" adresine yapılan istekleri karşılar
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @DeleteMapping("/{quizId}/questions/{questionId}")
    // Dönüş tipi ResponseEntity<Void> olarak ayarlandı
    public ResponseEntity<Void> removeQuestionFromQuiz(@PathVariable("quizId") int quizId,
                                                       @PathVariable("questionId") int questionId) {
        System.out.println("QuizController: Quizden soru siliniyor - Quiz ID: " + quizId + ", Soru ID: " + questionId);

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentRemoverId = getCurrentUserId(authentication); // Yardımcı metot kullanıldı

        // Service katmanındaki removeQuestionFromQuiz metodunu çağır
        quizService.removeQuestionFromQuiz(quizId, questionId, currentRemoverId);

        System.out.println("QuizController: Soru başarıyla silindi - Quiz ID: " + quizId + ", Soru ID: " + questionId);
        // Başarılı durumda 204 No Content yanıtı döndür
        return ResponseEntity.noContent().build(); // 204 No Content
    }



 // --- Şık (Option) Yönetimi Endpoint'leri (Soruya bağlı, genellikle Teacher/Admin yetkisi gerektirir) ---
    // URL : /api/quizzes/{quizId}/questions/{questionId}/options
    // Şık yönetimi genellikle soru yönetimi metotları içinde yapılabilir (QuestionCreate/UpdateRequest DTO'larına şıkları dahil ederek)
    // Ama ayrı endpointler de tanımlanabilir:

    // Bir Soruya Şık Ekleme
    @PostMapping("/{quizId}/questions/{questionId}/options")
    // Dönüş tipi ResponseEntity<OptionResponse> olarak güncellendi
    // @PathVariable("quizId") int quizId, // Quiz ID'si bu endpoint için zorunlu değil, sadece soru ID'si yeterli olabilir.
    // Ancak URL yapısı gereği tutulabilir. Servis metoduna sadece questionId geçmek yeterli olacaktır.
    public ResponseEntity<OptionResponse> addOptionToQuestion(@PathVariable("quizId") int quizId, // URL yapısı için tutuldu
                                                              @PathVariable("questionId") int questionId,
                                                              @Valid @RequestBody OptionCreateRequest optionCreateRequest) {
        System.out.println("QuizController: Soruya şık ekleniyor - Soru ID: " + questionId);

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentAdderId = getCurrentUserId(authentication); // Yardımcı metot kullanıldı

        // Service katmanındaki addOptionToQuestion metodunu çağır
        // Service'in Option döndürdüğü varsayılıyor
        Option addedOption = quizService.addOptionToQuestion(
                questionId, // Şık eklenecek soru ID'si
                optionCreateRequest.getText(), // Şık metni
                optionCreateRequest.isCorrect(), // Şık doğru cevap mı?
                currentAdderId // Şık ekleyen kullanıcı ID'si
        );

        // Eklenen şıkkı DTO'ya dönüştür
        OptionResponse optionResponse = new OptionResponse(addedOption);


        System.out.println("QuizController: Şık başarıyla eklendi - ID: " + addedOption.getId());
        // Başarılı durumda 201 Created yanıtı ve DTO'yu döndür
        return ResponseEntity.status(HttpStatus.CREATED).body(optionResponse);
    }

    // Bir Şıkkı Güncelleme
    @PutMapping("/{quizId}/questions/{questionId}/options/{optionId}")
    // Dönüş tipi ResponseEntity<OptionResponse> olarak güncellendi
    // @PathVariable("quizId") int quizId, // Quiz ID'si bu endpoint için zorunlu değil, sadece şık ID'si yeterli olabilir.
    // @PathVariable("questionId") int questionId, // Soru ID'si bu endpoint için zorunlu değil, sadece şık ID'si yeterli olabilir.
    // Ancak URL yapısı gereği tutulabilir. Servis metoduna sadece optionId geçmek yeterli olacaktır.
    public ResponseEntity<OptionResponse> updateOption(@PathVariable("quizId") int quizId, // URL yapısı için tutuldu
                                                       @PathVariable("questionId") int questionId, // URL yapısı için tutuldu
                                                       @PathVariable("optionId") int optionId,
                                                       @Valid @RequestBody OptionUpdateRequest optionUpdateRequest) {
        System.out.println("QuizController: Şık güncelleniyor - Şık ID: " + optionId);

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUpdaterId = getCurrentUserId(authentication); // Yardımcı metot kullanıldı

        // Service katmanındaki updateOption metodunu çağır
        // Service'in Option döndürdüğü varsayılıyor
        Option updatedOption = quizService.updateOption(
                optionId, // Güncellenecek şık ID'si
                optionUpdateRequest.getText(), // Güncellenmiş şık metni
                optionUpdateRequest.isCorrect(), // Güncellenmiş doğru cevap bilgisi
                currentUpdaterId // Güncelleme yapan kullanıcı ID'si
        );

        // Güncellenen şıkkı DTO'ya dönüştür
        OptionResponse optionResponse = new OptionResponse(updatedOption);


        System.out.println("QuizController: Şık başarıyla güncellendi - ID: " + updatedOption.getId());
        // Başarılı durumda 200 OK yanıtı ve DTO'yu döndür
        return ResponseEntity.ok(optionResponse);
    }

    // Bir Şıkkı Silme
    @DeleteMapping("/{quizId}/questions/{questionId}/options/{optionId}")
    // Dönüş tipi ResponseEntity<Void> olarak ayarlandı
    // @PathVariable("quizId") int quizId, // URL yapısı için tutuldu
    // @PathVariable("questionId") int questionId, // URL yapısı için tutuldu
    // Ancak URL yapısı gereği tutulabilir. Servis metoduna sadece optionId geçmek yeterli olacaktır.
    public ResponseEntity<Void> deleteOption(@PathVariable("quizId") int quizId, // URL yapısı için tutuldu
                                             @PathVariable("questionId") int questionId, // URL yapısı için tutuldu
                                             @PathVariable("optionId") int optionId) {
        System.out.println("QuizController: Şık siliniyor - Şık ID: " + optionId);

        // Güncel kullanıcı kimliğini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentDeleterId = getCurrentUserId(authentication); // Yardımcı metot kullanıldı

        // Service katmanındaki deleteOption metodunu çağır (silen kullanıcı ID'si ile birlikte)
        // Service void döner.
        quizService.deleteOption(optionId, currentDeleterId);

        System.out.println("QuizController: Şık başarıyla silindi - Şık ID: " + optionId);
        // Başarılı durumda 204 No Content yanıtı döndür
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    // --- Diğer Endpoint'ler ---
    // İstatistikler (showQuizStatistics, showQuizAnswers) StatisticsController'da olacak.
    // AI ile etkileşim (askAiToAnswerQuiz) bu Controller'da veya ayrı bir AIService/Controller'da olabilir.
    
    /**
     * Bir öğretmenin tüm quizlerini aktifleştirir
     */
    @PostMapping("/teacher/{teacherId}/activate-all")
    public ResponseEntity<?> activateAllQuizzesForTeacher(@PathVariable int teacherId) {
        System.out.println("QuizController: Activating all quizzes for teacher - ID: " + teacherId);
        
        try {
            // Get current user for authorization
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            int currentUserId = getCurrentUserId(authentication);
            
            // Yetkilendirme kontrolü
            boolean isTeacher = teacherId == currentUserId;
            boolean isAdmin = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .map(auth -> "ROLE_ADMIN".equals(auth))
                    .orElse(false);
            
            if (!isTeacher && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to activate quizzes for this teacher");
            }
            
            // Tüm quizleri aktifleştir ve aktifleştirilen quiz sayısını al
            int activatedCount = quizService.activateAllQuizzesForTeacher(teacherId);
            
            System.out.println("QuizController: " + activatedCount + " quizzes activated successfully for teacher ID: " + teacherId);
            
            return ResponseEntity.ok(Map.of(
                "message", activatedCount + " quizzes activated successfully",
                "teacherId", teacherId,
                "activatedCount", activatedCount
            ));
            
        } catch (Exception e) {
            System.err.println("QuizController: Error activating quizzes - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating quizzes: " + e.getMessage());
        }
    }
}
