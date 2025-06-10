package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidQuestionTypeForOptionException extends RuntimeException {

    public InvalidQuestionTypeForOptionException() {
        super("Bu soru tipine şık eklenemez.");
    }

    public InvalidQuestionTypeForOptionException(String message) {
        super(message);
    }

    public InvalidQuestionTypeForOptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidQuestionTypeForOptionException(Throwable cause) {
        super(cause);
    }
}