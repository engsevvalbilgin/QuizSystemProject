package com.example.QuizSystemProject.exception;

public class QuizSessionCompletedException extends RuntimeException {
    
    public QuizSessionCompletedException(String message) {
        super(message);
    }
    
    public QuizSessionCompletedException(String message, Throwable cause) {
        super(message, cause);
    }
}
