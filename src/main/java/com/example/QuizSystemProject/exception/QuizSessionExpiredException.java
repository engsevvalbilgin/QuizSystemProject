package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) 
public class QuizSessionExpiredException extends RuntimeException {
    public QuizSessionExpiredException(String message) {
        super(message);
    }
}