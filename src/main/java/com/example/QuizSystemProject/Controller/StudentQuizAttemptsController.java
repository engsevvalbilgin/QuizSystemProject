package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Model.Quiz;
import com.example.QuizSystemProject.Model.QuizSession;
import com.example.QuizSystemProject.Service.StudentQuizService;
import com.example.QuizSystemProject.dto.QuizAttemptDto;
import com.example.QuizSystemProject.dto.DetailedQuizAttemptDto;


import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.QuizSystemProject.Repository.QuizSessionRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/student/quiz-attempts")
@RequiredArgsConstructor
public class StudentQuizAttemptsController {

    private final StudentQuizService studentQuizService;
    private final QuizSessionRepository quizSessionRepository;
    /**
     * Get all quiz attempts for the current student
     */
    @GetMapping
    public List<QuizAttemptDto> getStudentQuizAttempts() {
        int studentId = studentQuizService.getCurrentUserId();
    List<QuizSession> sessions = quizSessionRepository.findByStudentId(studentId);

    // Her quiz için en son oturumu veya en iyi oturumu seçmek için bir Map kullanın
    Map<Integer, QuizSession> latestOrBestSessionByQuizId = new HashMap<>();

    for (QuizSession session : sessions) {
        Integer quizId = session.getQuiz().getId();
        latestOrBestSessionByQuizId.compute(quizId, (key, existingSession) -> {
            if (existingSession == null) {
                return session;
            }
            // Burada oturum seçimi mantığını belirleyin:
            // Örneğin, en son biten oturumu tercih et:
            if (session.getEndTime() != null && existingSession.getEndTime() == null) {
                return session; // Yeni oturum tamamlanmışsa ve eskisi tamamlanmamışsa yeniyi al
            } else if (session.getEndTime() != null && existingSession.getEndTime() != null) {
                return session.getEndTime().isAfter(existingSession.getEndTime()) ? session : existingSession;
            } else if (session.getEndTime() == null && existingSession.getEndTime() == null) {
                return session.getStartTime().isAfter(existingSession.getStartTime()) ? session : existingSession;
            }
            // Veya puanına göre en iyi oturumu tercih et (eğer puanlama yapıldıysa):
            // return (session.getScore() != null && existingSession.getScore() != null && session.getScore() > existingSession.getScore()) ? session : existingSession;
            return existingSession; // Varsayılan olarak mevcut oturumu koru
        });
    }

    List<QuizAttemptDto> attempts = new ArrayList<>();
    for (QuizSession session : latestOrBestSessionByQuizId.values()) {
        Quiz quiz = session.getQuiz();
        // ... QuizAttemptDto oluşturma kodları ...
        attempts.add(
            QuizAttemptDto.builder()
                .id(session.getId())
                .quizId(quiz.getId())
                .quizName(quiz.getName())
                .teacherName(quiz.getTeacher() != null ? quiz.getTeacher().getName() : "Unknown")
                .studentId(studentId)
                .startDate(session.getStartTime())
                .completionDate(session.getEndTime())
                .score(session.getScore())
                .passingScore(quiz.getPassingScore())
                .status(session.isSubmitted() ? "Tamamlandı" : "Devam Ediyor")
                .timeSpentSeconds(session.getTimeSpentSeconds() != null ? session.getTimeSpentSeconds() : 0)
                .correctAnswers(session.getCorrectAnswers())
                .totalQuestions(quiz.getQuestions().size())
                .totalPoints(quiz.getTotalPoints())
                .earnedPoints(session.getEarnedPoints())
                .build()
        );
    }
    return attempts;
}

    /**
     * Get a specific quiz attempt by ID
     */
    @GetMapping("/{attemptId}")
    public ResponseEntity<QuizAttemptDto> getQuizAttemptById(
            @PathVariable int attemptId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            int studentId = studentQuizService.getCurrentUserId();
            QuizAttemptDto attempt = studentQuizService.getQuizAttemptById(attemptId, studentId);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Get detailed quiz attempt with question results
     */
    @GetMapping("/{attemptId}/details")
    public ResponseEntity<DetailedQuizAttemptDto> getDetailedQuizAttempt(
            @PathVariable int attemptId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            int studentId = studentQuizService.getCurrentUserId();
            DetailedQuizAttemptDto attempt = studentQuizService.getDetailedQuizAttemptById(attemptId, studentId);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
