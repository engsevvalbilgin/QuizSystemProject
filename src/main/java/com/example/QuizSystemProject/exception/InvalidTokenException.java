package com.example.QuizSystemProject.exception; // Paket adınızın doğru olduğundan emin olun

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Bu exception, geçersiz (veritabanında bulunmayan) bir token kullanıldığında fırlatılır.
@ResponseStatus(HttpStatus.BAD_REQUEST) // HTTP 400 Bad Request yanıtı döndürür (İsteğin kendisi hatalı)
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