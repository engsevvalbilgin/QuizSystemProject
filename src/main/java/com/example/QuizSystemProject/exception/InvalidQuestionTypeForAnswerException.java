package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Soru tipine uygun olmayan cevap formatı gelirse fırlatılır (örn: açık uçlu soruya şık seçme)
@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request durum kodu döndürür
public class InvalidQuestionTypeForAnswerException extends RuntimeException {
    public InvalidQuestionTypeForAnswerException(String message) {
        super(message);
    }
}