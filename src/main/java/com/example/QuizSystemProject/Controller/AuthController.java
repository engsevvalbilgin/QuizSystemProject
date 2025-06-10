package com.example.QuizSystemProject.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.QuizSystemProject.Model.User;

import com.example.QuizSystemProject.dto.LoginRequest;
import com.example.QuizSystemProject.dto.UserRegistrationRequest;
import com.example.QuizSystemProject.dto.TeacherRegistrationRequest;
import com.example.QuizSystemProject.dto.UserResponse;
import com.example.QuizSystemProject.dto.AuthResponseDto;
import com.example.QuizSystemProject.dto.EmailRequestDto;
import com.example.QuizSystemProject.dto.ResetPasswordRequest;

import com.example.QuizSystemProject.Service.AuthenticationService;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.security.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

import com.example.QuizSystemProject.exception.UserNotFoundException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthenticationService authenticationService,
            JwtUtil jwtUtil,
            UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        System.out.println("AuthController: Kayıt isteği alındı - Kullanıcı adı: " + registrationRequest.getUsername());

        User registeredUser = authenticationService.registerUser(registrationRequest);

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

            AuthResponseDto authResponse = authenticationService.refreshToken(refreshToken);

            if (authResponse == null) {
                System.err.println("AuthController: AuthResponseDto null döndü");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Sunucu hatası");
            }

            System.out.println("AuthController: Token başarıyla yenilendi - Kullanıcı: " +
                    (authResponse.getUsername() != null ? authResponse.getUsername() : "Bilinmeyen") +
                    ", New Refresh Token: "
                    + (authResponse.getRefreshToken() != null ? authResponse.getRefreshToken().substring(0, 10) + "..."
                            : "[none]"));

            return ResponseEntity.ok(authResponse);
        } catch (ResponseStatusException e) {
            System.err.println("AuthController: Token yenileme hatası (ResponseStatusException): " + e.getReason());
            throw e;
        } catch (Exception e) {
            System.err.println("AuthController: Beklenmeyen token yenileme hatası: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Token yenileme sırasında bir hata oluştu");
        }
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<UserResponse> registerTeacher(
            @Valid @RequestBody TeacherRegistrationRequest registrationRequest) {
        System.out.println("AuthController: Öğretmen kayıt isteği alındı - Email: " + registrationRequest.getEmail());

        User registeredTeacher = authenticationService.registerTeacher(registrationRequest);

        UserResponse userResponse = new UserResponse(registeredTeacher);

        System.out.println("AuthController: Öğretmen kayıt başarılı - Kullanıcı ID: " + registeredTeacher.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println(
                "AuthController: Giriş isteği alındı - Kullanıcı adı/Email: " + loginRequest.getUsernameOrEmail());

        AuthResponseDto authResponse = authenticationService.authenticateUser(loginRequest);

        System.out.println("AuthController: Kimlik doğrulama başarılı. JWT döndürülüyor.");
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/verify-email")

    public ResponseEntity<String> verifyUserEmail(@RequestParam("token") String token) { 
                                                                                         
        System.out.println("AuthController: E-posta doğrulama isteği alındı - Token: " + token);

        User activatedUser = authenticationService.confirmEmail(token); 

        System.out.println(
                "AuthController: Kullanıcı başarıyla etkinleştirildi - Kullanıcı Adı: " + activatedUser.getUsername());

        return ResponseEntity.ok("Hesabınız başarıyla etkinleştirildi. Artık giriş yapabilirsiniz.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> initiatePasswordReset(@Valid @RequestBody EmailRequestDto emailRequestDto) {
        System.out.println("AuthController: Parola sıfırlama isteği alındı - Email: " + emailRequestDto.getEmail());

        try {
            authenticationService.forgotPassword(emailRequestDto.getEmail()); 

            System.out.println("AuthController: Parola sıfırlama isteği işlendi. Generic başarı yanıtı dönülüyor.");
            return ResponseEntity.ok("Parola sıfırlama talimatları e-posta adresinize gönderildi (varsa)."); 
                                                                                                            

        } catch (UserNotFoundException e) {
            System.err.println("AuthController: Parola sıfırlama isteği için kullanıcı bulunamadı: " + e.getMessage());
            System.out.println("AuthController: Güvenlik nedeniyle generic başarı yanıtı dönülüyor.");
            return ResponseEntity.ok("Parola sıfırlama talimatları e-posta adresinize gönderildi (varsa)."); 
                                                                                                            
        }
    }

    @PostMapping("/reset-password")

    public ResponseEntity<String> completePasswordReset(@Valid @RequestBody ResetPasswordRequest resetRequest) {
        System.out.println(
                "AuthController: Parola sıfırlama tamamlama isteği alındı - Token: " + resetRequest.getToken());

        User updatedUser = authenticationService.resetPassword(
                resetRequest.getToken(),
                resetRequest.getNewPassword());

        System.out.println("AuthController: Parola başarıyla sıfırlandı - Kullanıcı Adı: " + updatedUser.getUsername());
        return ResponseEntity.ok("Parolanız başarıyla sıfırlandı. Artık giriş yapabilirsiniz.");
    }

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
            }
        }

        return ResponseEntity.ok().body(Map.of("message", "Successfully logged out"));
    }
}
