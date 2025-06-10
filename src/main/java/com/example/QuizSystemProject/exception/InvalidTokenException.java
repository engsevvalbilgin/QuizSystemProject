package com.example.QuizSystemProject.exception; 

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) 
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Geçersiz token.");
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

     public InvalidTokenException(Throwable cause) {
         super(cause);
     }
}