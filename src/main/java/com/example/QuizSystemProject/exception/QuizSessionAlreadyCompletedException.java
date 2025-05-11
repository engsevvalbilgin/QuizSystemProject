package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Quiz oturumu zaten tamamlanmışsa fırlatılır
@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict durum kodu döndürür
public class QuizSessionAlreadyCompletedException extends RuntimeException {
    public QuizSessionAlreadyCompletedException(String message) {
        super(message);
    }
}