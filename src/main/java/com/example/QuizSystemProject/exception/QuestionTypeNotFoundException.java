package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // HTTP 404 Not Found
public class QuestionTypeNotFoundException extends RuntimeException {

    public QuestionTypeNotFoundException() {
        super("Soru tipi bulunamadi.");
    }

    public QuestionTypeNotFoundException(String message) {
        super(message);
    }

    public QuestionTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuestionTypeNotFoundException(Throwable cause) {
        super(cause);
    }
}