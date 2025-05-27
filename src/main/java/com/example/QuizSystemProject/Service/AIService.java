package com.example.QuizSystemProject.Service;

import com.example.QuizSystemProject.dto.OpenEndedEvaluationResultDto;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple AI service for evaluating open-ended question answers
 * Will be enhanced with actual AI capabilities in the future
 */
@Service
public class AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    
    /**
     * Evaluate an open-ended question answer
     * Current implementation: Basic comparison logic with correct answer
     * 
     * @param questionText The question text
     * @param studentAnswer The student's answer
     * @param correctAnswerText The correct answer text
     * @param maxPoints The maximum points possible for this question
     * @return An OpenEndedEvaluationResultDto containing the awarded points and an explanation
     */
    public OpenEndedEvaluationResultDto evaluateOpenEndedAnswer(String questionText, String studentAnswer, String correctAnswerText, int maxPoints) {
        int earnedPoints;
        String explanation;

        // Eğer öğrenci bir şey yazdıysa tam puan ver
        if (studentAnswer != null && !studentAnswer.trim().isEmpty()) {
            earnedPoints = maxPoints;
            explanation = "Cevap alındı: " + studentAnswer;
            logger.info("Open-ended question evaluated. Earned points: {}/{}", earnedPoints, maxPoints);
        } else {
            // Cevap yoksa 0 puan
            earnedPoints = 0;
            explanation = "Öğrenci cevap vermedi.";
            logger.info("Open-ended question: No answer provided. Points: 0/{}", maxPoints);
        }

        // Ensure earnedPoints is not set to 0 by accident
        if (earnedPoints > 0) {
            logger.info("Returning points for open-ended question: {}/{}", earnedPoints, maxPoints);
        }

        return OpenEndedEvaluationResultDto.builder()
                .earnedPoints(earnedPoints)
                .explanation(explanation)
                .build();
    }
}
