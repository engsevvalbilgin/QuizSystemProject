package com.example.QuizSystemProject.Controller;

// Spring ve Web importları

import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import java.util.Map;
import org.springframework.web.bind.annotation.*; // Tüm Web anotasyonları için
import org.springframework.web.server.ResponseStatusException;

// JPA/Model importları (Controller'da entity döndürmek yerine DTO döndürüyoruz, sadece DTO için gerekli entity importları kalır)
import com.example.QuizSystemProject.Model.User; // Service'ten dönen User Entity'si için (DTO'ya çevrilecek)

// DTO importları
import com.example.QuizSystemProject.dto.LoginRequest; // Login isteği DTO'su
import com.example.QuizSystemProject.dto.UserRegistrationRequest; // Kullanıcı kayıt isteği DTO'su
import com.example.QuizSystemProject.dto.TeacherRegistrationRequest; // Öğretmen kayıt isteği DTO'su
import com.example.QuizSystemProject.dto.UserResponse; // Kayıt yanıtı DTO'su (kullanıcı bilgileri için)
import com.example.QuizSystemProject.dto.AuthResponseDto; // Giriş yanıtı DTO'su (JWT için)
import com.example.QuizSystemProject.dto.EmailRequestDto; // Parola sıfırlama isteği (email içeren) DTO'su
import com.example.QuizSystemProject.dto.ResetPasswordRequest; // Parola sıfırlama tamamlama (token ve yeni parola içeren) DTO'su

// Service importu
import com.example.QuizSystemProject.Service.AuthenticationService; // Kimlik doğrulama servisimiz
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.security.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
// Validasyon importu
import jakarta.validation.Valid; // Validasyon için

// Exception importları (Controller seviyesinde yakalamadıklarımızı kaldırabiliriz)
import com.example.QuizSystemProject.exception.UserNotFoundException; // Forgot-password endpoint'i için hala yakalıyoruz

// Diğer Exception importları (GlobalExceptionHandler'da yakalanacaklar)
// import com.quizland.QuizSystemProject.exception.DuplicateUsernameException;
// import com.quizland.QuizSystemProject.exception.DuplicateEmailException;
// import com.quizland.QuizSystemProject.exception.ExpiredTokenException;
// import com.quizland.QuizSystemProject.exception.InvalidTokenException;
// import com.quizland.QuizSystemProject.exception.UserAlreadyEnabledException;


@RestController // Bu sınıfın bir REST Controller olduğunu belirtir
@RequestMapping("/api/auth") // Bu controller altındaki tüm endpoint'lerin "/api/auth" ile başlayacağını belirtir
public class AuthController {

