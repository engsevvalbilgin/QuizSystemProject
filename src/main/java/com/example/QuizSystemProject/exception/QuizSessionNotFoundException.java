package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) 
public class QuizSessionNotFoundException extends RuntimeException {
    public QuizSessionNotFoundException(String message) {
        super(message);
    }
}