package com.example.QuizSystemProject.exception; // Dosyayı kaydettiğiniz pakete uygun olarak ayarlayın

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice; // REST Controller'lar için

import java.time.LocalDateTime; // İsteğe bağlı, hata zamanını eklemek için


// Bu anotasyon, uygulamanın tüm Controller'larından (özellikle RestController'lardan) fırlatılan
// exception'ları yakalamak için bu sınıfın kullanılacağını belirtir.
@RestControllerAdvice
// Veya sadece belirli controller'lar için @ControllerAdvice(assignableTypes = {QuizController.class, UserController.class, QuizSessionController.class})
public class GlobalExceptionHandler {

    // İsteğe bağlı: Daha standart bir hata yanıtı gövdesi için iç içe sınıf veya ayrı bir DTO
    // static class ErrorResponse {
    //     private LocalDateTime timestamp;
    //     private int status;
    //     private String error; // HttpStatus'tan gelen "Not Found", "Bad Request" gibi
    //     private String message; // Exception'ın mesajı
    //     // private String path; // HttpServletRequest'ten alınabilir eğer Handler metoduna eklenirse
    //
    //     public ErrorResponse(HttpStatus status, String message) {
    //         this.timestamp = LocalDateTime.now();
    //         this.status = status.value();
    //         this.error = status.getReasonPhrase();
    //         this.message = message;
    //     }
         // Getter metotları...
    // }


    // Kaynak bulunamadı (404) hatalarını yakala
    @ExceptionHandler({
        UserNotFoundException.class,
        QuizNotFoundException.class,
        QuestionNotFoundException.class,
        OptionNotFoundException.class,
        QuestionTypeNotFoundException.class,
        QuizSessionNotFoundException.class // <--- YENİ EKLENDİ
        
    })
    
