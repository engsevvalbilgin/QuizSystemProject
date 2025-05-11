package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Öğrenci quizi zaten çözmüşse fırlatılır
@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict durum kodu döndürür
public class QuizAlreadyTakenException extends RuntimeException {
    public QuizAlreadyTakenException(String message) {
        super(message);
    }
}