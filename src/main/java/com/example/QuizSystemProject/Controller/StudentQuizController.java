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

    @GetMapping
    public ResponseEntity<List<StudentQuizDto>> getAvailableQuizzes() {
        int studentId = studentQuizService.getCurrentUserId();
        List<StudentQuizDto> quizzes = studentQuizService.getAvailableQuizzesForStudent(studentId);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<StudentQuizDto> getQuizForStudent(@PathVariable int quizId) {
        int studentId = studentQuizService.getCurrentUserId();
        StudentQuizDto quiz = studentQuizService.getQuizForStudent(quizId, studentId);
        return ResponseEntity.ok(quiz);
    }

    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<List<QuizAttemptDto>> getQuizAttempts(@PathVariable int quizId) {
        int studentId = studentQuizService.getCurrentUserId();
        List<QuizAttemptDto> attempts = studentQuizService.getQuizAttemptsForStudent(quizId, studentId);
        return ResponseEntity.ok(attempts);
    }

}
