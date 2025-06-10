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

    @GetMapping
    public List<QuizAttemptDto> getStudentQuizAttempts() {
        int studentId = studentQuizService.getCurrentUserId();
        List<QuizSession> sessions = quizSessionRepository.findByStudentId(studentId);

        Map<Integer, QuizSession> latestOrBestSessionByQuizId = new HashMap<>();

        for (QuizSession session : sessions) {
            Integer quizId = session.getQuiz().getId();
            latestOrBestSessionByQuizId.compute(quizId, (key, existingSession) -> {
                if (existingSession == null) {
                    return session;
                }
                if (session.getEndTime() != null && existingSession.getEndTime() == null) {
                    return session;
                } else if (session.getEndTime() != null && existingSession.getEndTime() != null) {
                    return session.getEndTime().isAfter(existingSession.getEndTime()) ? session : existingSession;
                } else if (session.getEndTime() == null && existingSession.getEndTime() == null) {
                    return session.getStartTime().isAfter(existingSession.getStartTime()) ? session : existingSession;
                }
                return existingSession;
            });
        }

        List<QuizAttemptDto> attempts = new ArrayList<>();
        for (QuizSession session : latestOrBestSessionByQuizId.values()) {
            Quiz quiz = session.getQuiz();

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
                            .build());
        }
        return attempts;
    }

    @GetMapping("/{attemptId}")
    public ResponseEntity<QuizAttemptDto> getQuizAttemptById(
            @PathVariable int attemptId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            int studentId = studentQuizService.getCurrentUserId();
            QuizAttemptDto attempt = studentQuizService.getQuizAttemptById(attemptId, studentId);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{attemptId}/details")
    public ResponseEntity<DetailedQuizAttemptDto> getDetailedQuizAttempt(
            @PathVariable int attemptId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
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
