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

    public QuizController(QuizService quizService, QuizRepository quizRepository,
            QuestionRepository questionRepository) {
        this.quizService = quizService;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    private int getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new SecurityException("Principal is not an instance of CustomUserDetails or UserDetails.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        return userDetails.getId();
    }

    @GetMapping
    public ResponseEntity<List<QuizResponse>> getAllQuizzes() {
        System.out.println("QuizController: Tüm quizler listeleniyor.");

        List<Quiz> quizzes = quizService.getAllQuizzes();

        List<QuizResponse> quizResponses = quizzes.stream()
                .map(QuizResponse::new)
                .collect(Collectors.toList());

        System.out.println("QuizController: " + quizResponses.size() + " quiz DTO'su oluşturuldu.");
        return ResponseEntity.ok(quizResponses);
    }

    @GetMapping("/teacher-quizzes")
    public ResponseEntity<List<QuizResponse>> getTeacherQuizzes() {
        System.out.println("QuizController: Öğretmenin quizleri getiriliyor");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentTeacherId = getCurrentUserId(authentication);

        List<Quiz> teacherQuizzes = quizService.getQuizzesByTeacher(currentTeacherId);

        List<QuizResponse> quizResponses = teacherQuizzes.stream()
                .map(QuizResponse::new)
                .collect(Collectors.toList());

        System.out.println("QuizController: " + quizResponses.size() + " quiz DTO'su oluşturuldu.");
        return ResponseEntity.ok(quizResponses);
    }

    @PutMapping("/{quizId}/activate")
    public ResponseEntity<?> activateQuiz(@PathVariable("quizId") int quizId) {
        System.out.println("QuizController: Activating quiz ID: " + quizId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUserId = getCurrentUserId(authentication);

        try {
            Optional<Quiz> quizOpt = quizRepository.findById(quizId);

            if (!quizOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Quiz not found with ID: " + quizId);
            }

            Quiz quiz = quizOpt.get();

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

            quiz.setActive(true);
            quizRepository.save(quiz);

            System.out.println("QuizController: Quiz activated successfully - ID: " + quizId);

            return ResponseEntity.ok(Map.of(
                    "message", "Quiz activated successfully",
                    "quizId", quizId,
                    "isActive", true));

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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUserId = getCurrentUserId(authentication);

        boolean isViewingOwnQuizzes = currentUserId == teacherId;
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("UNKNOWN");
        boolean isAdmin = "ROLE_ADMIN".equals(role);

        System.out.println("QuizController: Kullanıcı kendi quizlerini mi görüntülüyor: " + isViewingOwnQuizzes
                + ", Admin mi: " + isAdmin);

        List<Quiz> teacherQuizzes = quizService.getQuizzesByTeacher(teacherId);

        List<QuizResponse> quizResponses = teacherQuizzes.stream()
                .map(quiz -> {
                    QuizResponse response = new QuizResponse(quiz);
                    int questionCount = quizService.getQuestionCountForQuiz(quiz.getId());
                    response.setQuestionCount(questionCount);
                    return response;
                })
                .collect(Collectors.toList());

        System.out.println(
                "QuizController: Teacher ID " + teacherId + " için " + quizResponses.size() + " quiz bulundu.");
        return ResponseEntity.ok(quizResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDetailsResponse> getQuizById(@PathVariable("id") int id) {

        System.out.println("QuizController: Quiz detayları getiriliyor - ID: " + id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUserId = getCurrentUserId(authentication);
        System.out.println("QuizController: Güncel kullanıcı ID: " + currentUserId);

        try {
            Optional<Quiz> quizOptional = quizService.getQuizById(id, currentUserId);

            return quizOptional.map(quiz -> {
                System.out.println("QuizController: Quiz bulundu - ID: " + id);
                QuizDetailsResponse quizDetailsResponse = new QuizDetailsResponse(quiz);
                return ResponseEntity.ok(quizDetailsResponse);
            })
                    .orElseGet(() -> {
                        System.err.println("QuizController: Quiz bulunamadi - ID: " + id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (UserNotAuthorizedException e) {
            System.err.println("QuizController: Quiz görüntüleme yetkisi yok - Kullanıcı ID: " + currentUserId
                    + ", Quiz ID: " + id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

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
                    quizCreateRequest.isActive(), 
                    quizCreateRequest.getTopic() 
            );

            System.out.println("QuizController: Quiz oluşturuldu - ID: " + createdQuiz.getId() + ", isActive: "
                    + createdQuiz.isActive());

            QuizResponse response = new QuizResponse(createdQuiz);
            System.out.println("QuizController: Yanıt oluşturuldu - isActive: " + response.isActive());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UserNotAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                    "Quiz creation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizResponse> updateQuiz(@PathVariable("id") int id,
            @Valid @RequestBody QuizUpdateRequest quizUpdateRequest) {
        System.out.println("QuizController: Quiz güncelleniyor - ID: " + id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUpdaterId = getCurrentUserId(authentication);

        Boolean isActive = quizUpdateRequest.isActive();
        String topic = quizUpdateRequest.getTopic();
        System.out.println("QuizController: isActive değeri = " + isActive);
        System.out.println("QuizController: topic değeri = " + topic);

        Quiz updatedQuiz = quizService.updateQuiz(
                id,
                quizUpdateRequest.getName(),
                quizUpdateRequest.getDescription(),
                quizUpdateRequest.getDurationMinutes(),
                isActive,
                topic,
                currentUpdaterId);

        QuizResponse quizResponse = new QuizResponse(updatedQuiz);

        System.out.println("QuizController: Quiz başarıyla güncellendi - ID: " + updatedQuiz.getId());
        return ResponseEntity.ok(quizResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable("id") int id) {
        System.out.println("QuizController: Quiz siliniyor - ID: " + id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentDeleterId = getCurrentUserId(authentication); 

        quizService.deactivateQuiz(id, currentDeleterId);
        System.out.println("QuizController: Quiz başarıyla silindi - ID: " + id);
        return ResponseEntity.noContent().build(); 
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<?> getQuestionsByQuiz(@PathVariable("quizId") int quizId) {
        System.out.println("QuizController: Quize ait sorular getiriliyor - Quiz ID: " + quizId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentViewerId = getCurrentUserId(authentication);

        List<Question> questions = quizService.getQuestionsByQuiz(quizId, currentViewerId);

        String userRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        boolean isTeacherOrAdmin = userRole.equals("ROLE_TEACHER") || userRole.equals("ROLE_ADMIN");

        if (isTeacherOrAdmin) {
            List<TeacherQuestionResponse> teacherResponses = questions.stream()
                    .map(TeacherQuestionResponse::new)
                    .collect(Collectors.toList());

            System.out.println("QuizController: " + questions.size() + " soru bulundu. Öğretmen görünümü dönülüyor.");
            return ResponseEntity.ok(teacherResponses);
        } else {
            List<QuestionResponse> studentResponses = questions.stream()
                    .map(QuestionResponse::new)
                    .collect(Collectors.toList());

            System.out.println("QuizController: " + questions.size() + " soru bulundu. Öğrenci görünümü dönülüyor.");
            return ResponseEntity.ok(studentResponses);
        }
    }

    @PostMapping("/{quizId}/questions")
    public ResponseEntity<QuestionResponse> addQuestionToQuiz(
            @PathVariable("quizId") int quizId,
            @Valid @RequestBody QuestionCreateRequest questionCreateRequest) {

        System.out.println("QuizController: Quize soru ekleniyor - Quiz ID: " + quizId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentCreatorId = getCurrentUserId(authentication);

        List<Option> optionEntities = null;
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

        System.out.println("QuizController: Soru puanı: " + questionCreateRequest.getPoints());

        Question addedQuestion = quizService.addQuestionToQuiz(
                quizId,
                questionCreateRequest.getNumber(),
                questionCreateRequest.getQuestionSentence(),
                questionCreateRequest.getCorrectAnswerText(),
                questionCreateRequest.getQuestionTypeId(),
                questionCreateRequest.getPoints(),
                optionEntities,
                currentCreatorId);

        QuestionResponse questionResponse = new QuestionResponse(addedQuestion);

        System.out.println("QuizController: Soru başarıyla eklendi - ID: " + addedQuestion.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(questionResponse);
    }

    @PutMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestionInQuiz(@PathVariable("quizId") int quizId,
            @PathVariable("questionId") int questionId,
            @Valid @RequestBody QuestionUpdateRequest questionUpdateRequest) {
        System.out.println(
                "QuizController: Quizdeki soru güncelleniyor - Quiz ID: " + quizId + ", Soru ID: " + questionId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUpdaterId = getCurrentUserId(authentication);
        List<Option> optionEntities = null;
        if (questionUpdateRequest.getOptions() != null) {
            optionEntities = questionUpdateRequest.getOptions().stream()
                    .map(optionRequest -> {
                        Option option = new Option();
                        if (optionRequest.getId() != null) {
                            option.setId(optionRequest.getId()); 
                        }
                        option.setText(optionRequest.getText());
                        option.setCorrect(optionRequest.isCorrect());
                        return option;
                    })
                    .collect(Collectors.toList());
            System.out.println("QuizController: " + optionEntities.size() + " option entity oluşturuldu.");
        }

        System.out.println("QuizController: Soru puanı: " + questionUpdateRequest.getPoints());

        System.out.println("QuizController: Points değeri: " + questionUpdateRequest.getPoints());

        System.out
                .println("QuizController: Güncellemek istediğimiz points değeri: " + questionUpdateRequest.getPoints());

        int pointsValue = questionUpdateRequest.getPoints();

        if (pointsValue > 0) {
            try {
                Question existingQuestion = questionRepository.findById(questionId).orElse(null);
                if (existingQuestion != null) {
                    existingQuestion.setPoints(pointsValue);
                    questionRepository.save(existingQuestion);
                    System.out.println("QuizController: Soru puanı doğrudan veritabanında güncellendi: " + pointsValue);
                }

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
            }
        } else {
            System.out.println("QuizController: Points değeri 0 veya negatif olduğu için güncellenmedi");
        }

        Question updatedQuestion = quizService.updateQuestionInQuiz(
                quizId,
                questionId,
                questionUpdateRequest.getNumber(),
                questionUpdateRequest.getQuestionSentence(),
                questionUpdateRequest.getCorrectAnswerText(),
                questionUpdateRequest.getQuestionTypeId(),
                pointsValue, 
                optionEntities,
                currentUpdaterId);

        QuestionResponse questionResponse = new QuestionResponse(updatedQuestion);

        System.out.println("QuizController: Soru başarıyla güncellendi - ID: " + updatedQuestion.getId());
        return ResponseEntity.ok(questionResponse);
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<Void> removeQuestionFromQuiz(@PathVariable("quizId") int quizId,
            @PathVariable("questionId") int questionId) {
        System.out.println("QuizController: Quizden soru siliniyor - Quiz ID: " + quizId + ", Soru ID: " + questionId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentRemoverId = getCurrentUserId(authentication);

        quizService.removeQuestionFromQuiz(quizId, questionId, currentRemoverId);

        System.out.println("QuizController: Soru başarıyla silindi - Quiz ID: " + quizId + ", Soru ID: " + questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{quizId}/questions/{questionId}/options")
    public ResponseEntity<OptionResponse> addOptionToQuestion(@PathVariable("quizId") int quizId,
            @PathVariable("questionId") int questionId,
            @Valid @RequestBody OptionCreateRequest optionCreateRequest) {
        System.out.println("QuizController: Soruya şık ekleniyor - Soru ID: " + questionId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentAdderId = getCurrentUserId(authentication);

        Option addedOption = quizService.addOptionToQuestion(
                questionId,
                optionCreateRequest.getText(),
                optionCreateRequest.isCorrect(),
                currentAdderId);

        OptionResponse optionResponse = new OptionResponse(addedOption);

        System.out.println("QuizController: Şık başarıyla eklendi - ID: " + addedOption.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(optionResponse);
    }

    @PutMapping("/{quizId}/questions/{questionId}/options/{optionId}")
    public ResponseEntity<OptionResponse> updateOption(@PathVariable("quizId") int quizId,
            @PathVariable("questionId") int questionId,
            @PathVariable("optionId") int optionId,
            @Valid @RequestBody OptionUpdateRequest optionUpdateRequest) {
        System.out.println("QuizController: Şık güncelleniyor - Şık ID: " + optionId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentUpdaterId = getCurrentUserId(authentication);

        Option updatedOption = quizService.updateOption(
                optionId,
                optionUpdateRequest.getText(),
                optionUpdateRequest.isCorrect(),
                currentUpdaterId);

        OptionResponse optionResponse = new OptionResponse(updatedOption);

        System.out.println("QuizController: Şık başarıyla güncellendi - ID: " + updatedOption.getId());
        return ResponseEntity.ok(optionResponse);
    }

    @DeleteMapping("/{quizId}/questions/{questionId}/options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable("quizId") int quizId,
            @PathVariable("questionId") int questionId,
            @PathVariable("optionId") int optionId) {
        System.out.println("QuizController: Şık siliniyor - Şık ID: " + optionId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentDeleterId = getCurrentUserId(authentication);

        quizService.deleteOption(optionId, currentDeleterId);

        System.out.println("QuizController: Şık başarıyla silindi - Şık ID: " + optionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/teacher/{teacherId}/activate-all")
    public ResponseEntity<?> activateAllQuizzesForTeacher(@PathVariable int teacherId) {
        System.out.println("QuizController: Activating all quizzes for teacher - ID: " + teacherId);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            int currentUserId = getCurrentUserId(authentication);

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

            int activatedCount = quizService.activateAllQuizzesForTeacher(teacherId);

            System.out.println("QuizController: " + activatedCount + " quizzes activated successfully for teacher ID: "
                    + teacherId);

            return ResponseEntity.ok(Map.of(
                    "message", activatedCount + " quizzes activated successfully",
                    "teacherId", teacherId,
                    "activatedCount", activatedCount));

        } catch (Exception e) {
            System.err.println("QuizController: Error activating quizzes - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating quizzes: " + e.getMessage());
        }
    }
}
