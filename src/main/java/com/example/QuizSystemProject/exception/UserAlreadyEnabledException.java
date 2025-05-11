package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Bir hesabın zaten etkinleştirilmiş olduğu durumda fırlatılacak özel Exception
// @ResponseStatus ile bu hatanın HTTP 400 Bad Request olarak dönmesini sağlayabiliriz
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserAlreadyEnabledException extends RuntimeException {

    public UserAlreadyEnabledException(String message) {
        super(message);
    }
}