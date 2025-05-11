package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Quiz oturum süresi dolmuşsa fırlatılır
@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request durum kodu döndürür
public class QuizSessionExpiredException extends RuntimeException {
    public QuizSessionExpiredException(String message) {
        super(message);
    }
}