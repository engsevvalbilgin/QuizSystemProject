package com.example.QuizSystemProject.exception; // Paket adınızın doğru olduğundan emin olun

import org.springframework.http.HttpStatus; // HTTP durum kodu için
import org.springframework.web.bind.annotation.ResponseStatus; // HTTP durum kodunu belirtmek için

// Bu exception, kaydedilmek istenen e-posta adresinin zaten kullanımda olması durumunda fırlatılacaktır.
// @ResponseStatus anotasyonu, Spring'in bu exception'ı yakaladığında otomatik olarak
// 409 Conflict HTTP yanıtı dönmesini sağlar (Benzersizlik çakışmaları için 409 uygundur).
@ResponseStatus(HttpStatus.CONFLICT) // HTTP 409 Conflict yanıtı döndürür
public class DuplicateEmailException extends RuntimeException { // RuntimeException'dan extend ediyoruz

    // Argümansız constructor
    public DuplicateEmailException() {
        super("Email adresi zaten kullanımda."); // Varsayılan hata mesajı
    }

    // Hata mesajı alan constructor
    public DuplicateEmailException(String message) {
        super(message); // Belirtilen hata mesajını kullanır
    }

    // Hata mesajı ve neden (cause) alan constructor
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause); // Belirtilen mesajı ve nedeni kullanır
    }

     // Neden (cause) alan constructor
     public DuplicateEmailException(Throwable cause) {
         super(cause); // Belirtilen nedeni kullanır
     }
}