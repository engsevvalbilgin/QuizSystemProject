package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Service.AIService;
import com.example.QuizSystemProject.dto.OpenEndedEvaluationResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, Object>> evaluateAnswer(
            @RequestBody Map<String, Object> requestBody
    ) {
        String questionText = (String) requestBody.get("questionText");
        String studentAnswer = (String) requestBody.get("studentAnswer");
        String correctAnswerText = (String) requestBody.get("correctAnswerText");
        int maxPoints = Integer.parseInt(requestBody.get("maxPoints").toString());
        
        OpenEndedEvaluationResultDto evaluationResult = aiService.evaluateOpenEndedAnswer(questionText, studentAnswer, correctAnswerText, maxPoints);
        
        return ResponseEntity.ok(Map.of(
            "earnedPoints", evaluationResult.getEarnedPoints(),
            "maxPoints", maxPoints,
            "explanation", evaluationResult.getExplanation()
        ));
    }
}
