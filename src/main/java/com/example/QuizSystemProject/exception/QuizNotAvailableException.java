package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Quiz aktif değilse veya süresi dolmuşsa fırlatılır
@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request durum kodu döndürür
public class QuizNotAvailableException extends RuntimeException {
    public QuizNotAvailableException(String message) {
        super(message);
    }
}