package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidQuestionTypeForAnswerException extends RuntimeException {
    public InvalidQuestionTypeForAnswerException(String message) {
        super(message);
    }
}