package com.example.QuizSystemProject.Service;

import com.example.QuizSystemProject.dto.AuthResponseDto;
import com.example.QuizSystemProject.dto.LoginRequest;
import com.example.QuizSystemProject.dto.UserRegistrationRequest;
import com.example.QuizSystemProject.dto.TeacherRegistrationRequest;
import com.example.QuizSystemProject.exception.ExpiredTokenException;
import com.example.QuizSystemProject.exception.InvalidTokenException;
import com.example.QuizSystemProject.exception.UserNotFoundException;
import com.example.QuizSystemProject.Model.Student;
import com.example.QuizSystemProject.Model.Teacher;
import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.security.jwt.JwtUtil;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.QuizSystemProject.exception.DuplicateUsernameException;
import com.example.QuizSystemProject.exception.DuplicateEmailException;
import com.example.QuizSystemProject.exception.UserNotAuthorizedException;
import com.example.QuizSystemProject.exception.UserAlreadyEnabledException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;

@Service
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final long CONFIRMATION_TOKEN_EXPIRY_HOURS = 24;
    private final long RESET_TOKEN_EXPIRY_MINUTES = 60;
    private final String appBaseUrl = "http://localhost:8080";

    public AuthenticationService(UserRepository userRepository, 
                               PasswordEncoder passwordEncoder, 
                               MailService mailService, 
                               AuthenticationManager authenticationManager, 
                               JwtUtil jwtUtil, 
                               UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // --- Kullanıcı Kayıt (Register) ---
    @Transactional // Bu metodun transaction içinde çalışmasını sağlar
    public User registerUser(UserRegistrationRequest registrationRequest) {
        System.out.println("AuthenticationService: Kullanıcı kayıt işlemi başlatıldı - Username: " + registrationRequest.getUsername());

        // Kullanıcı adı ve e-posta benzersizlik kontrolü
        if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            System.out.println("AuthenticationService: Kayıt başarısız - Kullanıcı adı zaten mevcut.");
            throw new DuplicateUsernameException("Kullanıcı adı zaten kullanımda");
        }
         
        
        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            System.out.println("AuthenticationService: Kayıt başarısız - Email adresi zaten mevcut.");
            throw new DuplicateEmailException("Email adresi zaten kullanımda");
        }

        // Yeni Student objesi oluşturma (JPA kalıtımı için)
        Student newUser = new Student();
        newUser.setUsername(registrationRequest.getUsername());
        // Parolayı şifreleme ve set etme
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setName(registrationRequest.getName());
        newUser.setSurname(registrationRequest.getSurname());
        newUser.setEmail(registrationRequest.getEmail());
        newUser.setAge(registrationRequest.getAge());
        newUser.setSchoolName(registrationRequest.getSchoolName()); // Okul ismini set et
        newUser.setRole("ROLE_STUDENT"); // Varsayılan rol STUDENT
        newUser.setActive(true); // Varsayılan olarak aktif (Silinmemiş)
        newUser.setEnabled(false); // Başlangıçta etkin değil (E-posta Doğrulama bekleniyor)

        newUser.setCreatedDate(LocalDateTime.now());
        newUser.setUpdatedDate(LocalDateTime.now());

        // --- E-posta Doğrulama Token'ı Oluştur ve Set Et ---
        String confirmationToken = UUID.randomUUID().toString(); // Benzersiz token oluştur
        newUser.setConfirmationToken(confirmationToken);
        // Token'ın son kullanma tarihi (CONFIRMATION_TOKEN_EXPIRY_HOURS saat sonra)
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(CONFIRMATION_TOKEN_EXPIRY_HOURS);
        newUser.setConfirmationTokenExpiryDate(expiryDate);

        System.out.println("AuthenticationService: E-posta doğrulama token'ı oluşturuldu - Token: " + confirmationToken);
        System.out.println("AuthenticationService: Token geçerlilik tarihi: " + expiryDate);

        // Kullanıcıyı token bilgileriyle kaydet
        User savedUser = userRepository.save(newUser);
        System.out.println("AuthenticationService: Kullanıcı başarıyla kaydedildi - ID: " + savedUser.getId());

        // --- Doğrulama Maili Gönderme ---
        // Doğrulama linki oluşturuluyor (appBaseUrl + Controller endpoint'i + token)
        String confirmationLink = appBaseUrl + "/api/auth/verify-email?token=" + confirmationToken; // <-- Gerçek API endpoint'inizi ve uygulama adresini kontrol edin

        System.out.println("AuthenticationService: E-posta doğrulama maili gönderiliyor...");
        try {
            mailService.sendVerificationMail(savedUser.getEmail(), confirmationLink); // MailService çağrısı
            System.out.println("AuthenticationService: E-posta doğrulama maili gönderme isteği başarılı. Link: " + confirmationLink);
        } catch (MailException e) {
            System.err.println("AuthenticationService: E-posta doğrulama maili gönderilemedi: " + e.getMessage());
            // Mail gönderme hatası durumunda transaction geri alınır (RuntimeException fırlatıldığı için).
            // Kullanıcı kaydı da gerçekleşmez. Farklı bir strateji istenirse burası değiştirilebilir.
             throw new RuntimeException("Kayıt başarılı ancak doğrulama maili gönderilemedi: " + e.getMessage());
        }


        return savedUser; // Kaydedilen User objesini döndür
    }
    
    // --- Öğretmen Kayıt (Teacher Register) ---
    @Transactional
    public User registerTeacher(TeacherRegistrationRequest registrationRequest) {
        System.out.println("AuthenticationService: Öğretmen kayıt işlemi başlatıldı - Email: " + registrationRequest.getEmail());

        // E-posta benzersizlik kontrolü
        
        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            System.out.println("AuthenticationService: Öğretmen kayıt başarısız - Email adresi zaten mevcut.");
            throw new DuplicateEmailException("Email adresi zaten kullanımda");
        }

        // Yeni Teacher objesi oluşturma
        Teacher newTeacher = new Teacher();
        // Email'i kullanıcı adı olarak kullan
        newTeacher.setUsername(registrationRequest.getEmail());
        newTeacher.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newTeacher.setName(registrationRequest.getName());
        newTeacher.setSurname(registrationRequest.getSurname());
        newTeacher.setEmail(registrationRequest.getEmail());
        newTeacher.setAge(registrationRequest.getAge());
        newTeacher.setRole("ROLE_TEACHER"); // Öğretmen rolü
        
        // Öğretmen-spesifik alanları ayarla
        newTeacher.setSubject(registrationRequest.getSubject());
        newTeacher.setGraduateSchool(registrationRequest.getGraduateSchool());
        newTeacher.setDiplomaNumber(registrationRequest.getDiplomaNumber());
        
        newTeacher.setCreatedDate(LocalDateTime.now());
        newTeacher.setUpdatedDate(LocalDateTime.now());
        newTeacher.setActive(true);
        newTeacher.setEnabled(false); // ÖNEMLİ: Öğretmen admin onayına kadar etkin değil

        // --- E-posta Doğrulama Token'ı Oluştur ve Set Et ---
        String confirmationToken = UUID.randomUUID().toString(); // Benzersiz token oluştur
        newTeacher.setConfirmationToken(confirmationToken);
        // Token'ın son kullanma tarihi (CONFIRMATION_TOKEN_EXPIRY_HOURS saat sonra)
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(CONFIRMATION_TOKEN_EXPIRY_HOURS);
        newTeacher.setConfirmationTokenExpiryDate(expiryDate);

        System.out.println("AuthenticationService: Öğretmen için e-posta doğrulama token'ı oluşturuldu - Token: " + confirmationToken);

        // Öğretmeni kaydet
        User savedTeacher = userRepository.save(newTeacher);
        System.out.println("AuthenticationService: Öğretmen başarıyla kaydedildi - ID: " + savedTeacher.getId());

        // --- Bekleyen inceleme maili gönderme ---
        String emailSubject = "Öğretmenlik Başvurunuz Alındı";
        String emailBody = "Sayın " + savedTeacher.getName() + " " + savedTeacher.getSurname() + ",\n\n" +
                         "Öğretmenlik başvurunuz başarıyla alınmıştır. Başvurunuz admin tarafından incelendikten sonra" +
                         " sizinle iletişime geçilecektir.\n\n" +
                         "Saygılarımızla,\nQuizland Ekibi";

        try {
            mailService.sendEmail(savedTeacher.getEmail(), emailSubject, emailBody);
            System.out.println("AuthenticationService: Bekleyen inceleme maili başarıyla gönderildi.");
        } catch (MailException e) {
            System.err.println("AuthenticationService: Bekleyen inceleme maili gönderilemedi: " + e.getMessage());
            // Mail gönderme hatası durumunda kayıt yine de başarılı sayılır
        }

        return savedTeacher;
    }

    // --- Kullanıcı Giriş (Login) ---
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public AuthResponseDto authenticateUser(LoginRequest loginRequest) {
        System.out.println("AuthenticationService: Kullanıcı giriş denemesi - Username/Email: " + loginRequest.getUsernameOrEmail());

        // Spring Security AuthenticationManager kullanarak kimlik doğrulama
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(), // username veya email olabilir (CustomUserDetailsService'de işlenmeli)
                        loginRequest.getPassword()
                )
        );

        System.out.println("AuthenticationService: Kimlik doğrulama başarılı.");

        // Kimlik doğrulama başarılı olsa bile, kullanıcının etkin (enabled) olup olmadığını kontrol etmeliyiz.
        // Eğer e-posta doğrulama aktifse, doğrulanmamış kullanıcıların girişini engellemeliyiz.
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // UserDetails'teki username ile kullanıcıyı veritabanından bul (Auth sonrası)
        User user = userRepository.findByUsername(userDetails.getUsername()) 
                                .orElseThrow(() -> new RuntimeException("Kimlik doğrulama sonrası kullanıcı veritabanında bulunamadı.")); // Bu hata olmamalı teorik olarak


        // Kullanıcı etkin değilse giriş izni verme
        if (!user.isEnabled()) {
             System.out.println("AuthenticationService: Giriş başarısız - Kullanıcı etkin değil - Kullanıcı Adı: " + user.getUsername());
             // Etkin olmayan kullanıcılar için özel bir Exception fırlatılır. GlobalExceptionHandler yakalayacak.
             throw new UserNotAuthorizedException("Hesabınız henüz etkinleştirilmemiştir. Lütfen e-posta adresinizi doğrulayın.");
        }


        // Kullanıcı etkinse JWT oluştur
        String jwt = jwtUtil.generateToken(userDetails);
        System.out.println("AuthenticationService: JWT oluşturuldu.");

        // AuthResponseDto oluştur ve döndür
        AuthResponseDto authResponse = new AuthResponseDto(
                user.getId(),
                user.getUsername(),
                List.of(user.getRole()), // Kullanıcının rolünü bir String listesi olarak döndürülüyor (Spring Security formatında değil, kendi DTO formatımızda)
                jwt
        );

        System.out.println("AuthenticationService: Giriş başarılı. AuthResponseDto döndürülüyor.");
        return authResponse;
    }


    // --- E-posta Doğrulama ---
    @Transactional // Kullanıcıyı güncelleyeceği için transaction gerekli
    public User confirmEmail(String confirmationToken) { // Aktif edilen User'ı döndürür
        System.out.println("AuthenticationService: E-posta doğrulama başlatıldı - Token: " + confirmationToken);

        // Token ile kullanıcıyı bul
        
        User user = userRepository.findByConfirmationToken(confirmationToken)
                                 .orElseThrow(() -> {
                                     System.err.println("AuthenticationService: E-posta doğrulama başarısız - Geçersiz token: " + confirmationToken);
                                     return new InvalidTokenException("Geçersiz doğrulama token'ı."); // Mesajı daha net yapalım
                                 });

        System.out.println("AuthenticationService: Kullanıcı token ile bulundu - Kullanıcı Adı: " + user.getUsername());

        // E-posta değişikliği için kullanılıyorsa, hesap zaten etkinleştirilmiş olsa bile devam et
        boolean isEmailChange = user.getPendingEmail() != null && !user.getPendingEmail().isEmpty();
        
        // Kullanıcı zaten etkinleştirilmiş mi kontrolü - sadece yeni hesap doğrulaması için kontrol et
        if (user.isEnabled() && !isEmailChange) {
             System.out.println("AuthenticationService: Kullanıcı zaten etkinleştirilmiş - Kullanıcı Adı: " + user.getUsername());
             // Zaten aktifse VE email değişikliği değilse hata fırlatılır.
             throw new UserAlreadyEnabledException("Hesap zaten etkinleştirilmiş.");
        }
        
        if (user.isEnabled() && isEmailChange) {
             System.out.println("AuthenticationService: Etkinleştirilmiş kullanıcı için e-posta değişikliği doğrulaması - Kullanıcı Adı: " + user.getUsername());
        }

        // Token süresinin dolup dolmadığını kontrol et
        if (user.getConfirmationTokenExpiryDate() == null || user.getConfirmationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            System.err.println("AuthenticationService: E-posta doğrulama başarısız - Token süresi dolmuş - Kullanıcı: " + user.getUsername());
            throw new ExpiredTokenException("Doğrulama token'ının süresi dolmuş.");
        }

        // E-posta değişikliği için token kullanılıyorsa, bekleyen e-postayı uygula
        if (user.getPendingEmail() != null && !user.getPendingEmail().isEmpty()) {
            String oldEmail = user.getEmail();
            String newEmail = user.getPendingEmail();
            
            // E-posta adresini güncelle
            user.setEmail(newEmail);
            user.setPendingEmail(null); // Bekleyen e-postayı temizle
            
            System.out.println("AuthenticationService: Kullanıcının e-posta adresi güncellendi - Kullanıcı: " 
                + user.getUsername() + ", Eski E-posta: " + oldEmail + ", Yeni E-posta: " + newEmail);
        } 

        // Kullanıcıyı etkinleştir (yeni kayıt doğrulaması ise)
        if (!user.isEnabled()) {
            user.setEnabled(true);
            System.out.println("AuthenticationService: Kullanıcı etkinleştirildi - Kullanıcı Adı: " + user.getUsername());
        }

        // Token bilgilerini temizle (Token tek kullanımlık olmalı)
        user.setConfirmationToken(null);
        user.setConfirmationTokenExpiryDate(null);
        System.out.println("AuthenticationService: Token bilgileri temizlendi.");

        // Güncellenmiş kullanıcıyı kaydet
        userRepository.save(user);
        System.out.println("AuthenticationService: Kullanıcı bilgileri veritabanına kaydedildi.");

        return user; // Aktif edilen kullanıcıyı döndür
    }

    // --- Parola Sıfırlama İsteği ---
    // Kullanıcı 'parolamı unuttum' dediğinde çağrılır.
    @Transactional // Token oluşturma ve kaydetme transaction gerektirir
    public void forgotPassword(String email) { // Hata durumunda exception fırlatır
        System.out.println("AuthenticationService: Parola sıfırlama isteği alındı - Email: " + email);

        // 1. E-posta adresine göre kullanıcıyı bul
        
        User user = userRepository.findByEmail(email)
                                 .orElseThrow(() -> {
                                     System.err.println("AuthenticationService: Parola sıfırlama isteği başarısız - Email bulunamadı: " + email);
                                     // Güvenlik notu: Üretimde UserNotFoundException yerine,
                                     // bu e-posta adresiyle ilişkili bir hesap bulup bulmadığına bakılmaksızın
                                     // her zaman "Parola sıfırlama talimatları e-posta adresinize gönderildi" gibi
                                     // generic bir mesaj göstermek yaygın practice'tir.
                                     // Şu an Controller bu hatayı yakalayıp generic mesaj dönüyor. Service hata fırlatabilir.
                                     return new UserNotFoundException("Email adresi ile kullanıcı bulunamadı: " + email);
                                 });

        System.out.println("AuthenticationService: Parola sıfırlama için kullanıcı bulundu - Kullanıcı Adı: " + user.getUsername());


        // 2. Parola sıfırlama token'ı oluştur
        String resetPasswordToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetPasswordToken);

        // 3. Token'ın geçerlilik süresini belirle (RESET_TOKEN_EXPIRY_MINUTES dakika sonra)
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES); // Sabit kullanıldı
        user.setResetPasswordTokenExpiryDate(expiryDate);

        System.out.println("AuthenticationService: Parola sıfırlama token'ı oluşturuldu - Token: " + resetPasswordToken);
        System.out.println("AuthenticationService: Token geçerlilik tarihi: " + expiryDate);


        // 4. Kullanıcıyı güncellenmiş token bilgileriyle kaydet
        userRepository.save(user);
        System.out.println("AuthenticationService: Kullanıcı token bilgileriyle kaydedildi.");


       
        String frontendResetUrl = "http://localhost:3000/reset-password"; // <-- Frontend Parola Sıfırlama URL'si (Controller'da da sabit tanımlanabilir)
        String resetLink = frontendResetUrl + "?token=" + resetPasswordToken; // Parola sıfırlama linki

        System.out.println("AuthenticationService: Parola sıfırlama maili gönderiliyor...");
        try {
            mailService.sendPasswordResetMail(user.getEmail(), resetLink); // MailService çağrısı
             System.out.println("AuthenticationService: Parola sıfırlama maili gönderme isteği başarılı. Link: " + resetLink);
        } catch (MailException e) {
            System.err.println("AuthenticationService: Parola sıfırlama maili gönderilemedi: " + e.getMessage());
            // Mail gönderme hatası durumunda transaction geri alınır (RuntimeException fırlatıldığı için).
            // Token kaydedilmez. Farklı bir strateji istenirse burası değiştirilebilir.
             throw new RuntimeException("Parola sıfırlama talebi alındı ancak mail gönderilemedi: " + e.getMessage());
        }

        // void döndüğü için burada return true gerekmez, exception fırlatılmazsa başarılı demektir.
        System.out.println("AuthenticationService: Parola sıfırlama isteği işlendi (mail gönderimi denendi).");
    }

    // --- Parola Sıfırlama (Token ile) ---
    // Kullanıcının parola sıfırlama linkine tıkladığında ve yeni parolayı gönderdiğinde çağrılır.
    @Transactional // Parolayı güncelleyeceği için transaction gerekli
    public User resetPassword(String resetToken, String newPassword) { // Güncellenen User'ı döndürür
        System.out.println("AuthenticationService: Parola sıfırlama işlemi başlatıldı - Token: " + resetToken);

        // 1. Sıfırlama token'ı ile kullanıcıyı bul
        
        User user = userRepository.findByResetPasswordToken(resetToken)
                                 .orElseThrow(() -> {
                                     System.err.println("AuthenticationService: Parola sıfırlama başarısız - Geçersiz token: " + resetToken);
                                     return new InvalidTokenException("Geçersiz veya süresi dolmuş parola sıfırlama token'ı.");
                                 });

        System.out.println("AuthenticationService: Kullanıcı token ile bulundu - Kullanıcı Adı: " + user.getUsername());

        // 2. Token'ın süresinin dolup dolmadığını kontrol et
        if (user.getResetPasswordTokenExpiryDate() == null || user.getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
            System.err.println("AuthenticationService: Parola sıfırlama başarısız - Token süresi dolmuş - Kullanıcı: " + user.getUsername());
            throw new ExpiredTokenException("Parola sıfırlama token'ının süresi dolmuş.");
        }

        // 3. Yeni parolayı şifrele
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        System.out.println("AuthenticationService: Yeni parola şifrelendi.");


        // 4. Kullanıcının parolasını güncelle
        user.setPassword(encodedNewPassword);
        System.out.println("AuthenticationService: Kullanıcının parolanız güncellendi.");

        // 5. Token ve geçerlilik tarihi alanlarını temizle (Token tek kullanımlık olmalı)
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiryDate(null);
        System.out.println("AuthenticationService: Parola sıfırlama token bilgileri temizlendi.");


        // 6. Güncellenmiş kullanıcıyı kaydet
        User savedUser = userRepository.save(user); // Kaydedilen objeyi al
        System.out.println("AuthenticationService: Kullanıcı bilgileri veritabanına kaydedildi.");


        // --- Kullanıcıya parolasının başarıyla sıfırlandığına dair e-posta gönderme ---
        System.out.println("AuthenticationService: Parola sıfırlama başarı e-postası gönderiliyor...");
        try {
           mailService.sendPasswordResetSuccessMail(savedUser.getEmail()); // MailService'e eklenen metot çağrıldı
           System.out.println("AuthenticationService: Parola sıfırlama başarı maili gönderme isteği başarılı.");
        } catch (MailException e) {
           System.err.println("AuthenticationService: Parola sıfırlama başarı maili gönderilemedi: " + e.getMessage());
           
           
        }


        return savedUser; // Güncellenen kullanıcıyı döndür
    }


    public AuthResponseDto refreshToken(String refreshToken) {
    // Validate the refresh token
    if (!jwtUtil.validateRefreshToken(refreshToken)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Geçersiz refresh token");
    }
    
    // Extract username from refresh token
    String username = jwtUtil.extractUsername(refreshToken);
    
    // Load user details
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    
    // Generate new access token
    String newAccessToken = jwtUtil.generateToken(userDetails);
    
    // Get user details for roles
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));
    
    // Get user role (since User has a single role as String)
    List<String> roles = List.of(user.getRole());
            
    // Return new tokens with user details
    return new AuthResponseDto(user.getId(), username, roles, newAccessToken);
}
    
   

}