    private final AuthenticationService authenticationService; // Kimlik doğrulama iş mantığı servisi
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // Bağımlılıkların enjekte edildiği constructor
    public AuthController(AuthenticationService authenticationService, 
                         JwtUtil jwtUtil,
                         UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // --- Kimlik Doğrulama ve Kayıt Endpoint'leri ---

    // Kullanıcı Kayıt Endpoint'i
    // HTTP POST isteği ile "/api/auth/register" adresine yapılan istekleri karşılar
    @PostMapping("/register")
    // ResponseEntity<?> kalabilir veya UserResponse olarak güncellendi
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        System.out.println("AuthController: Kayıt isteği alındı - Kullanıcı adı: " + registrationRequest.getUsername());

        // Service katmanındaki kullanıcı kayıt iş mantığını çağır
        // Service User döndürür veya exception fırlatır (DuplicateUsername/Email gibi).
        // Exception'lar GlobalExceptionHandler tarafından yakalanacak.
        User registeredUser = authenticationService.registerUser(registrationRequest);

        // Kayıt başarılıysa 201 Created (Oluşturuldu) yanıtı döndür
        // User Entity'sini UserResponse DTO'suna çeviriyoruz.
        UserResponse userResponse = new UserResponse(registeredUser);

        System.out.println("AuthController: Kayıt başarılı - Kullanıcı ID: " + registeredUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.err.println("AuthController: Eksik veya hatalı Authorization başlığı: " + authHeader);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçersiz veya eksik token");
        }
        
        try {
            String refreshToken = authHeader.substring(7);
            System.out.println("AuthController: Token yenileme isteği alındı - Token: " + 
                (refreshToken.length() > 10 ? refreshToken.substring(0, 10) + "..." : "[too_short]"));
            
            // Refresh the tokens
            AuthResponseDto authResponse = authenticationService.refreshToken(refreshToken);
            
            if (authResponse == null) {
                System.err.println("AuthController: AuthResponseDto null döndü");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Sunucu hatası");
            }
            
            // Log successful token refresh
            System.out.println("AuthController: Token başarıyla yenilendi - Kullanıcı: " + 
                (authResponse.getUsername() != null ? authResponse.getUsername() : "Bilinmeyen") +
                ", New Refresh Token: " + (authResponse.getRefreshToken() != null ? 
                    authResponse.getRefreshToken().substring(0, 10) + "..." : "[none]"));
                
            return ResponseEntity.ok(authResponse);
        } catch (ResponseStatusException e) {
            System.err.println("AuthController: Token yenileme hatası (ResponseStatusException): " + e.getReason());
            throw e;
        } catch (Exception e) {
            System.err.println("AuthController: Beklenmeyen token yenileme hatası: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token yenileme sırasında bir hata oluştu");
        }
    }
    // Öğretmen Kayıt Endpoint'i
    // HTTP POST isteği ile "/api/auth/register/teacher" adresine yapılan istekleri karşılar
    @PostMapping("/register/teacher")
    public ResponseEntity<UserResponse> registerTeacher(@Valid @RequestBody TeacherRegistrationRequest registrationRequest) {
        System.out.println("AuthController: Öğretmen kayıt isteği alındı - Email: " + registrationRequest.getEmail());

        // Service katmanındaki öğretmen kayıt iş mantığını çağır
        User registeredTeacher = authenticationService.registerTeacher(registrationRequest);

        // Kayıt başarılıysa 201 Created (Oluşturuldu) yanıtı döndür
        UserResponse userResponse = new UserResponse(registeredTeacher);

        System.out.println("AuthController: Öğretmen kayıt başarılı - Kullanıcı ID: " + registeredTeacher.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

     // Kullanıcı Giriş Endpoint'i
    @PostMapping("/login")
    // ResponseEntity<?> kalabilir veya AuthResponseDto olarak güncellendi.
    public ResponseEntity<AuthResponseDto> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("AuthController: Giriş isteği alındı - Kullanıcı adı/Email: " + loginRequest.getUsernameOrEmail());

        // Service katmanındaki kimlik doğrulama iş mantığını çağır
        // Service AuthResponseDto döndürür veya exception fırlatır (BadCredentialsException, UserNotFoundException, UserNotEnabledException gibi).
        // Exception'lar GlobalExceptionHandler (veya Spring Security default handler'lar) tarafından yakalanacak.
        AuthResponseDto authResponse = authenticationService.authenticateUser(loginRequest);

        System.out.println("AuthController: Kimlik doğrulama başarılı. JWT döndürülüyor.");
        // Başarılı giriş durumunda 200 OK yanıtı ve AuthResponseDto'yu döndür
        return ResponseEntity.ok(authResponse);
    }

    // --- E-posta Doğrulama Endpoint'i ---
    // Maildeki linke tıklanınca çağrılacak (GET isteği)
    // URL formatı: /api/auth/verify-email?token=OLUSTURULAN_TOKEN
    @GetMapping("/verify-email") // Endpoint adı verify-email veya confirm-email olabilir, karar sizin ve SecurityConfig ile uyumlu olmalı
    // Dönüş tipi ResponseEntity<String> olarak güncellendi
    public ResponseEntity<String> verifyUserEmail(@RequestParam("token") String token) { // URL'den 'token' parametresini alır
       System.out.println("AuthController: E-posta doğrulama isteği alındı - Token: " + token);

       // Service katmanındaki doğrulama iş mantığını çağır
       // Service User objesi döndürür veya exception fırlatır (InvalidTokenException, ExpiredTokenException, UserAlreadyEnabledException gibi).
       // Exception'lar GlobalExceptionHandler tarafından yakalanacak.
       User activatedUser = authenticationService.confirmEmail(token); // <-- Service artık User döndürüyor

       System.out.println("AuthController: Kullanıcı başarıyla etkinleştirildi - Kullanıcı Adı: " + activatedUser.getUsername());

       // Başarılı durumda kullanıcıya bilgi verilebilir veya bir yönlendirme yapılabilir
       // Genellikle kullanıcıya "Hesabınız başarıyla etkinleştirildi" gibi basit bir mesaj gösterilir.
       // Frontend bir başarı mesajı gösterebilir veya login sayfasına yönlendirebilir.
       // Şimdilik basit bir String mesaj döndürelim.
       // HTTP 200 OK dönecek.
       return ResponseEntity.ok("Hesabınız başarıyla etkinleştirildi. Artık giriş yapabilirsiniz.");
    }

    // --- Parola Sıfırlama Akışı Endpoint'leri ---

    // Parola Sıfırlama İsteği Endpoint'i
    // İstemciden sadece e-posta adresini POST olarak alır.
    // HTTP POST isteği ile "/api/auth/forgot-password" adresine yapılır.
    @PostMapping("/forgot-password")
    // Parametre EmailRequestDto olarak değişti ve @Valid eklendi
    // Dönüş tipi ResponseEntity<String> olarak güncellendi
    public ResponseEntity<String> initiatePasswordReset(@Valid @RequestBody EmailRequestDto emailRequestDto) {
        System.out.println("AuthController: Parola sıfırlama isteği alındı - Email: " + emailRequestDto.getEmail());

        // Service katmanındaki parola sıfırlama başlatma iş mantığını çağır
        // Service void döndürür veya exception fırlatır (Mail gönderme hatası gibi).
        // UserNotFoundException'ı güvenlik amacıyla Controller içinde yakalamaya devam ediyoruz
        // Diğer Exception'lar (RuntimeException dahil) GlobalExceptionHandler tarafından yakalanacak.
        try {
            authenticationService.forgotPassword(emailRequestDto.getEmail()); // <-- Service artık void döndürüyor

             System.out.println("AuthController: Parola sıfırlama isteği işlendi. Generic başarı yanıtı dönülüyor.");
             // Güvenlik nedeniyle, kullanıcı bulunsa da bulunmasa da aynı generic mesajı dönmek en iyi practice'tir.
             // Bu, e-posta adreslerinin sistemde kayıtlı olup olmadığına dair bilgi sızdırmayı önler.
             return ResponseEntity.ok("Parola sıfırlama talimatları e-posta adresinize gönderildi (varsa)."); // Mesajı güncelledim

        } catch (UserNotFoundException e) {
            // Kullanıcı bulunamadığında bile güvenlik için aynı generic mesajı dön
            System.err.println("AuthController: Parola sıfırlama isteği için kullanıcı bulunamadı: " + e.getMessage());
            System.out.println("AuthController: Güvenlik nedeniyle generic başarı yanıtı dönülüyor.");
            // Yine de 200 OK dönüyoruz ve aynı generic mesajı veriyoruz.
            return ResponseEntity.ok("Parola sıfırlama talimatları e-posta adresinize gönderildi (varsa)."); // Mesajı güncelledim
        }
    }

    // Parola Sıfırlama Tamamlama Endpoint'i
    // İstemciden token ve yeni parolayı bir DTO içinde POST olarak alır.
    // HTTP POST isteği ile "/api/auth/reset-password" adresine yapılır.
    @PostMapping("/reset-password")
    // Parametre ResetPasswordRequest DTO olarak değişti ve @Valid eklendi
    // Dönüş tipi ResponseEntity<String> olarak güncellendi
    public ResponseEntity<String> completePasswordReset(@Valid @RequestBody ResetPasswordRequest resetRequest) {
       System.out.println("AuthController: Parola sıfırlama tamamlama isteği alındı - Token: " + resetRequest.getToken());

       // Service katmanındaki parola sıfırlama tamamlama iş mantığını çağır
       // Service User objesi döndürür veya exception fırlatır (InvalidTokenException, ExpiredTokenException gibi).
       // Exception'lar GlobalExceptionHandler tarafından yakalanacak.
       User updatedUser = authenticationService.resetPassword( // <-- Service artık User döndürüyor
           resetRequest.getToken(), // DTO'dan token'ı al
           resetRequest.getNewPassword() // DTO'dan yeni parolayı al
       );

       System.out.println("AuthController: Parola başarıyla sıfırlandı - Kullanıcı Adı: " + updatedUser.getUsername());

       // Başarılı durumda 200 OK yanıtı ve başarı mesajı döndür
       // Service başarılı olursa burası çalışır. Service hata fırlatırsa GlobalHandler yakalar.
       return ResponseEntity.ok("Parolanız başarıyla sıfırlandı. Artık giriş yapabilirsiniz.");
    }

    // --- Diğer Endpoint'ler ---
    // Güvenlik yapısı kurulduktan sonra user detaylarını getirme vb. endpoint'ler eklenebilir.

    @PostMapping("/logout")
public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            if (username != null) {
                userRepository.findByUsername(username).ifPresent(user -> {
                    user.setRefreshToken(null);
                    userRepository.save(user);
                    System.out.println("User logged out successfully - Username: " + username);
                });
            }
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            // Continue with the response even if there was an error
        }
    }
    
    return ResponseEntity.ok().body(Map.of("message", "Successfully logged out"));
}
}
