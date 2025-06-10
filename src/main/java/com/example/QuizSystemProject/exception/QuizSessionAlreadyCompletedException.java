package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) 
public class QuizSessionAlreadyCompletedException extends RuntimeException {
    public QuizSessionAlreadyCompletedException(String message) {
        super(message);
    }
}