package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Model.Question;
import com.example.QuizSystemProject.Service.AIService;
import com.example.QuizSystemProject.Service.QuizSessionService;
import com.example.QuizSystemProject.Service.StudentQuizService;
import com.example.QuizSystemProject.dto.OpenEndedEvaluationResultDto;
import com.example.QuizSystemProject.dto.QuizSessionStartResponse;
import com.example.QuizSystemProject.dto.QuizResultDto;
import com.example.QuizSystemProject.dto.StudentAnswerDto;
import com.example.QuizSystemProject.dto.AnswerType;
import com.example.QuizSystemProject.dto.CompleteQuizSessionRequest;
import com.example.QuizSystemProject.security.JwtTokenUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/student/quiz-sessions")
@RequiredArgsConstructor
public class StudentQuizSessionController {
    
    private final StudentQuizService studentQuizService;
    private final QuizSessionService quizSessionService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AIService aiService;

    /**
     * Creates a success response with the given message and data
     * @param message The success message
     * @param data Additional data to include in the response
     * @return A map containing the success response
     */
    private Map<String, Object> createSuccessResponse(String message, Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (data != null) {
            response.putAll(data);
        }
        return response;
    }

    /**
     * Creates an error response with the given error message
     * @param errorMessage The error message to include in the response
     * @return A map containing the error response
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", errorMessage);
        return response;
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> completeQuizSession(
            @Valid @RequestBody CompleteQuizSessionRequest request,
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        log.info("\n=== QUIZ COMPLETION REQUEST RECEIVED ===");
        log.info("Request received at: {}", new java.util.Date());
        log.info("Quiz Session ID: {}", request.getQuizSessionId());
        log.info("Number of answers received: {}", request.getAnswers() != null ? request.getAnswers().size() : 0);
        
        // Log detailed answer information
        if (request.getAnswers() != null) {
            log.info("--- Detailed Answers ---");
            for (int i = 0; i < request.getAnswers().size(); i++) {
                CompleteQuizSessionRequest.QuestionAnswerDto answer = request.getAnswers().get(i);
                log.info("Answer {}: Question ID={}", i + 1, answer.getQuestionId());
                if (answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().isEmpty()) {
                    log.info("  - Multiple Choice Options: {}", answer.getSelectedOptionIds());
                }
                if (answer.getOpenEndedAnswer() != null && !answer.getOpenEndedAnswer().trim().isEmpty()) {
                    log.info("  - Open Ended Answer: {}", answer.getOpenEndedAnswer());
                }
            }
            log.info("--- End of Answers ---\n");
        }
        // Detailed logging for JSON payload deserialization verification
        log.info("=== QUIZ COMPLETION REQUEST RECEIVED ====");
        log.info("Quiz tamamlama isteği alındı. QuizSessionId: {}", request.getQuizSessionId());
        
        // Count multiple choice and open-ended answers
        int multipleChoiceCount = 0;
        int openEndedCount = 0;
        
        log.info("Gelen cevapların detaylı dökümü:");
        if (request.getAnswers() != null) {
            for (int i = 0; i < request.getAnswers().size(); i++) {
                CompleteQuizSessionRequest.QuestionAnswerDto answer = request.getAnswers().get(i);
                if (answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().isEmpty()) {
                    log.info("Çoktan seçmeli cevap #{} - Soru ID: {}, Seçilen Seçenek ID'leri: {}", 
                            i+1, answer.getQuestionId(), answer.getSelectedOptionIds());
                    multipleChoiceCount++;
                }
                
                if (answer.getOpenEndedAnswer() != null && !answer.getOpenEndedAnswer().trim().isEmpty()) {
                    log.info("Açık uçlu cevap #{} - Soru ID: {}, Cevap: {}", 
                            i+1, answer.getQuestionId(), answer.getOpenEndedAnswer());
                    openEndedCount++;
                }
            }
        }
        
        log.info("Toplam {} cevap alındı: {} çoktan seçmeli, {} açık uçlu", 
                request.getAnswers() != null ? request.getAnswers().size() : 0, 
                multipleChoiceCount, openEndedCount);
        log.info("=== JSON DESERIALIZATION COMPLETED ====");
        
        // Log authorization header (masking the token for security)
        String authHeaderLog = authorizationHeader != null ? 
            (authorizationHeader.length() > 10 ? 
                authorizationHeader.substring(0, 10) + "..." + 
                authorizationHeader.substring(authorizationHeader.length() - 5) : 
                "[TOO_SHORT]") : 
            "[NULL]";
            
        log.info("Authorization header: {}", authHeaderLog);
        
        // Validate authorization header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication required"));
        }

        try {
            // Extract and validate JWT token
            String token = authorizationHeader.substring(7);
            log.debug("JWT Token extracted (first 10 chars): {}", token.length() > 10 ? token.substring(0, 10) + "..." : token);
            
            int studentId = jwtTokenUtil.getStudentIdFromToken(token);
            log.info("Extracted student ID from token: {}", studentId);
            
            if (studentId <= 0) {
                log.warn("Invalid student ID from token: {}", studentId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid authentication token"));
            }
            
            // Validate request
            log.info("Validating request...");
            if (request.getQuizSessionId() <= 0) {
                String errorMsg = "Quiz session ID is required and must be greater than 0";
                log.warn("Validation failed: {}", errorMsg);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse(errorMsg));
            }
            
            if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
                String errorMsg = "At least one answer is required";
                log.warn("Validation failed: {}", errorMsg);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse(errorMsg));
            }
            
            log.info("Processing {} answers for quiz session: {}, student: {}", 
                    request.getAnswers().size(), request.getQuizSessionId(), studentId);
            
            // Log request structure for debugging
            log.info("Complete request structure deserialized from JSON: {}", request);
            
            // Save all answers
            log.info("Processing answers for quiz session: {}", request.getQuizSessionId());
            int answerCount = 0;
            
            for (CompleteQuizSessionRequest.QuestionAnswerDto answer : request.getAnswers()) {
                answerCount++;
                log.debug("Processing answer {} of {} - Question ID: {}", 
                    answerCount, request.getAnswers().size(), answer.getQuestionId());
                    
                try {
                    StudentAnswerDto answerDto = new StudentAnswerDto();
                    answerDto.setQuestionId(answer.getQuestionId());
                    answerDto.setSelectedOptionIds(answer.getSelectedOptionIds());
                    answerDto.setTextAnswer(answer.getOpenEndedAnswer());
                    
                    // Log answer details
                    log.debug("Answer {} - Question ID: {}", answerCount, answer.getQuestionId());
                    
                    // Determine and set the correct answer type based on data
                    if (answer.getOpenEndedAnswer() != null && !answer.getOpenEndedAnswer().trim().isEmpty()) {
                        answerDto.setAnswerType(AnswerType.TEXT);
                        log.info("Processing Answer #{}/{} - Question ID: {} - Type: Open Ended - Answer: {}", 
                            answerCount, request.getAnswers().size(), 
                            answer.getQuestionId(), 
                            answer.getOpenEndedAnswer().length() > 50 ? 
                                answer.getOpenEndedAnswer().substring(0, 50) + "..." : 
                                answer.getOpenEndedAnswer());
                    } else if (answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().isEmpty()) {
                        answerDto.setAnswerType(AnswerType.MULTIPLE_CHOICE);
                        log.info("Processing Answer #{}/{} - Question ID: {} - Type: Multiple Choice - Selected Options: {}", 
                            answerCount, request.getAnswers().size(),
                            answer.getQuestionId(), 
                            answer.getSelectedOptionIds());
                    } else {
                        log.warn("Answer #{}/{} - Question ID: {} - No valid answer type detected (neither open-ended nor multiple choice)", 
                            answerCount, request.getAnswers().size(), answer.getQuestionId());
                        answerDto.setAnswerType(AnswerType.TEXT); // Default as text for backward compatibility
                    }
                    
                    // Evaluate open-ended answers if present
                    if (answer.getOpenEndedAnswer() != null && !answer.getOpenEndedAnswer().trim().isEmpty()) {
                        // Get the question to pass to AIService
                        Question question = quizSessionService.getQuestionById(answer.getQuestionId());
                        if (question != null) {
                            // Use AIService to evaluate the answer
                            OpenEndedEvaluationResultDto evaluationResult = aiService.evaluateOpenEndedAnswer(
                                question.getQuestionSentence(),
                                answer.getOpenEndedAnswer(),
                                question.getCorrectAnswerText(),
                                question.getPoints()
                            );
                            
                            // Set both score and AI explanation
                            answerDto.setScore(evaluationResult.getEarnedPoints());
                            answerDto.setAiExplanation(evaluationResult.getExplanation());
                            
                            log.info("AI evaluated open-ended answer - Question ID: {}, Score: {}/{}, Explanation: {}",
                                question.getId(), evaluationResult.getEarnedPoints(), question.getPoints(), 
                                evaluationResult.getExplanation());
                        } else {
                            log.warn("Question not found: {}", answer.getQuestionId());
                            answerDto.setScore(0);
                        }
                    }
                    
                    // Log before saving
                    log.debug("Saving answer {}/{} for question ID: {} - Type: {}", 
                        answerCount, request.getAnswers().size(), 
                        answer.getQuestionId(),
                        answerDto.getAnswerType());
                        
                    // Save the answer using the quiz session service
                    boolean isSaved = quizSessionService.saveAnswer(
                            request.getQuizSessionId(),
                            studentId,
                            answerDto
                    );
                    
                    // Log the result
                    if (isSaved) {
                        log.info("Successfully saved answer {}/{} for question ID: {}",
                            answerCount, request.getAnswers().size(),
                            answer.getQuestionId());
                    } else {
                        log.warn("Failed to save answer {}/{} for question ID: {}",
                            answerCount, request.getAnswers().size(),
                            answer.getQuestionId());
                    }
                        
                } catch (Exception e) {
                    log.error("Error saving answer {}/{} for question {}: {}", 
                        answerCount, request.getAnswers().size(),
                        answer.getQuestionId(), 
                        e.getMessage(), 
                        e);
                    // Continue with other answers even if one fails
                }
            }
            
            // Complete the quiz session
            log.info("Marking quiz session {} as completed for student {}", 
                request.getQuizSessionId(), studentId);
                
            QuizResultDto result = quizSessionService.completeQuizSession(
                request.getQuizSessionId(), 
                studentId
            );
            
            // Log completion
            log.info("Quiz session {} completed successfully for student {} - Score: {}/{}", 
                request.getQuizSessionId(), 
                studentId,
                result != null ? result.getScore() : "N/A",
                result != null ? result.getTotalPoints() : "N/A");
                
            // Prepare success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quiz submitted successfully");
            response.put("quizSessionId", request.getQuizSessionId());
            response.put("processedAnswers", answerCount);
            
            if (result != null) {
                response.put("score", result.getScore());
                response.put("totalPoints", result.getTotalPoints());
                response.put("earnedPoints", result.getEarnedPoints());
                response.put("correctAnswers", result.getCorrectAnswers());
                response.put("totalQuestions", result.getTotalQuestions());
                
                // Calculate percentage if needed
                if (result.getTotalPoints() > 0) {
                    double percentage = (result.getEarnedPoints() * 100.0) / result.getTotalPoints();
                    response.put("percentage", Math.round(percentage * 100.0) / 100.0);
                } else {
                    response.put("percentage", 0);
                }
            }
            
            log.info("Successfully processed quiz session {} with {} answers", 
                request.getQuizSessionId(), answerCount);
            
            // Return success response with result
            return ResponseEntity.ok(createSuccessResponse(
                "Quiz completed successfully", 
                Map.of("result", result)
            ));
            
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Quiz session or related entity not found"));
                    
        } catch (AccessDeniedException | SecurityException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Not authorized to complete this quiz session"));
                    
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
                    
        } catch (Exception e) {
            log.error("Unexpected error completing quiz session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An unexpected error occurred while completing the quiz"));
        }
    }
    
    @PostMapping("/start/{quizId}")
    public ResponseEntity<?> startQuizSession(
            @PathVariable("quizId") int quizId,
            @RequestHeader(value = "Authorization") String authHeader
    ) {
        try {
            log.info("Starting quiz session. Quiz ID: {}", quizId);
            
            // Extract and validate JWT token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String token = authHeader.substring(7);
            int studentId = jwtTokenUtil.getStudentIdFromToken(token);
            
            if (studentId <= 0) {
                log.warn("Invalid student ID from token: {}", studentId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            log.info("Student ID: {}", studentId);
            
            // Start quiz session
            QuizSessionStartResponse response = studentQuizService.startQuizSession(quizId, studentId);
            return ResponseEntity.ok(createSuccessResponse("Quiz session started successfully", 
                Map.of("session", response)));
            
        } catch (Exception e) {
            log.error("Error starting quiz session - Quiz ID: {}", quizId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while starting the quiz session"));
        }
    }

    /**
     * Submits an answer to a question
     * @param sessionId The ID of the quiz session
     * @param answerDto The answer data
     * @param authHeader The Authorization header containing the JWT token
     * @return Response with the result of the operation
     */
    @PostMapping("/{sessionId}/answers")
    public ResponseEntity<Map<String, Object>> submitAnswer(
            @PathVariable("sessionId") int sessionId,
            @RequestBody StudentAnswerDto answerDto,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            // Validate authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }
            
            // Extract and validate JWT token
            String token = authHeader.substring(7);
            int studentId = jwtTokenUtil.getStudentIdFromToken(token);
            
            if (studentId <= 0) {
                log.warn("Invalid student ID from token: {}", studentId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid authentication token"));
            }
            
            // For open-ended questions, evaluate the answer using AIService
            if (answerDto.getAnswerType() == AnswerType.TEXT && answerDto.getTextAnswer() != null && !answerDto.getTextAnswer().trim().isEmpty()) {
                // Get the question details
                Question question = quizSessionService.getQuestionById(answerDto.getQuestionId());
                
                // Call AIService to evaluate the answer
                OpenEndedEvaluationResultDto evaluationResult = aiService.evaluateOpenEndedAnswer(
                    question.getQuestionSentence(),
                    answerDto.getTextAnswer(),
                    question.getCorrectAnswerText(),
                    question.getPoints()
                );
                
                // Set both score and explanation in the answerDto
                answerDto.setScore(evaluationResult.getEarnedPoints());
                answerDto.setAiExplanation(evaluationResult.getExplanation());
                
                log.info("AI evaluation for question {} - Score: {}/{}, Explanation: {}", 
                    answerDto.getQuestionId(), evaluationResult.getEarnedPoints(), 
                    question.getPoints(), evaluationResult.getExplanation());
            }
            
            // Save the answer
            boolean success = quizSessionService.saveAnswer(sessionId, studentId, answerDto);
            
            if (success) {
                return ResponseEntity.ok(createSuccessResponse(
                    "Answer saved successfully",
                    Map.of("score", answerDto.getScore())
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Failed to save answer"));
            }
        } catch (Exception e) {
            log.error("Error submitting answer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while saving the answer"));
        }
    }
    


    
}
