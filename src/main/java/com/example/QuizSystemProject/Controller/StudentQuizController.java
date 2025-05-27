package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Service.StudentQuizService;
import com.example.QuizSystemProject.dto.QuizAttemptDto;
import com.example.QuizSystemProject.dto.StudentQuizDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/quizzes")
@RequiredArgsConstructor
public class StudentQuizController {

    private final StudentQuizService studentQuizService;

    /**
     * Öğrencinin çözebileceği tüm aktif quizleri getirir
     */
    @GetMapping
    public ResponseEntity<List<StudentQuizDto>> getAvailableQuizzes() {
        int studentId = studentQuizService.getCurrentUserId();
        List<StudentQuizDto> quizzes = studentQuizService.getAvailableQuizzesForStudent(studentId);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Belirli bir quizin detaylarını getirir (öğrenci için)
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<StudentQuizDto> getQuizForStudent(@PathVariable int quizId) {
        int studentId = studentQuizService.getCurrentUserId();
        StudentQuizDto quiz = studentQuizService.getQuizForStudent(quizId, studentId);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Get all attempts for a specific quiz by the current student
     */
    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<List<QuizAttemptDto>> getQuizAttempts(@PathVariable int quizId) {
        int studentId = studentQuizService.getCurrentUserId();
        List<QuizAttemptDto> attempts = studentQuizService.getQuizAttemptsForStudent(quizId, studentId);
        return ResponseEntity.ok(attempts);
    }
    
}
