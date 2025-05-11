package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // HTTP 400 Bad Request veya Conflict (409) da olabilir
public class QuestionDoesNotBelongToQuizException extends RuntimeException {

    public QuestionDoesNotBelongToQuizException() {
        super("Belirtilen soru bu quize ait degil.");
    }

    public QuestionDoesNotBelongToQuizException(String message) {
        super(message);
    }

    public QuestionDoesNotBelongToQuizException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuestionDoesNotBelongToQuizException(Throwable cause) {
        super(cause);
    }
}