package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) 
public class AnswerAlreadySubmittedException extends RuntimeException {
    public AnswerAlreadySubmittedException(String message) {
        super(message);
    }
}