package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Seçilen şık belirtilen soruya ait değilse fırlatılır
@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request durum kodu döndürür
public class InvalidOptionForQuestionException extends RuntimeException {
    public InvalidOptionForQuestionException(String message) {
        super(message);
    }
}