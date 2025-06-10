package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class QuizAlreadyTakenException extends RuntimeException {
    public QuizAlreadyTakenException(String message) {
        super(message);
    }
}