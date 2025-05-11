package com.example.QuizSystemProject.exception; // Paket adınızın doğru olduğundan emin olun

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Bu exception, süresi dolmuş bir token kullanıldığında fırlatılır.
@ResponseStatus(HttpStatus.BAD_REQUEST) // HTTP 400 Bad Request yanıtı döndürür
public class ExpiredTokenException extends RuntimeException {

    public ExpiredTokenException() {
        super("Token süresi dolmuş.");
    }

    public ExpiredTokenException(String message) {
        super(message);
    }

    public ExpiredTokenException(String message, Throwable cause) {
        super(message, cause);
    }

     public ExpiredTokenException(Throwable cause) {
         super(cause);
     }
}