package com.example.QuizSystemProject.Controller;// Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.dto.*; // İleride oluşturulacak tüm ilgili DTO'ları import edin (veya tek tek)
import com.example.QuizSystemProject.Model.Quiz; // Quiz Entity'sini döndürmek için (DTO kullanmak daha iyi olabilir)
import com.example.QuizSystemProject.Model.Question; // Question Entity'sini döndürmek için (DTO kullanmak daha iyi olabilir)
import com.example.QuizSystemProject.Model.Option; // Option Entity'sini döndürmek için (DTO kullanmak daha iyi olabilir)
import com.example.QuizSystemProject.Service.QuizService; // QuizService'i import edin
import jakarta.validation.Valid; // Girdi doğrulama için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.web.bind.annotation.*; // Web anotasyonları için (@RestController, @RequestMapping, @PostMapping vb.)
import org.springframework.security.core.context.SecurityContextHolder; // <--- YENİ EKLENECEK
import org.springframework.security.core.Authentication; // <--- YENİ EKLENECEK
import org.springframework.security.core.userdetails.UserDetails; // <--- YENİ EKLENECEK
import com.example.QuizSystemProject.dto.QuizCreateRequest; // <--- Eklendiğinden emin olun
import com.example.QuizSystemProject.dto.QuizResponse; // <--- Eklendiğinden emin olun
import com.example.QuizSystemProject.dto.OptionCreateRequest; // <--- YENİ EKLENECEK (Eğer OptionCreateRequest varsa)
import com.example.QuizSystemProject.dto.QuestionUpdateRequest;
import java.util.List; // List importu
import java.util.Optional; // Optional importu
import java.util.stream.Collectors;

@RestController // Bu sınıfın bir REST Controller olduğunu belirtir
@RequestMapping("/api/quizzes") // Bu controller altındaki tüm endpoint'lerin "/api/quizzes" ile başlayacağını belirtir
public class QuizController {

    private final QuizService quizService; // Quiz iş mantığı servisi

    // QuizService bağımlılığının enjekte edildiği constructor
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

 // --- Quiz Yönetimi Endpoint'leri (Genellikle Teacher/Admin yetkisi gerektirir) ---

    // Tüm Quizleri Getirme (Opsiyonel olarak filtrelenebilir, herkes görebilir veya yetkiye göre değişir)
    // HTTP GET isteği ile "/api/quizzes" adresine yapılan istekleri karşılar
    @GetMapping
    // Dönüş tipi ResponseEntity<List<QuizResponse>> olarak güncellendi
    public ResponseEntity<List<QuizResponse>> getAllQuizzes() {
        System.out.println("QuizController: Tüm quizler listeleniyor.");

        // try-catch bloğu yok, hatalar GlobalExceptionHandler'a gidecek.

        // Service katmanındaki tüm quizleri getiren metodu çağır
        // Service metodu List<Quiz> döndürür.
        List<Quiz> quizzes = quizService.getAllQuizzes();

        // Entity listesini DTO listesine dönüştür
        List<QuizResponse> quizResponses = quizzes.stream()
                                                 .map(QuizResponse::new) // Her Quiz Entity'sini QuizResponse DTO'suna dönüştür
                                                 .collect(Collectors.toList()); // Sonucu List olarak topla


        System.out.println("QuizController: " + quizResponses.size() + " quiz DTO'su oluşturuldu.");
        // Başarılı durumda 200 OK yanıtı ve DTO listesini döndür
        return ResponseEntity.ok(quizResponses);
    }

 // ID'ye Göre Quiz Getirme
    // HTTP GET isteği ile "/api/quizzes/{id}" adresine yapılan istekleri karşılar
    @GetMapping("/{id}")
    // Dönüş tipi ResponseEntity<QuizDetailsResponse> olarak güncellendi
    public ResponseEntity<QuizDetailsResponse> getQuizById(@PathVariable("id") int id) {
    
    
    System.out.println("QuizController: Quiz detayları getiriliyor - ID: " + id);
    
    // try-catch bloğu yok, service'ten fırlatılan diğer hatalar GlobalExceptionHandler'a gidecek.
    // Ancak Service'in Optional<Quiz> dönmesi durumunda "bulunamadı" senaryosunu burada handle ediyoruz.
    
    // Service'ten quizi ID'ye göre çekme
    Optional<Quiz> quizOptional = quizService.getQuizById(id);
    
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
    }

