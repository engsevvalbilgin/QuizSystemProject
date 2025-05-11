package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // HTTP 404 Not Found
public class QuizNotFoundException extends RuntimeException {

    public QuizNotFoundException() {
        super("Quiz bulunamadi.");
    }

    public QuizNotFoundException(String message) {
        super(message);
    }

    public QuizNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuizNotFoundException(Throwable cause) {
        super(cause);
    }
}