package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Quiz oturumu bulunamazsa fırlatılır
@ResponseStatus(HttpStatus.NOT_FOUND) // 404 Not Found durum kodu döndürür
public class QuizSessionNotFoundException extends RuntimeException {
    public QuizSessionNotFoundException(String message) {
        super(message);
    }
}