    public ResponseEntity<String> handleResourceNotFoundException(RuntimeException ex) { // Bu exception'ların hepsi RuntimeException'dan extend ediyor
        System.err.println("GlobalExceptionHandler: Kaynak bulunamadi (404) hatasi yakalandi - " + ex.getMessage());
        // Exception'ın @ResponseStatus'ı varsa onu kullan, yoksa default 404 dön
        HttpStatus status = HttpStatus.NOT_FOUND;
        if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
            status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        }
        return ResponseEntity.status(status).body(ex.getMessage());
    }

    // Yetkilendirme hatası (403 Forbidden) yakala
    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<String> handleUserNotAuthorizedException(UserNotAuthorizedException ex) {
        System.err.println("GlobalExceptionHandler: Yetkilendirme (403) hatasi yakalandi - " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

     // Geçersiz argüman veya iş mantığı hatası (400 Bad Request) yakala
     // IllegalArgumentException, QuestionDoesNotBelongToQuizException, InvalidQuestionTypeForOptionException gibi
     @ExceptionHandler({
        IllegalArgumentException.class,
        QuestionDoesNotBelongToQuizException.class,
        InvalidQuestionTypeForOptionException.class,
        QuizNotAvailableException.class, // <--- YENİ EKLENDİ
        InvalidQuestionTypeForAnswerException.class, // <--- YENİ EKLENDİ
        QuizSessionExpiredException.class, // <-- YENİ EKLENDİ
        InvalidOptionForQuestionException.class // <-- YENİ EKLENDİ
     })
     public ResponseEntity<String> handleBadRequestException(RuntimeException ex) { // Bu exception'ların hepsi RuntimeException'dan extend ediyor
        System.err.println("GlobalExceptionHandler: Geçersiz İstek (400) hatasi yakalandi - " + ex.getMessage());
         // Exception'ın @ResponseStatus'ı varsa onu kullan (örn: QuestionDoesNotBelongToQuizException'da 400), yoksa default 400 dön
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
            status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        }
        return ResponseEntity.status(status).body(ex.getMessage());
     }

     // Veri çakışması (409 Conflict) hatalarını yakala (Örn: Duplicate kullanıcı adı/email, zaten cevap verilmiş)
     @ExceptionHandler({
         DuplicateUsernameException.class,
         DuplicateEmailException.class,
         AnswerAlreadySubmittedException.class, // <--- YENİ EKLENDİ
         QuizAlreadyTakenException.class,
         QuizSessionAlreadyCompletedException.class
     })
     public ResponseEntity<String> handleConflictException(RuntimeException ex) { // Bu exception'ların hepsi RuntimeException'dan extend ediyor
         System.err.println("GlobalExceptionHandler: Veri cakismasi (409) hatasi yakalandi - " + ex.getMessage());
         // Exception'ın @ResponseStatus'ı varsa onu kullan (örn: 409), yoksa default 409 dön
         HttpStatus status = HttpStatus.CONFLICT;
         if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
             status = ex.getClass().getAnnotation(ResponseStatus.class).value();
         }
         return ResponseEntity.status(status).body(ex.getMessage());
     }

     // Kimlik doğrulama hatası (401 Unauthorized) yakala
     // BadCredentialsException gibi Spring Security hataları da burada yakalanabilir.
     // AuthenticationException'ı yakalamak genellikle Spring Security'nin kendi handler'larına bırakılır,
     // ancak özel durumlar için burada yakalanabilir. AuthenticationException Spring'in bir hatasıdır.
     // Eğer BadCredentialsException gibi spesifik hataları yakalamak istiyorsanız, onları buraya ekleyin.
     // @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class}) // AuthenticationException için Spring Security importu gerekir
     // public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) {
     //      System.err.println("GlobalExceptionHandler: Kimlik dogrulama (401) hatasi yakalandi - " + ex.getMessage());
     //      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kimlik doğrulama başarısız: " + ex.getMessage());
     // }
     // Not: Yukarıdaki BadCredentialsException handler'ı User Controller'daki BadCredentialsException catch bloğunu gereksiz kılar.

     // Token hatalarını (geçersiz, süresi dolmuş) yakala (400 veya 401 olabilir)
     // @ResponseStatus ile işaretlendikleri için otomatik durum kodu dönecek, ama burada yakalayabiliriz.
      @ExceptionHandler({
         InvalidTokenException.class,
         ExpiredTokenException.class
      })
      public ResponseEntity<String> handleTokenException(RuntimeException ex) { // RuntimeException'dan extend ediyorlar
          System.err.println("GlobalExceptionHandler: Token hatasi yakalandi - " + ex.getMessage());
          // Exception'ın @ResponseStatus'ı varsa onu kullan, yoksa default 400 dön
          HttpStatus status = HttpStatus.BAD_REQUEST; // Varsayılan
          if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
              status = ex.getClass().getAnnotation(ResponseStatus.class).value();
          }
          return ResponseEntity.status(status).body(ex.getMessage());
      }


    // Beklenmeyen diğer RuntimeException'ları yakala (daha spesifik handler'lar yakalamazsa)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleGenericRuntimeException(RuntimeException ex) {
        System.err.println("GlobalExceptionHandler: Beklenmeyen RuntimeException yakalandi - " + ex.getMessage());
        ex.printStackTrace(); // Hata detaylarını konsola/log dosyasına yazdır
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Beklenmeyen sunucu hatasi olustu."); // 500 Internal Server Error
    }

     // En genel Exception sınıfını da yakalayabiliriz (RuntimeException dahil tüm checked/unchecked hatalar)
     // Bu handler, yukarıdaki hiçbir handler'ın yakalayamadığı tüm Exception'ları yakalar.
     // @ExceptionHandler(Exception.class)
     // public ResponseEntity<String> handleGenericException(Exception ex) {
     //     System.err.println("GlobalExceptionHandler: Beklenmeyen genel Exception yakalandi - " + ex.getMessage());
     //     ex.printStackTrace();
     //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Genel sunucu hatasi olustu."); // 500 Internal Server Error
     // }
}