package com.example.QuizSystemProject.exception; // Paket adınızın doğru olduğundan emin olun

import org.springframework.http.HttpStatus; // HTTP durum kodu için
import org.springframework.web.bind.annotation.ResponseStatus; // HTTP durum kodunu belirtmek için

// Bu exception, kullanıcı veritabanında bulunamadığında fırlatılacaktır.
// @ResponseStatus anotasyonu, Spring'in bu exception'ı yakaladığında otomatik olarak
// 404 Not Found HTTP yanıtı dönmesini sağlar (Controller metotlarından fırlatıldığında).
@ResponseStatus(HttpStatus.NOT_FOUND) // HTTP 404 Not Found yanıtı döndürür
public class UserNotFoundException extends RuntimeException { // RuntimeException'dan extend ediyoruz

    // Argümansız constructor
    public UserNotFoundException() {
        super("Kullanıcı bulunamadı."); // Varsayılan hata mesajı
    }

    // Hata mesajı alan constructor
    public UserNotFoundException(String message) {
        super(message); // Belirtilen hata mesajını kullanır
    }

    // Hata mesajı ve neden (cause) alan constructor
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause); // Belirtilen mesajı ve nedeni kullanır
    }

     // Neden (cause) alan constructor
     public UserNotFoundException(Throwable cause) {
         super(cause); // Belirtilen nedeni kullanır
     }
}