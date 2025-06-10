package com.example.QuizSystemProject.Service;

import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Model.Student;
import com.example.QuizSystemProject.Model.Teacher;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.security.jwt.JwtUtil;

import io.jsonwebtoken.JwtException;

import com.example.QuizSystemProject.dto.AuthResponseDto;
import com.example.QuizSystemProject.dto.LoginRequest;
import com.example.QuizSystemProject.dto.UserRegistrationRequest;
import com.example.QuizSystemProject.dto.TeacherRegistrationRequest;
import com.example.QuizSystemProject.dto.UserDto;
import com.example.QuizSystemProject.exception.DuplicateUsernameException;
import com.example.QuizSystemProject.exception.DuplicateEmailException;
import com.example.QuizSystemProject.exception.UserNotAuthorizedException;
import com.example.QuizSystemProject.exception.UserAlreadyEnabledException;
import com.example.QuizSystemProject.exception.InvalidTokenException;
import com.example.QuizSystemProject.exception.ExpiredTokenException;
import com.example.QuizSystemProject.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.MailException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.List;

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

    
    @Transactional 
    public User registerUser(UserRegistrationRequest registrationRequest) {
        System.out.println("AuthenticationService: Kullanıcı kayıt işlemi başlatıldı - Username: " + registrationRequest.getUsername());

       
        if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            System.out.println("AuthenticationService: Kayıt başarısız - Kullanıcı adı zaten mevcut.");
            throw new DuplicateUsernameException("Kullanıcı adı zaten kullanımda");
        }
         
        
        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            System.out.println("AuthenticationService: Kayıt başarısız - Email adresi zaten mevcut.");
            throw new DuplicateEmailException("Email adresi zaten kullanımda");
        }

        Student newUser = new Student(
            registrationRequest.getName(),
            registrationRequest.getSurname(),
            registrationRequest.getAge(),
            registrationRequest.getEmail(),
            registrationRequest.getUsername(),
            passwordEncoder.encode(registrationRequest.getPassword()),
            registrationRequest.getSchoolName()
        );
        
        newUser.setActive(true);
        newUser.setEnabled(false); 
        newUser.setCreatedDate(LocalDateTime.now());
        newUser.setUpdatedDate(LocalDateTime.now());

        
        String confirmationToken = UUID.randomUUID().toString(); 
        newUser.setConfirmationToken(confirmationToken);
        
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(CONFIRMATION_TOKEN_EXPIRY_HOURS);
        newUser.setConfirmationTokenExpiryDate(expiryDate);

        System.out.println("AuthenticationService: E-posta doğrulama token'ı oluşturuldu - Token: " + confirmationToken);
        System.out.println("AuthenticationService: Token geçerlilik tarihi: " + expiryDate);

        User savedUser = userRepository.save(newUser);
        System.out.println("AuthenticationService: Kullanıcı başarıyla kaydedildi - ID: " + savedUser.getId());

        
        String confirmationLink = appBaseUrl + "/api/auth/verify-email?token=" + confirmationToken; 

        System.out.println("AuthenticationService: E-posta doğrulama maili gönderiliyor...");
        try {
            mailService.sendVerificationMail(savedUser.getEmail(), confirmationLink);
            System.out.println("AuthenticationService: E-posta doğrulama maili gönderme isteği başarılı. Link: " + confirmationLink);
        } catch (MailException e) {
            System.err.println("AuthenticationService: E-posta doğrulama maili gönderilemedi: " + e.getMessage());
        }


        return savedUser; 
    }
    
    
    @Transactional
    public User registerTeacher(TeacherRegistrationRequest registrationRequest) {
        System.out.println("AuthenticationService: Öğretmen kayıt işlemi başlatıldı - Email: " + registrationRequest.getEmail());

        
        
        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            System.out.println("AuthenticationService: Öğretmen kayıt başarısız - Email adresi zaten mevcut.");
            throw new DuplicateEmailException("Email adresi zaten kullanımda");
        }

        Teacher newTeacher = new Teacher();
        newTeacher.setUsername(registrationRequest.getEmail());
        newTeacher.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newTeacher.setName(registrationRequest.getName());
        newTeacher.setSurname(registrationRequest.getSurname());
        newTeacher.setEmail(registrationRequest.getEmail());
        newTeacher.setAge(registrationRequest.getAge());
        newTeacher.setRole("ROLE_TEACHER"); 
        
        
        newTeacher.setSubject(registrationRequest.getSubject());
        newTeacher.setGraduateSchool(registrationRequest.getGraduateSchool());
        newTeacher.setDiplomaNumber(registrationRequest.getDiplomaNumber());
        
        newTeacher.setCreatedDate(LocalDateTime.now());
        newTeacher.setUpdatedDate(LocalDateTime.now());
        newTeacher.setActive(true);
        newTeacher.setEnabled(false); 

        
        String confirmationToken = UUID.randomUUID().toString(); 
        newTeacher.setConfirmationToken(confirmationToken);
        
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(CONFIRMATION_TOKEN_EXPIRY_HOURS);
        newTeacher.setConfirmationTokenExpiryDate(expiryDate);

        System.out.println("AuthenticationService: Öğretmen için e-posta doğrulama token'ı oluşturuldu - Token: " + confirmationToken);

        
        User savedTeacher = userRepository.save(newTeacher);
        System.out.println("AuthenticationService: Öğretmen başarıyla kaydedildi - ID: " + savedTeacher.getId());

        
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
           
        }

        return savedTeacher;
    }

    
    @Transactional 
    public AuthResponseDto authenticateUser(LoginRequest loginRequest) {
        System.out.println("AuthenticationService: Kullanıcı giriş denemesi - Username/Email: " + loginRequest.getUsernameOrEmail());

        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(), 
                        loginRequest.getPassword()
                )
        );

        System.out.println("AuthenticationService: Kimlik doğrulama başarılı.");

        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        
        User user = userRepository.findByUsername(userDetails.getUsername()) 
                                .orElseThrow(() -> new RuntimeException("Kimlik doğrulama sonrası kullanıcı veritabanında bulunamadı.")); 


        if (!user.isEnabled()) {
             System.out.println("AuthenticationService: Giriş başarısız - Kullanıcı etkin değil - Kullanıcı Adı: " + user.getUsername());
             
             throw new UserNotAuthorizedException("Hesabınız henüz etkinleştirilmemiştir. Lütfen e-posta adresinizi doğrulayın.");
        }


        
        String jwt = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        try {
            
            user.setRefreshToken(refreshToken);
            
            user = userRepository.saveAndFlush(user);
            
            System.out.println("AuthenticationService: JWT ve refresh token oluşturuldu ve veritabanına kaydedildi." +
                "\nKullanıcı ID: " + user.getId() +
                "\nKullanıcı Adı: " + user.getUsername() +
                "\nYeni Refresh Token: " + (refreshToken != null ? refreshToken.substring(0, 10) + "..." : "null") +
                "\nVeritabanındaki Refresh Token: " + (user.getRefreshToken() != null ? user.getRefreshToken().substring(0, 10) + "..." : "null"));
                
        } catch (Exception e) {
            System.err.println("AuthenticationService: Refresh token kaydedilirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Kullanıcı girişi sırasında bir hata oluştu", e);
        }

        AuthResponseDto authResponse = new AuthResponseDto(
    user.getId(),
    user.getUsername(),
    List.of(user.getRole()),
    jwt,
    refreshToken,
    UserDto.fromUser(user) 
);

        System.out.println("AuthenticationService: Giriş başarılı. AuthResponseDto döndürülüyor. Kullanıcı bilgileri eklendi.");
        return authResponse;
    }


    
    @Transactional 
    public User confirmEmail(String confirmationToken) { 
        System.out.println("AuthenticationService: E-posta doğrulama başlatıldı - Token: " + confirmationToken);

        
        
        User user = userRepository.findByConfirmationToken(confirmationToken)
                                 .orElseThrow(() -> {
                                     System.err.println("AuthenticationService: E-posta doğrulama başarısız - Geçersiz token: " + confirmationToken);
                                     return new InvalidTokenException("Geçersiz doğrulama token'ı."); 
                                 });

        System.out.println("AuthenticationService: Kullanıcı token ile bulundu - Kullanıcı Adı: " + user.getUsername());

        
        boolean isEmailChange = user.getPendingEmail() != null && !user.getPendingEmail().isEmpty();
        
        
        if (user.isEnabled() && !isEmailChange) {
             System.out.println("AuthenticationService: Kullanıcı zaten etkinleştirilmiş - Kullanıcı Adı: " + user.getUsername());
             
             throw new UserAlreadyEnabledException("Hesap zaten etkinleştirilmiş.");
        }
        
        if (user.isEnabled() && isEmailChange) {
             System.out.println("AuthenticationService: Etkinleştirilmiş kullanıcı için e-posta değişikliği doğrulaması - Kullanıcı Adı: " + user.getUsername());
        }

        
        if (user.getConfirmationTokenExpiryDate() == null || user.getConfirmationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            System.err.println("AuthenticationService: E-posta doğrulama başarısız - Token süresi dolmuş - Kullanıcı: " + user.getUsername());
            throw new ExpiredTokenException("Doğrulama token'ının süresi dolmuş.");
        }

        
        if (user.getPendingEmail() != null && !user.getPendingEmail().isEmpty()) {
            String oldEmail = user.getEmail();
            String newEmail = user.getPendingEmail();
            
            
            user.setEmail(newEmail);
            user.setPendingEmail(null); 
            
            System.out.println("AuthenticationService: Kullanıcının e-posta adresi güncellendi - Kullanıcı: " 
                + user.getUsername() + ", Eski E-posta: " + oldEmail + ", Yeni E-posta: " + newEmail);
        } 

        
        if (!user.isEnabled()) {
            user.setEnabled(true);
            System.out.println("AuthenticationService: Kullanıcı etkinleştirildi - Kullanıcı Adı: " + user.getUsername());
        }

        
        user.setConfirmationToken(null);
        user.setConfirmationTokenExpiryDate(null);
        System.out.println("AuthenticationService: Token bilgileri temizlendi.");

        
        userRepository.save(user);
        System.out.println("AuthenticationService: Kullanıcı bilgileri veritabanına kaydedildi.");

        return user; 
    }

    
    @Transactional 
    public void forgotPassword(String email) { 
        System.out.println("AuthenticationService: Parola sıfırlama isteği alındı - Email: " + email);

        
        
        User user = userRepository.findByEmail(email)
                                 .orElseThrow(() -> {
                                     System.err.println("AuthenticationService: Parola sıfırlama isteği başarısız - Email bulunamadı: " + email);
                                     
                                     
                                     return new UserNotFoundException("Email adresi ile kullanıcı bulunamadı: " + email);
                                 });

        System.out.println("AuthenticationService: Parola sıfırlama için kullanıcı bulundu - Kullanıcı Adı: " + user.getUsername());


        
        String resetPasswordToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetPasswordToken);

        
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES); 
        user.setResetPasswordTokenExpiryDate(expiryDate);

        System.out.println("AuthenticationService: Parola sıfırlama token'ı oluşturuldu - Token: " + resetPasswordToken);
        System.out.println("AuthenticationService: Token geçerlilik tarihi: " + expiryDate);


        
        userRepository.save(user);
        System.out.println("AuthenticationService: Kullanıcı token bilgileriyle kaydedildi.");


       
        String frontendResetUrl = "http://localhost:3000/reset-password"; 
        String resetLink = frontendResetUrl + "?token=" + resetPasswordToken; 

        System.out.println("AuthenticationService: Parola sıfırlama maili gönderiliyor...");
        try {
            mailService.sendPasswordResetMail(user.getEmail(), resetLink); 
             System.out.println("AuthenticationService: Parola sıfırlama maili gönderme isteği başarılı. Link: " + resetLink);
        } catch (MailException e) {
            System.err.println("AuthenticationService: Parola sıfırlama maili gönderilemedi: " + e.getMessage());
           
             throw new RuntimeException("Parola sıfırlama talebi alındı ancak mail gönderilemedi: " + e.getMessage());
        }

        
        System.out.println("AuthenticationService: Parola sıfırlama isteği işlendi (mail gönderimi denendi).");
    }

    
    @Transactional 
    public User resetPassword(String resetToken, String newPassword) { 
        System.out.println("AuthenticationService: Parola sıfırlama işlemi başlatıldı - Token: " + resetToken);

        
        
        User user = userRepository.findByResetPasswordToken(resetToken)
                                 .orElseThrow(() -> {
                                     System.err.println("AuthenticationService: Parola sıfırlama başarısız - Geçersiz token: " + resetToken);
                                     return new InvalidTokenException("Geçersiz veya süresi dolmuş parola sıfırlama token'ı.");
                                 });

        System.out.println("AuthenticationService: Kullanıcı token ile bulundu - Kullanıcı Adı: " + user.getUsername());

        
        if (user.getResetPasswordTokenExpiryDate() == null || user.getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
            System.err.println("AuthenticationService: Parola sıfırlama başarısız - Token süresi dolmuş - Kullanıcı: " + user.getUsername());
            throw new ExpiredTokenException("Parola sıfırlama token'ının süresi dolmuş.");
        }

        
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        System.out.println("AuthenticationService: Yeni parola şifrelendi.");

        
        user.setPassword(encodedNewPassword);
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiryDate(null);
        
        User savedUser = userRepository.save(user);
        
        try {
            mailService.sendPasswordResetSuccessMail(savedUser.getEmail());
        } catch (MailException e) {
           System.err.println("AuthenticationService: Parola sıfırlama başarı maili gönderilemedi: " + e.getMessage());
        }

        return savedUser;
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDto refreshToken(String refreshToken) {
        System.out.println("AuthenticationService: Token yenileme isteği alındı - Token: " + 
            (refreshToken != null && refreshToken.length() > 10 ? refreshToken.substring(0, 10) + "..." : "[invalid]"));
            
        try {
          
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                System.err.println("AuthenticationService: Boş refresh token");
                throw new JwtException("Refresh token gereklidir");
            }

            
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                System.err.println("AuthenticationService: Geçersiz refresh token formatı veya imzası");
                throw new JwtException("Geçersiz refresh token formatı veya imzası");
            }

            
            String username = jwtUtil.extractUsername(refreshToken);
            if (username == null || username.trim().isEmpty()) {
                System.err.println("AuthenticationService: Refresh token'da kullanıcı adı bulunamadı");
                throw new JwtException("Geçersiz refresh token: Kullanıcı adı bulunamadı");
            }
            
            System.out.println("AuthenticationService: Token'dan çıkarılan kullanıcı adı: " + username);

            
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println("AuthenticationService: Kullanıcı bulunamadı: " + username);
                    return new UsernameNotFoundException("Kullanıcı bulunamadı: " + username);
                });
                
            System.out.println("AuthenticationService: Veritabanından kullanıcı bulundu - ID: " + user.getId() + 
                ", Kullanıcı Adı: " + user.getUsername());
                
            String storedRefreshToken = user.getRefreshToken();
            System.out.println("AuthenticationService: Kullanıcının veritabanındaki refresh token: " + 
                (storedRefreshToken != null ? storedRefreshToken.substring(0, 10) + "..." : "null"));

            
            if (storedRefreshToken == null) {
                System.err.println("AuthenticationService: Kullanıcının kayıtlı refresh token'ı yok - Kullanıcı ID: " + user.getId());
                throw new JwtException("Geçersiz refresh token: Kullanıcı için kayıtlı token bulunamadı");
            }
            
            if (!storedRefreshToken.equals(refreshToken)) {
                System.err.println("AuthenticationService: Token eşleşmiyor - Beklenen: " + 
                    (storedRefreshToken.length() > 10 ? storedRefreshToken.substring(0, 10) + "..." : storedRefreshToken) + 
                    ", Alınan: " + (refreshToken.length() > 10 ? refreshToken.substring(0, 10) + "..." : refreshToken));
                throw new JwtException("Geçersiz refresh token: Token eşleşmiyor");
            }

            
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("AuthenticationService: Kullanıcı detayları yüklendi - Yetkiler: " + userDetails.getAuthorities());

            
            String newAccessToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            System.out.println("AuthenticationService: Yeni token'lar oluşturuldu - " +
                "Access Token: " + (newAccessToken != null ? newAccessToken.substring(0, 10) + "..." : "null") + 
                ", New Refresh Token: " + (newRefreshToken != null ? newRefreshToken.substring(0, 10) + "..." : "null"));

            
            user.setRefreshToken(newRefreshToken);
            user = userRepository.saveAndFlush(user);
            
            System.out.println("AuthenticationService: Yeni refresh token veritabanına kaydedildi - Kullanıcı ID: " + user.getId());

            
            List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            System.out.println("AuthenticationService: Yeni token'lar başarıyla oluşturuldu ve döndürülüyor - Kullanıcı: " + user.getUsername());
            
            AuthResponseDto response = new AuthResponseDto(
    user.getId(),
    user.getUsername(),
    roles,
    newAccessToken,
    newRefreshToken,
    UserDto.fromUser(user) 
);
            
            return response;
        } catch (JwtException | UsernameNotFoundException e) {
            System.err.println("AuthenticationService: Token yenileme hatası: " + e.getMessage());
            throw e; 
        } catch (Exception e) {
            System.err.println("AuthenticationService: Beklenmeyen token yenileme hatası: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Token yenileme sırasında bir hata oluştu: " + e.getMessage(), e);
        }
    }
}