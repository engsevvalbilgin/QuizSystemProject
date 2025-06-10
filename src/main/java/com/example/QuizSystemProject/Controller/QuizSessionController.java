package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.dto.AnswerSubmissionRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.QuizSystemProject.dto.QuizSessionResponse;

import com.example.QuizSystemProject.Model.QuizSession;
import com.example.QuizSystemProject.Model.AnswerAttempt;
import com.example.QuizSystemProject.Service.QuizSessionService;
import com.example.QuizSystemProject.Service.StudentQuizService;
import com.example.QuizSystemProject.dto.QuizSessionDetailsResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.QuizSystemProject.dto.AnswerAttemptResponse;
import java.util.List;

import java.util.*;
import com.example.QuizSystemProject.dto.QuizWithQuestionsDto;

@RestController
@RequestMapping("/api/sessions")
public class QuizSessionController {

    private final QuizSessionService quizSessionService;
    private final StudentQuizService studentQuizService;

    public QuizSessionController(QuizSessionService quizSessionService, StudentQuizService studentQuizService) {
        this.quizSessionService = quizSessionService;
        this.studentQuizService = studentQuizService;
    }

    @PostMapping("/start/{quizId}")
    public ResponseEntity<Map<String, Object>> startQuizSession(@PathVariable("quizId") int quizId) {
        System.out.println("QuizSessionController: Quiz oturumu başlatılıyor - Quiz ID: " + quizId);

        try {
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

            QuizSession newSession = quizSessionService.startQuizSession(currentStudentId, quizId);

            QuizWithQuestionsDto quizWithQuestions = studentQuizService.getQuizWithQuestions(quizId, currentStudentId);

            Map<String, Object> response = new HashMap<>();
            response.put("session", new QuizSessionResponse(newSession));
            response.put("quiz", quizWithQuestions);

            System.out.println(
                    "QuizSessionController: Quiz oturumu başarıyla başlatıldı - Oturum ID: " + newSession.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("Quiz oturumu başlatılırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<AnswerAttemptResponse> submitAnswer(@PathVariable("sessionId") int sessionId,
            @Valid @RequestBody AnswerSubmissionRequest answerSubmissionRequest) {
        System.out.println("QuizSessionController: Cevap gönderildi - Oturum ID: " + sessionId + ", Soru ID: "
                + answerSubmissionRequest.getQuestionId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentStudentId;
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

        int questionId = answerSubmissionRequest.getQuestionId();
        String submittedAnswerText = answerSubmissionRequest.getSubmittedAnswerText();
        Set<Integer> selectedOptionIds = answerSubmissionRequest.getSelectedOptionIds();

        AnswerAttempt savedAttempt = quizSessionService.submitAnswer(
                sessionId,
                questionId,
                submittedAnswerText,
                selectedOptionIds,
                currentStudentId);

        AnswerAttemptResponse attemptResponse = new AnswerAttemptResponse(savedAttempt);

        System.out.println("QuizSessionController: Cevap başarıyla kaydedildi - Attempt ID: " + savedAttempt.getId());
        return ResponseEntity.ok(attemptResponse);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<QuizSessionDetailsResponse> getQuizSessionDetails(@PathVariable("sessionId") int sessionId) {
        System.out.println("QuizSessionController: Oturum detayları getiriliyor - Oturum ID: " + sessionId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int currentViewerId;
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

        QuizSession session = quizSessionService.getQuizSessionDetails(sessionId, currentViewerId); 
                                                                                                    

        QuizSessionDetailsResponse sessionDetailsResponse = new QuizSessionDetailsResponse(session);

        System.out
                .println("QuizSessionController: Oturum detayları başarıyla getirildi - Oturum ID: " + session.getId());
        return ResponseEntity.ok(sessionDetailsResponse);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("#studentId == authentication.principal.username or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<QuizSessionResponse>> getStudentQuizSessions(@PathVariable("studentId") int studentId) {
        System.out.println("QuizSessionController: Öğrenci oturumları getiriliyor - Öğrenci ID: " + studentId);

        List<QuizSession> sessions = quizSessionService.getStudentQuizSessions(studentId);

        List<QuizSessionResponse> sessionResponses = sessions.stream()
                .map(QuizSessionResponse::new)
                .sorted((s1, s2) -> {
                    if (s1.getStartTime() != null && s2.getStartTime() != null) {
                        return s1.getStartTime().compareTo(s2.getStartTime());
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        System.out.println("QuizSessionController: Öğrenci ID " + studentId + " icin " + sessionResponses.size()
                + " adet oturum DTO'su oluşturuldu.");
        return ResponseEntity.ok(sessionResponses);
    }

}