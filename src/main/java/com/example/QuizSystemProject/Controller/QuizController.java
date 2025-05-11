package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Model.Quiz;
import com.example.QuizSystemProject.Repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;

    @GetMapping("/{id}")
    public Quiz getQuizDetails(@PathVariable int id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
    }
}
