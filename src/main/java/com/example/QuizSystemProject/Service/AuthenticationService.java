package com.example.QuizSystemProject.Service;

import com.example.QuizSystemProject.dto.AuthResponseDto;
import com.example.QuizSystemProject.dto.LoginRequest;
import com.example.QuizSystemProject.dto.UserRegistrationRequest;
import com.example.QuizSystemProject.exception.ExpiredTokenException;
import com.example.QuizSystemProject.exception.InvalidTokenException;
import com.example.QuizSystemProject.exception.UserNotFoundException;
import com.example.QuizSystemProject.Model.Student;
import com.example.QuizSystemProject.Model.Teacher;
import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.security.jwt.JwtUtil; // JWT importu
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // application.properties'ten değer okumak için
import org.springframework.security.authentication.AuthenticationManager; // Spring Security Kimlik Doğrulama Yöneticisi
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Kimlik doğrulama için token
import org.springframework.security.core.Authentication; // Kimlik doğrulama objesi
import org.springframework.security.core.userdetails.UserDetails; // Kullanıcı detayları
import org.springframework.security.crypto.password.PasswordEncoder; // Parola şifreleyici
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Transaction yönetimi
import com.example.QuizSystemProject.exception.DuplicateUsernameException;
import com.example.QuizSystemProject.exception.DuplicateEmailException;
import com.example.QuizSystemProject.exception.UserNotAuthorizedException; // Kullanıcı yetkisiz hatası
import com.example.QuizSystemProject.exception.UserAlreadyEnabledException; // Hesap zaten etkin hatası
import com.example.QuizSystemProject.exception.QuizSessionNotFoundException; // QuizSessionNotFoundException (Daha önceki istatistik implementasyonunda kullanıldı)
import com.example.QuizSystemProject.exception.QuizNotFoundException; // QuizNotFoundException (Daha önceki istatistik implementasyonunda kullanıldı)


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.temporal.ChronoUnit; // Zaman birimleri için

// MailService importu ve MailException importu
import com.example.QuizSystemProject.Service.MailService;
import org.springframework.mail.MailException;


@Service
@Transactional // Sınıf seviyesinde transaction yönetimi - Tüm public metotlar transactional olur
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService; // MailService inject edildi

    private final AuthenticationManager authenticationManager; // Spring Security'den inject edilecek
    private final JwtUtil jwtUtil; // JWT util inject edilecek

    // Doğrulama token'ının geçerlilik süresi (örn: 24 saat)
    private final long CONFIRMATION_TOKEN_EXPIRY_HOURS = 24; // Doğrulama token süresi için sabit

    // Parola sıfırlama token'ının geçerlilik süresi (örn: 1 saat)
    private final long RESET_TOKEN_EXPIRY_MINUTES = 60; // Parola sıfırlama token süresi için sabit

    // Uygulama baz URL'si (maildeki linkleri oluşturmak için)
    // application.properties'ten okunabilir veya sabit tanımlanabilir.
    // @Value("${app.base.url}") // application.properties'e ekleyebilirsiniz: app.base.url=http://localhost:8080
    private String appBaseUrl = "http://localhost:8080"; // todo Şimdilik sabit veya application.properties'ten okuyabilirsiniz.


    @Autowired
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, MailService mailService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
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

        // Yeni User objesi oluşturma
        User newUser = new User();
        newUser.setUsername(registrationRequest.getUsername());
        // Parolayı şifreleme ve set etme
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setName(registrationRequest.getName());
        newUser.setSurname(registrationRequest.getSurname());
        newUser.setEmail(registrationRequest.getEmail());
        newUser.setAge(registrationRequest.getAge());
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

        // Kullanıcı zaten etkinleştirilmiş mi kontrolü
        if (user.isEnabled()) {
             System.out.println("AuthenticationService: Kullanıcı zaten etkinleştirilmiş - Kullanıcı Adı: " + user.getUsername());
             // Zaten aktifse hata fırlatılır. GlobalExceptionHandler yakalayacak.
             throw new UserAlreadyEnabledException("Hesap zaten etkinleştirilmiş.");
        }

        // Token süresinin dolup dolmadığını kontrol et
        if (user.getConfirmationTokenExpiryDate() == null || user.getConfirmationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            System.err.println("AuthenticationService: E-posta doğrulama başarısız - Token süresi dolmuş - Kullanıcı: " + user.getUsername());
            throw new ExpiredTokenException("Doğrulama token'ının süresi dolmuş.");
        }

        // Kullanıcıyı etkinleştir
        user.setEnabled(true);
        System.out.println("AuthenticationService: Kullanıcı etkinleştirildi - Kullanıcı Adı: " + user.getUsername());

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


        // --- MailService kullanarak parola sıfırlama maili gönderme ---
        // TODO: Gerçek frontend parola sıfırlama sayfasının URL'sini buraya yazın!
        // Örneğin: http://localhost:3000/reset-password?token=OLUSTURULAN_TOKEN
        
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

    // TODO: AuthenticationService'de JWT doğrulaması, parola güncelleme (giriş yapmış kullanıcı için) gibi diğer metotlar buraya eklenebilir.
    // Ancak şu anki temel akış için yukarıdaki metotlar yeterlidir.

}
