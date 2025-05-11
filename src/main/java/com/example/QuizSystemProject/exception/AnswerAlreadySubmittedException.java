package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Bir soruya zaten cevap verilmişse fırlatılır
@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict durum kodu döndürür
public class AnswerAlreadySubmittedException extends RuntimeException {
    public AnswerAlreadySubmittedException(String message) {
        super(message);
    }
}