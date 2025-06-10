package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Model.Option;
import com.example.QuizSystemProject.Model.Question;
import com.example.QuizSystemProject.Model.QuestionType;
import com.example.QuizSystemProject.Repository.QuestionRepository;
import com.example.QuizSystemProject.dto.OptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class CorrectOptionController {

    private final QuestionRepository questionRepository;

    public CorrectOptionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping("/{questionId}/correct-option")
    public ResponseEntity<OptionResponse> getCorrectOptionForQuestion(@PathVariable("questionId") int questionId) {
        System.out.println("CorrectOptionController: Soru için doğru şık getiriliyor - Soru ID: " + questionId);

        try {
          
            Optional<Question> questionOpt = questionRepository.findById(questionId);

            if (!questionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Question question = questionOpt.get();

            QuestionType questionType = question.getType();
            if (questionType == null || questionType.getId() != 1) {
                return ResponseEntity.badRequest().build();
            }

            Option correctOption = null;
            for (Option option : question.getOptions()) {
                if (option.isCorrect()) {
                    correctOption = option;
                    break;
                }
            }

            if (correctOption == null) {
                if (!question.getOptions().isEmpty()) {
                    correctOption = question.getOptions().get(0);
                } else {
                    return ResponseEntity.notFound().build();
                }
            }

            OptionResponse optionResponse = new OptionResponse(correctOption);

            return ResponseEntity.ok(optionResponse);
        } catch (Exception e) {
            System.err.println("CorrectOptionController: Doğru şık getirilirken hata oluştu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
