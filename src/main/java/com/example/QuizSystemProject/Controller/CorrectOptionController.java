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

/**
 * Doğru şıkları getirmek için özel controller
 * Bu controller, frontend'in doğru şıkları alma işlemlerini yönetir
 */
@RestController
@RequestMapping("/api/questions")
public class CorrectOptionController {

    private final QuestionRepository questionRepository;

    public CorrectOptionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    /**
     * Bir sorunun doğru şıkkını getirme
     * HTTP GET isteği ile "/api/questions/{questionId}/correct-option" adresine yapılan istekleri karşılar
     */
    @GetMapping("/{questionId}/correct-option")
    public ResponseEntity<OptionResponse> getCorrectOptionForQuestion(@PathVariable("questionId") int questionId) {
        System.out.println("CorrectOptionController: Soru için doğru şık getiriliyor - Soru ID: " + questionId);
        
        try {
            // Soruyu bul
            Optional<Question> questionOpt = questionRepository.findById(questionId);
            
            if (!questionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Question question = questionOpt.get();
            
            // Çoktan seçmeli soru kontrolü
            QuestionType questionType = question.getType();
            if (questionType == null || questionType.getId() != 1) { // 1 = Çoktan Seçmeli
                return ResponseEntity.badRequest().build();
            }
            
            // Doğru şıkkı bul
            Option correctOption = null;
            for (Option option : question.getOptions()) {
                if (option.isCorrect()) {
                    correctOption = option;
                    break;
                }
            }
            
            if (correctOption == null) {
                // Eğer doğru şık yoksa, ilk şıkkı dön
                if (!question.getOptions().isEmpty()) {
                    correctOption = question.getOptions().get(0);
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
            
            // Doğru şıkkı DTO'ya dönüştür
            OptionResponse optionResponse = new OptionResponse(correctOption);
            
            return ResponseEntity.ok(optionResponse);
        } catch (Exception e) {
            System.err.println("CorrectOptionController: Doğru şık getirilirken hata oluştu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
