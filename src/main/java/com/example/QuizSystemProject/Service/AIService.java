package com.example.QuizSystemProject.Service;

import com.example.QuizSystemProject.dto.OpenEndedEvaluationResultDto;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    
 
    public OpenEndedEvaluationResultDto evaluateOpenEndedAnswer(String questionText, String studentAnswer, String correctAnswerText, int maxPoints) {
        int earnedPoints;
        String explanation;

        if (studentAnswer != null && !studentAnswer.trim().isEmpty()) {
            earnedPoints = maxPoints;
            explanation = "Cevap alındı: " + studentAnswer;
            logger.info("Open-ended question evaluated. Earned points: {}/{}", earnedPoints, maxPoints);
        } else {
            earnedPoints = 0;
            explanation = "Öğrenci cevap vermedi.";
            logger.info("Open-ended question: No answer provided. Points: 0/{}", maxPoints);
        }
        if (earnedPoints > 0) {
            logger.info("Returning points for open-ended question: {}/{}", earnedPoints, maxPoints);
        }

        return OpenEndedEvaluationResultDto.builder()
                .earnedPoints(earnedPoints)
                .explanation(explanation)
                .build();
    }
}