 // Yeni Quiz Oluşturma
    // HTTP POST isteği ile "/api/quizzes" adresine yapılan istekleri karşılar
    // (Teacher/Admin yetkisi gerektirir)
    @PostMapping
    // Dönüş tipi ResponseEntity<QuizResponse> olarak güncellendi
    public ResponseEntity<QuizResponse> createQuiz(@Valid @RequestBody QuizCreateRequest quizCreateRequest) {
        int currentTeacherId;
        System.out.println("QuizController: Yeni quiz oluşturuluyor - Ad: " + quizCreateRequest.getName());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            System.err.println("QuizController: User not authenticated or anonymous for quiz creation.");
            // Consider throwing UserNotAuthorizedException or returning ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); 
        }
        
        Object principal = authentication.getPrincipal();
    
        if (principal instanceof UserDetails) {
            try {
                 currentTeacherId = Integer.parseInt(((UserDetails) principal).getUsername());
            } catch (NumberFormatException e) {
                 System.err.println("QuizController: Principal username is not a valid integer ID during quiz creation: " + ((UserDetails) principal).getUsername());
                 // For a production app, return a proper error DTO instead of null or throwing an unhandled IllegalStateException
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
            }
        } else {
             System.err.println("QuizController: Principal is not an instance of UserDetails during quiz creation. Actual type: " + (principal != null ? principal.getClass().getName() : "null"));
             // For a production app, return a proper error DTO
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); 
        }

        // Proceed with quiz creation if currentTeacherId was successfully obtained
        Quiz createdQuiz = quizService.createQuiz(
            currentTeacherId,
            quizCreateRequest.getName(),
            quizCreateRequest.getDescription(),
            quizCreateRequest.getDurationMinutes()
        );

        QuizResponse quizResponse = new QuizResponse(createdQuiz);
        System.out.println("QuizController: Quiz başarıyla oluşturuldu - ID: " + createdQuiz.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(quizResponse);
    }
 // Mevcut Quiz Güncelleme
    // HTTP PUT 
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @PutMapping("/{id}")
    // 
    public ResponseEntity<QuizResponse> updateQuiz(@PathVariable("id") int id, @Valid @RequestBody QuizUpdateRequest quizUpdateRequest) {
        System.out.println("QuizController:  - ID: " + id);
        // @Valid: DTO 
        // @RequestBody: 

        // 

        // 
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    int currentUpdaterId;
    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails) {
        try {
             currentUpdaterId = Integer.parseInt(((UserDetails) principal).getUsername());
        } catch (NumberFormatException e) {
             System.err.println("QuizController: Principal username , ID .");
             throw new IllegalStateException(" .");
        }
    } else {
         System.err.println("QuizController: Principal , " + principal.getClass().getName());
         throw new IllegalStateException(" .");
    }

    // 
    Quiz updatedQuiz = quizService.updateQuiz(
        id, // 
        quizUpdateRequest.getName(),
        quizUpdateRequest.getDescription(),
        quizUpdateRequest.getDurationMinutes(),
        quizUpdateRequest.isActive(),
        currentUpdaterId // 
    );

    // 
    QuizResponse quizResponse = new QuizResponse(updatedQuiz);

    System.out.println("QuizController:  - ID: " + updatedQuiz.getId());
    // 
    return ResponseEntity.ok(quizResponse);
}

 // Quiz Silme
    // HTTP DELETE 
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable("id") int id) {
    // ResponseEntity<Void>: . 

    System.out.println("QuizController:  - ID: " + id);
    
            // 

    // 
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            int currentDeleterId;
            Object principal = authentication.getPrincipal();
    
            if (principal instanceof UserDetails) {
                try {
                     currentDeleterId = Integer.parseInt(((UserDetails) principal).getUsername());
                } catch (NumberFormatException e) {
                     System.err.println("QuizController: Principal username , ID .");
                     throw new IllegalStateException(" .");
                }
            } else {
                 System.err.println("QuizController: Principal , " + principal.getClass().getName());
                 throw new IllegalStateException(" .");
            }
    
    
    // 
    quizService.deleteQuiz(id, currentDeleterId);
    
    System.out.println("QuizController:  - ID: " + id);
    // 
    return ResponseEntity.noContent().build(); // 204 No Content
}


    // --- Soru Yönetimi Endpoint'leri (Quiz'e bağlı, genellikle Teacher/Admin yetkisi gerektirir) ---
    // URL : /api/quizzes/{quizId}/questions

 // Bir Quize ait tüm soruları getirme
    // HTTP GET 
    @GetMapping("/{quizId}/questions")
    // 
    public ResponseEntity<List<QuestionResponse>> getQuestionsByQuiz(@PathVariable("quizId") int quizId) {
    System.out.println("QuizController:  - Quiz ID: " + quizId);
    
    // 

    // 
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    int currentViewerId;
    Object principal = authentication.getPrincipal();
    
              if (principal instanceof UserDetails) {
                  try {
                       currentViewerId = Integer.parseInt(((UserDetails) principal).getUsername());
                  } catch (NumberFormatException e) {
                       System.err.println("QuizController: Principal username , ID .");
                       throw new IllegalStateException(" .");
                  }
              } else {
                   System.err.println("QuizController: Principal , " + principal.getClass().getName());
                   throw new IllegalStateException(" .");
              }
    
    // 
    List<Question> questions = quizService.getQuestionsByQuiz(quizId, currentViewerId);
    
    // 
    // 
    List<QuestionResponse> questionResponses = questions.stream()
                                                              .map(QuestionResponse::new) // 
                                                              // 
                                                              .sorted((q1, q2) -> Integer.compare(q1.getNumber(), q2.getNumber()))
                                                              .collect(Collectors.toList()); // 


    System.out.println("QuizController: Quiz ID " + quizId + " " + questionResponses.size() + " ");
    // 
    return ResponseEntity.ok(questionResponses);
    }

 // Quize Yeni Soru Ekleme
    // HTTP POST 
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @PostMapping("/{quizId}/questions")
    // 
    public ResponseEntity<QuestionResponse> addQuestionToQuiz(@PathVariable("quizId") int quizId,
    @Valid @RequestBody QuestionCreateRequest questionCreateRequest) {
    System.out.println("QuizController:  - Quiz ID: " + quizId);
    
    // 

    // 
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    int currentAdderId;
    Object principal = authentication.getPrincipal();
    
    if (principal instanceof UserDetails) {
                try {
                     currentAdderId = Integer.parseInt(((UserDetails) principal).getUsername());
                } catch (NumberFormatException e) {
                     System.err.println("QuizController: Principal username could not be parsed as ID.");
                     throw new IllegalStateException("Invalid user ID format.");
                }
            } else if (principal instanceof Integer) {
                 currentAdderId = (Integer) principal;
            } else {
                 System.err.println("QuizController: Unexpected principal type: " + principal.getClass().getName());
                 throw new IllegalStateException("Invalid authentication principal type.");
            }
    
            // 
            List<Option> optionEntities = List.of(); // 
            if (questionCreateRequest.getOptions() != null && !questionCreateRequest.getOptions().isEmpty()) {
                 optionEntities = questionCreateRequest.getOptions().stream()
                                                    .map(optionRequest -> {
                                                        // 
                                                        Option option = new Option();
                                                        option.setText(optionRequest.getText());
                                                        option.setCorrect(optionRequest.isCorrect());
                                                        // 
                                                        return option;
                                                    })
                                                    .collect(Collectors.toList());
                 System.out.println("QuizController: " + optionEntities.size() + " ");
            }
    
    
    // 
    Question addedQuestion = quizService.addQuestionToQuiz(
    quizId,
    questionCreateRequest.getNumber(),
    questionCreateRequest.getQuestionSentence(),
    questionCreateRequest.getCorrectAnswerText(),
    questionCreateRequest.getQuestionTypeId(),
    optionEntities,
    currentAdderId
    );
    
            // 
            QuestionResponse questionResponse = new QuestionResponse(addedQuestion);
    
    
    System.out.println("QuizController:  - ID: " + addedQuestion.getId());
    // 
    return ResponseEntity.status(HttpStatus.CREATED).body(questionResponse);
    }

 // Quizdeki Soruyu Güncelleme
    // HTTP PUT 
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @PutMapping("/{quizId}/questions/{questionId}")
    // 
    public ResponseEntity<QuestionResponse> updateQuestionInQuiz(@PathVariable("quizId") int quizId,
    @PathVariable("questionId") int questionId,
    @Valid @RequestBody QuestionUpdateRequest questionUpdateRequest) {
    System.out.println("QuizController:  - Quiz ID: " + quizId + ", Soru ID: " + questionId);
    
    // 

    // 
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    int currentUpdaterId;
    Object principal = authentication.getPrincipal();
    
              if (principal instanceof UserDetails) {
                  try {
                       currentUpdaterId = Integer.parseInt(((UserDetails) principal).getUsername());
                  } catch (NumberFormatException e) {
                       System.err.println("QuizController: Principal username , ID .");
                       throw new IllegalStateException(" .");
                  }
              } else {
                   System.err.println("QuizController: Principal , " + principal.getClass().getName());
                   throw new IllegalStateException(" .");
              }
    
             // 
             // 
             List<Option> optionEntities = List.of(); // 
             if (questionUpdateRequest.getOptions() != null) { // 
                  optionEntities = questionUpdateRequest.getOptions().stream()
                                                    .map(optionRequest -> {
                                                        // 
                                                        Option option = new Option();
                                                        option.setText(optionRequest.getText());
                                                        option.setCorrect(optionRequest.isCorrect());
                                                        // 
                                                        return option;
                                                    })
                                                    .collect(Collectors.toList());
                  System.out.println("QuizController: " + optionEntities.size() + " ");
             }
    
    
    // 
    Question updatedQuestion = quizService.updateQuestionInQuiz(
    quizId, // 
    questionId, // 
    // 
    questionUpdateRequest.getNumber(),
    questionUpdateRequest.getQuestionSentence(),
    questionUpdateRequest.getCorrectAnswerText(),
    questionUpdateRequest.getQuestionTypeId(),
    optionEntities, // 
    currentUpdaterId // 
    );
    
            // 
            QuestionResponse questionResponse = new QuestionResponse(updatedQuestion);
    
    
    System.out.println("QuizController:  - ID: " + updatedQuestion.getId());
    // 
    return ResponseEntity.ok(questionResponse);
    }

 // Quizden Soru Silme
    // HTTP DELETE 
    // (Quiz'in öğretmeni veya Admin yetkisi gerektirir)
    @DeleteMapping("/{quizId}/questions/{questionId}")
    // 
    public ResponseEntity<Void> removeQuestionFromQuiz(@PathVariable("quizId") int quizId,
    @PathVariable("questionId") int questionId) {
    System.out.println("QuizController:  - Quiz ID: " + quizId + ", Soru ID: " + questionId);
    
    // 

    // 
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    int currentRemoverId;
    Object principal = authentication.getPrincipal();
    
              if (principal instanceof UserDetails) {
                  try {
                       currentRemoverId = Integer.parseInt(((UserDetails) principal).getUsername());
                  } catch (NumberFormatException e) {
                       System.err.println("QuizController: Principal username , ID .");
                       throw new IllegalStateException(" .");
                  }
              } else {
                   System.err.println("QuizController: Principal , " + principal.getClass().getName());
                   throw new IllegalStateException(" .");
              }
    
    // 
    quizService.removeQuestionFromQuiz(quizId, questionId, currentRemoverId);
    
    System.out.println("QuizController:  - Quiz ID: " + quizId + ", Soru ID: " + questionId);
    // 
    return ResponseEntity.noContent().build(); // 204 No Content
}



 // --- Şık (Option) Yönetimi Endpoint'leri (Soruya bağlı, genellikle Teacher/Admin yetkisi gerektirir) ---
    // URL : /api/quizzes/{quizId}/questions/{questionId}/options
    // Şık yönetimi genellikle soru yönetimi metotları içinde yapılabilir (QuestionCreate/UpdateRequest DTO'larına şıkları dahil ederek)
    // Ama ayrı endpointler de tanımlanabilir:

 // Bir Soruya Şık Ekleme
    @PostMapping("/{quizId}/questions/{questionId}/options")
    // 
    // 
    public ResponseEntity<OptionResponse> addOptionToQuestion(@PathVariable("quizId") int quizId, // 
    @PathVariable("questionId") int questionId,
    @Valid @RequestBody OptionCreateRequest optionCreateRequest) {
          System.out.println("QuizController:  - Soru ID: " + questionId);

          // 

          // 
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          int currentAdderId;
          Object principal = authentication.getPrincipal();

          if (principal instanceof UserDetails) {
              try {
                   currentAdderId = Integer.parseInt(((UserDetails) principal).getUsername());
              } catch (NumberFormatException e) {
                   System.err.println("QuizController: Principal username , ID .");
                   throw new IllegalStateException(" .");
              }
          } else {
               System.err.println("QuizController: Principal , " + principal.getClass().getName());
               throw new IllegalStateException(" .");
          }


          // 
          Option addedOption = quizService.addOptionToQuestion(
          questionId, // 
          optionCreateRequest.getText(), // 
          optionCreateRequest.isCorrect(), // 
          currentAdderId // 
          );

         // 
         OptionResponse optionResponse = new OptionResponse(addedOption);


          System.out.println("QuizController:  - ID: " + addedOption.getId());
          // 
          return ResponseEntity.status(HttpStatus.CREATED).body(optionResponse);
      }

 // Bir Şıkkı Güncelleme
    @PutMapping("/{quizId}/questions/{questionId}/options/{optionId}")
    // 
    // 
    public ResponseEntity<OptionResponse> updateOption(@PathVariable("quizId") int quizId, // 
    @PathVariable("questionId") int questionId, // 
    @PathVariable("optionId") int optionId,
    @Valid @RequestBody OptionUpdateRequest optionUpdateRequest) {
    System.out.println("QuizController:  - Şık ID: " + optionId);

    // 

    // 
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    int currentUpdaterId;
    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails) {
              try {
                   currentUpdaterId = Integer.parseInt(((UserDetails) principal).getUsername());
              } catch (NumberFormatException e) {
                   System.err.println("QuizController: Principal username , ID .");
                   throw new IllegalStateException(" .");
              }
          } else {
               System.err.println("QuizController: Principal , " + principal.getClass().getName());
               throw new IllegalStateException(" .");
          }

          // 
          Option updatedOption = quizService.updateOption(
          optionId, // 
          optionUpdateRequest.getText(), // 
          optionUpdateRequest.isCorrect(), // 
          currentUpdaterId // 
          );

         // 
         OptionResponse optionResponse = new OptionResponse(updatedOption);


          System.out.println("QuizController:  - ID: " + updatedOption.getId());
          // 
          return ResponseEntity.ok(optionResponse);
      }

 // Bir Şıkkı Silme
    @DeleteMapping("/{quizId}/questions/{questionId}/options/{optionId}")
    // 
    // 
    public ResponseEntity<Void> deleteOption(@PathVariable("quizId") int quizId, // 
    @PathVariable("questionId") int questionId, // 
    @PathVariable("optionId") int optionId) {
    System.out.println("QuizController:  - Şık ID: " + optionId);
    
    // 

    // 
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    int currentDeleterId;
    Object principal = authentication.getPrincipal();
    
              if (principal instanceof UserDetails) {
                  try {
                       currentDeleterId = Integer.parseInt(((UserDetails) principal).getUsername());
                  } catch (NumberFormatException e) {
                       System.err.println("QuizController: Principal username , ID .");
                       throw new IllegalStateException(" .");
                  }
              } else {
                   System.err.println("QuizController: Principal , " + principal.getClass().getName());
                   throw new IllegalStateException(" .");
              }
    
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
}