package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // HTTP 404 Not Found
public class OptionNotFoundException extends RuntimeException {

    public OptionNotFoundException() {
        super("Sik bulunamadi.");
    }

    public OptionNotFoundException(String message) {
        super(message);
    }

    public OptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OptionNotFoundException(Throwable cause) {
        super(cause);
    }
}