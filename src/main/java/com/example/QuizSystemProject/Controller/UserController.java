package com.example.QuizSystemProject.Controller; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.dto.*; // İleride oluşturulacak tüm ilgili DTO'ları import edin (veya tek tek)
import com.example.QuizSystemProject.Model.User; // User Entity'sini döndürmek için (DTO kullanmak daha iyi olabilir)
import com.example.QuizSystemProject.Service.UserService; // UserService'i import edin
import com.example.QuizSystemProject.Service.MailService; // MailService'i import edin
import jakarta.validation.Valid; // Girdi doğrulama için
import org.springframework.security.crypto.password.PasswordEncoder; // Parola şifreleme için


import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.web.bind.annotation.*; // Web anotasyonları için (@RestController, @RequestMapping, @GetMapping vb.)

import java.time.LocalDateTime; // Tarih/saat için
import java.util.UUID; // UUID oluşturmak için

import java.util.List; // List importu
import java.util.Map;
import java.util.HashMap;
// Optional importu artık doğrudan Controller'da kullanılmıyor (Service tarafından handle ediliyor)
// import java.util.Optional;

// Bu exception importlarına Controller içinde artık yakalamadığımız için gerek kalmadı.
// Ancak Service ve GlobalExceptionHandler'da hala gerekli olacaklardır.
// import com.quizland.QuizSystemProject.exception.UserNotFoundException;
// import com.quizland.QuizSystemProject.exception.DuplicateEmailException;
// import org.springframework.security.authentication.BadCredentialsException;
// import java.lang.IllegalArgumentException; // GlobalHandler tarafından yakalanıyor

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication; // Authentication objesi için
import org.springframework.security.access.prepost.PreAuthorize; // @PreAuthorize için
import java.util.Collections; // Collections.singletonMap için


@RestController // Bu sınıfın bir REST Controller olduğunu belirtir
@RequestMapping("/api/users") // Bu controller altındaki tüm endpoint'lerin "/api/users" ile başlayacağını belirtir
public class UserController {

    private final UserService userService; // Kullanıcı yönetimi iş mantığı servisi
    private final PasswordEncoder passwordEncoder; // Parola şifreleme için
    private final MailService mailService; // Mail servisi

    // Constructor injection
    public UserController(UserService userService, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    // --- Kullanıcı Yönetimi Endpoint'leri (Genellikle Admin yetkisi gerektirir) ---

    // Tüm kullanıcıları listeleme
    // HTTP GET isteği ile "/api/users" adresine yapılan istekleri karşılar
    // (Sadece Admin yetkisi gerektirir - Security ile sağlanacak)
    @GetMapping
    // Dönüş tipi ResponseEntity<List<UserResponse>> olarak değişti
    public ResponseEntity<List<UserResponse>> getAllUsers() {
            System.out.println("UserController: Tüm kullanıcılar listeleniyor.");

            // try-catch bloğu kaldırıldı. Beklenmeyen Exception'lar
            // artık GlobalExceptionHandler tarafından yakalanacak.

            // Service katmanındaki tüm kullanıcıları getiren metodu çağır
            // Service metodu zaten List<UserResponse> döndürüyor.
            List<UserResponse> users = userService.getAllUsers();

            System.out.println("UserController: " + users.size() + " kullanıcı bulundu.");
            // Başarılı durumda 200 OK yanıtı ve DTO listesini döndür
            return ResponseEntity.ok(users);
        }

    // Kullanıcıları role göre listeleme
    // HTTP GET isteği ile "/api/users/role/{roleName}" adresine yapılan istekleri karşılar
    // (Sadece Admin yetkisi gerektirir - Security ile sağlanacak)
    @GetMapping("/role/{roleName}")
    // Dönüş tipi ResponseEntity<List<UserResponse>> olarak değişti
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable("roleName") String roleName) {
            System.out.println("UserController: Rolü '" + roleName + "' olan kullanıcılar listeleniyor.");

            // try-catch blokları kaldırıldı. IllegalArgumentException ve diğer Exception'lar
            // artık GlobalExceptionHandler tarafından yakalanacak.

            // Service katmanındaki role göre kullanıcıları getiren metodu çağır
            // Service metodu rolü doğrular ve List<UserResponse> döndürür veya IllegalArgumentException fırlatır.
            List<UserResponse> users = userService.getUsersByRole(roleName);

            System.out.println("UserController: Rolü '" + roleName + "' olan " + users.size() + " kullanıcı bulundu.");
            // Başarılı durumda 200 OK yanıtı ve DTO listesini döndür
            return ResponseEntity.ok(users);
        }


    // Mevcut kullanıcının profil bilgilerini getirir
    @GetMapping("/profile")
    public ResponseEntity<UserDetailsResponse> getCurrentUserProfile() {
        // SecurityContext'ten mevcut kullanıcının bilgilerini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("UserController: Mevcut kullanıcı profili isteniyor - Kullanıcı: " + username);
        
        // Kullanıcıyı kullanıcı adına göre bul
        UserDetailsResponse userDetailsResponse = userService.getUserByUsername(username);
        
        System.out.println("UserController: Kullanıcı profili bulundu - Kullanıcı: " + username);
        return ResponseEntity.ok(userDetailsResponse);
    }

    // ID'ye göre kullanıcı getirme
    // HTTP GET isteği ile "/api/users/{id}" adresine yapılan istekleri karşılar
    // (Admin veya kullanıcı kendi profilini getirebilir - Security ile sağlanacak)
    @GetMapping("/{id}")
    // Dönüş tipi ResponseEntity<UserDetailsResponse> olarak değişti
    public ResponseEntity<UserDetailsResponse> getUserById(@PathVariable("id") int id) {
        System.out.println("UserController: Kullanıcı detayları isteniyor - ID: " + id);

        // try-catch blokları kaldırıldı. UserNotFoundException ve diğer Exception'lar
        // artık GlobalExceptionHandler tarafından yakalanacak.
        
        // Service katmanındaki kullanıcıyı ID'ye göre getiren metodu çağır
        // Service metodu kullanıcıyı bulursa UserDetailsResponse döndürür, bulamazsa UserNotFoundException fırlatır.
        UserDetailsResponse userDetailsResponse = userService.getUserById(id);

        System.out.println("UserController: Kullanıcı bulundu - ID: " + id);
        // Başarılı durumda 200 OK yanıtı ve UserDetailsResponse DTO'sunu döndür
        return ResponseEntity.ok(userDetailsResponse);
    }

    // Kullanıcı bilgilerini güncelleme
    // HTTP PUT isteği ile "/api/users/{id}" adresine yapılan istekleri karşılar
    // (Admin yetkisi gerektirecek, veya kullanıcı kendi bilgilerini gücelleyebilir - rol, email ve parola hariç - Security ile sağlanacak)
    @PutMapping("/{id}")
    // Dönüş tipi ResponseEntity<UserDetailsResponse> olarak değişti
    public ResponseEntity<UserDetailsResponse> updateUser(@PathVariable("id") int id, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
            System.out.println("UserController: Kullanıcı güncelleniyor - ID: " + id);

        // try-catch blokları kaldırıldı. UserNotFoundException ve diğer Exception'lar
        // artık GlobalExceptionHandler tarafından yakalanacak.

            // Service katmanındaki updateUser metodunu çağır
            // Service metodu kullanıcıyı bulur, günceller, kaydeder ve güncellenmiş User Entity'sini döndürür veya UserNotFoundException fırlatır.
            User updatedUser = userService.updateUser(id, userUpdateRequest);

            // Service'ten dönen User Entity'sini UserDetailsResponse DTO'suna dönüştür
            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(updatedUser);

            System.out.println("UserController: Kullanıcı başarıyla güncellendi - ID: " + updatedUser.getId());
            // Başarılı durumda 200 OK yanıtı ve güncellenmiş kullanıcı DTO'sunu döndür
            return ResponseEntity.ok(userDetailsResponse);
        }
    // Kullanıcı rolünü değiştirme
    // HTTP PUT isteği ile "/api/users/{id}/role" adresine yapılan istekleri karşılar
    // (Sadece Admin yetkisi gerektirecek - Security ile sağlanacak)
    @PutMapping("/{id}/role")
    // Dönüş tipi ResponseEntity<UserDetailsResponse> olarak değişti
    public ResponseEntity<UserDetailsResponse> changeUserRole(@PathVariable("id") int id, @Valid @RequestBody RoleChangeRequest roleChangeRequest) {
            System.out.println("UserController: Kullanıcı rolü değiştiriliyor - ID: " + id + ", Yeni Rol: " + roleChangeRequest.getNewRole());

        // try-catch blokları kaldırıldı. UserNotFoundException, IllegalArgumentException ve diğer Exception'lar
        // artık GlobalExceptionHandler tarafından yakalanacak.

            // Service katmanındaki changeUserRole metodunu çağır
            // Service metodu kullanıcıyı bulur, rolü değiştirir, kaydeder ve güncellenmiş User Entity'sini döndürür veya UserNotFound/IllegalArgumentException fırlatır.
            User updatedUser = userService.changeUserRole(id, roleChangeRequest);

            // Service'ten dönen User Entity'sini UserDetailsResponse DTO'sına dönüştür
            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(updatedUser);

            System.out.println("UserController: Kullanıcı rolü başarıyla değiştirildi - ID: " + updatedUser.getId() + ", Yeni Rol: " + updatedUser.getRole());
            // Başarılı durumda 200 OK yanıtı ve güncellenmiş kullanıcı DTO'sunu döndür
            return ResponseEntity.ok(userDetailsResponse);
        }

    // Kullanıcının parolasını değiştirme
    // HTTP PUT isteği ile "/api/users/{id}/password" adresine yapılan istekleri karşılar
    // (Admin yetkisi gerektirecek veya kullanıcı kendi parolasını değiştirebilir - Security ile sağlanacak)
    @PutMapping("/{id}/password")
    // Dönüş tipi ResponseEntity<UserDetailsResponse> olarak değişti
    public ResponseEntity<UserDetailsResponse> changeUserPassword(@PathVariable("id") int id, @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
            System.out.println("UserController: Kullanıcı parolası değiştiriliyor - ID: " + id);

        // try-catch blokları kaldırıldı. UserNotFoundException, BadCredentialsException ve diğer Exception'lar
        // artık GlobalExceptionHandler tarafından yakalanacak.
        // Not: BadCredentialsException'ı GlobalExceptionHandler'da yakalamak için özel bir handler eklemelisiniz
        // veya RuntimeException handler'ı yakalayacaktır (500 dönebilir, 401 için özel handler önerilir).

            // Spring Security bağlamından şu anki kimliği doğrulanmış kullanıcının bilgilerini al
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // Principal genellikle UserDetails objesidir
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // İstek yapan kullanıcının Admin olup olmadığını belirle
            // UserDetails'in getAuthorities() metodu kullanıcının rollerini (GrantedAuthority olarak) döner.
            boolean isAdminAction = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            System.out.println("UserController: Parola değiştirme isteği - Kullanıcı ID: " + id + ", ADMIN işlemi mi?: " + isAdminAction);


            // Service katmanındaki changeUserPassword metodunu çağır
            // Service metodu kullanıcıyı bulur, parolayı şifreler, günceller, kaydeder ve güncellenmiş User Entity'sini döndürür
            // veya UserNotFound/BadCredentialsException fırlatır.
            User updatedUser = userService.changeUserPassword(
                    id, // Parolası değiştirilecek kullanıcının ID'si (URL'den geldi)
                    passwordChangeRequest, // Mevcut ve yeni parola DTO'su
                    isAdminAction // İsteği yapanın Admin olup olmadığı bilgisi
            );

            // Service'ten dönen User Entity'sini UserDetailsResponse DTO'sına dönüştür
            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(updatedUser);

            System.out.println("UserController: Kullanıcı parolası başarıyla değiştirildi - ID: " + updatedUser.getId());
            // Başarılı durumda 200 OK yanıtı ve güncellenmiş kullanıcı DTO'sunu döndür
            return ResponseEntity.ok(userDetailsResponse);
        }

    // --- Öğretmen Kayıt Taleplerini İnceleme Endpoint'i (Sadece Admin) ---
    @PostMapping("/teachers/{userId}/review")
    // @PreAuthorize("hasRole('ADMIN')") // Güvenlik için (Admin yetkisi gerektirir)
    public ResponseEntity<UserDetailsResponse> reviewTeacherRegistration(
            @PathVariable("userId") int userId,
            @RequestParam("approve") boolean approve) {
        System.out.println("UserController: Öğretmen kayıt talebi inceleniyor - ID: " + userId + ", Onay: " + approve);
        User updatedUser = userService.reviewTeacherRequest(userId, approve);
        UserDetailsResponse responseDto = new UserDetailsResponse(updatedUser);
        System.out.println("UserController: Öğretmen kayıt talebi başarıyla işlendi - ID: " + updatedUser.getId() + ", Onay durumu: " + approve);
        return ResponseEntity.ok(responseDto);
    }

    // --- Bekleyen Öğretmen Kayıt Taleplerini Listeleme Endpoint'i (Sadece Admin) ---
    @GetMapping("/teachers/pending")
    // @PreAuthorize("hasRole('ADMIN')") // Güvenlik için (Admin yetkisi gerektirir)
    public ResponseEntity<List<UserResponse>> getPendingTeacherRegistrations() {
        System.out.println("UserController: Bekleyen öğretmen kayıt talepleri listeleniyor.");
        List<UserResponse> pendingTeachers = userService.getPendingTeacherRequests();
        System.out.println("UserController: " + pendingTeachers.size() + " adet bekleyen öğretmen kaydı bulundu.");
        return ResponseEntity.ok(pendingTeachers);
    }

    // Kullanıcının e-posta adresini değiştirme
    // HTTP PUT isteği ile "/api/users/{id}/email" adresine yapılan istekleri karşılar
    // (Admin yetkisi gerektirecek veya kullanıcı kendi e-postasını değiştirebilir - Security ile sağlanacak)
    @PutMapping("/{id}/email")
    // Dönüş tipi ResponseEntity<UserDetailsResponse> olarak değişti
    public ResponseEntity<UserDetailsResponse> changeUserEmail(@PathVariable("id") int id, @Valid @RequestBody EmailChangeRequest emailChangeRequest) {
            System.out.println("UserController: Kullanıcı e-postası değiştiriliyor - ID: " + id + ", Yeni Email: " + emailChangeRequest.getNewEmail());

        // try-catch blokları kaldırıldı. UserNotFoundException, DuplicateEmailException ve diğer Exception'lar
        // artık GlobalExceptionHandler tarafından yakalanacak.

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            boolean isAdminAction = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            System.out.println("UserController: E-posta değiştirme isteği - Kullanıcı ID: " + id + ", ADMIN işlemi mi?: " + isAdminAction);

            User updatedUser = userService.changeUserEmail(id, emailChangeRequest, isAdminAction);
            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(updatedUser);
            System.out.println("UserController: Kullanıcı e-postası başarıyla değiştirildi - ID: " + updatedUser.getId());
            return ResponseEntity.ok(userDetailsResponse);
    }


// Kullanıcıyı silme (mantıksal silme: isActive = false yapma)
    // HTTP DELETE isteği ile "/api/users/{id}" adresine yapılan istekleri karşılar
    // (Sadece Admin yetkisi gerektirecek - Security ile sağlanacak)
    @DeleteMapping("/{id}")
    // Dönüş tipi ResponseEntity<Void> olarak kalacak
    public ResponseEntity<Void> softDeleteUser(@PathVariable("id") int id) {
            System.out.println("UserController: Kullanıcı siliniyor (mantıksal) - ID: " + id);

            // try-catch blokları kaldırıldı. UserNotFoundException ve diğer Exception'lar
            // artık GlobalExceptionHandler tarafından yakalanacak.

            // Service katmanındaki softDeleteUser metodunu çağır
            // Service metodu kullanıcıyı bulur, isActive = false yapar ve kaydeder veya UserNotFoundException fırlatır.
            userService.softDeleteUser(id);

            System.out.println("UserController: Kullanıcı başarıyla silindi (mantıksal olarak pasif yapıldı) - ID: " + id);
            // Başarılı durumda 204 No Content yanıtı döndür
            return ResponseEntity.noContent().build();
        }

// Öğretmen olma isteğini gözden geçirme
// HTTP PUT isteği ile "/api/users/{id}/review-teacher-request" adresine yapılan istekleri karşılar
// (Sadece Admin yetkisi gerektirecek - Security ile sağlanacak)
@PutMapping("/{id}/review-teacher-request")
// Dönüş tipi ResponseEntity<UserDetailsResponse> olarak değişti
public ResponseEntity<UserDetailsResponse> reviewTeacherRequest(@PathVariable("id") int id, @RequestParam("approve") boolean approve) {
           
            System.out.println("UserController: Öğretmen isteği gözden geçiriliyor - Kullanıcı ID: " + id + ", Onay: " + approve);

            // try-catch blokları kaldırıldı. UserNotFoundException ve diğer Exception'lar
            // artık GlobalExceptionHandler tarafından yakalanacak.
            // NOT: Service'in reviewTeacherRequest metodu boolean dönüyordu, bu yüzden if(success) bloğu kaldı.

            // Service katmanındaki reviewTeacherRequest metodunu çağır
            // Service metodu kullanıcıyı bulur, onaylanırsa rolü değiştirir, kaydeder ve güncellenmiş User objesini döndürür veya UserNotFoundException fırlatır.
            User updatedUser = userService.reviewTeacherRequest(id, approve);

            // User objesini UserDetailsResponse DTO'suna çevir
            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(updatedUser);

            System.out.println("UserController: Öğretmen isteği başarıyla işlendi - Kullanıcı ID: " + id + ", Onay: " + approve);
            // Başarılı durumda 200 OK yanıtı ve güncellenmiş kullanıcı DTO'sunu döndür
            return ResponseEntity.ok(userDetailsResponse);
    }

    // --- Mevcut kullanıcının şifresini değiştirme ---
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        // Mevcut kullanıcıyı al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("UserController: Kullanıcı şifre değiştirme isteği - Kullanıcı: " + username);
        
        try {
            // Kullanıcıyı bul
            User user = userService.findUserByUsername(username);
            
            // Mevcut şifre doğruluğunu kontrol et
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                System.out.println("UserController: Mevcut şifre doğru değil - Kullanıcı: " + username);
                return ResponseEntity.badRequest().body("Mevcut şifre doğru değil.");
            }
            
            // Yeni şifreyi şifrele ve kaydet
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setUpdatedDate(LocalDateTime.now());
            userService.saveUser(user);
            
            System.out.println("UserController: Şifre başarıyla değiştirildi - Kullanıcı: " + username);
            
            // Şifre değişikliği hakkında email gönder
            try {
                mailService.sendPasswordChangeNotification(user.getEmail());
                System.out.println("UserController: Şifre değişikliği bildirimi gönderildi - E-posta: " + user.getEmail());
            } catch (Exception mailEx) {
                // Email gönderimi başarısız olsa bile işleme devam et, sadece log tut
                System.err.println("UserController: Şifre değişikliği bildirimi gönderilemedi - Hata: " + mailEx.getMessage());
            }
            
            return ResponseEntity.ok("Şifreniz başarıyla değiştirildi. E-posta adresinize bir bildirim gönderildi.");
            
        } catch (Exception e) {
            System.err.println("UserController: Şifre değiştirme sırasında hata: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Şifre değiştirme sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
        }
    }
    
    // --- Mevcut kullanıcının emailini değiştirme ---
    @PostMapping("/change-email")
    public ResponseEntity<String> changeEmail(@Valid @RequestBody ChangeEmailRequest request) {
        // Mevcut kullanıcıyı al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("UserController: Kullanıcı email değiştirme isteği - Kullanıcı: " + username);
        
        try {
            // Kullanıcıyı bul
            User user = userService.findUserByUsername(username);
            
            // Şifre doğruluğunu kontrol et
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                System.out.println("UserController: Şifre doğru değil - Kullanıcı: " + username);
                return ResponseEntity.badRequest().body("Şifre doğru değil.");
            }
            
            // Email adresi zaten kullanılıyor mu kontrol et
            if (userService.existsByEmail(request.getNewEmail()) && 
                    !user.getEmail().equals(request.getNewEmail())) {
                System.out.println("UserController: Email adresi zaten kullanımda - Email: " + request.getNewEmail());
                return ResponseEntity.badRequest().body("Bu email adresi zaten kullanımda.");
            }
            
            // Token oluştur ve kaydet
            String token = UUID.randomUUID().toString();
            user.setConfirmationToken(token);
            user.setConfirmationTokenExpiryDate(LocalDateTime.now().plusHours(24));
            
            // Yeni e-posta adresini pendingEmail alanına kaydet
            user.setPendingEmail(request.getNewEmail());
            System.out.println("UserController: Doğrulama bekleyen yeni e-posta adresi kaydedildi: " + request.getNewEmail());
            
            userService.saveUser(user);
            
            // Yeni email adresine doğrulama maili gönder
            try {
                // Frontend base URL - frontend'inizin çalıştığı URL ile güncelleyin
                String baseUrl = "http://localhost:5173";
                mailService.sendEmailChangeVerification(request.getNewEmail(), token, baseUrl);
                System.out.println("UserController: Email değişikliği doğrulama maili gönderildi - E-posta: " + request.getNewEmail());
            } catch (Exception mailEx) {
                // Email gönderimi başarısız olsa bile işleme devam et, sadece log tut
                System.err.println("UserController: Email değişikliği doğrulama maili gönderilemedi - Hata: " + mailEx.getMessage());
            }
            
            System.out.println("UserController: Email değiştirme talebi kabul edildi - Kullanıcı: " + username);
            return ResponseEntity.ok("Email değiştirme talebiniz alındı. Lütfen yeni email adresinize gönderilen doğrulama bağlantısına tıklayın.");
            
        } catch (Exception e) {
            System.err.println("UserController: Email değiştirme sırasında hata: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Email değiştirme sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
        }
    }

    // --- Diğer Endpoint'ler ---
    // Kimlik doğrulama (login, register vb.) AuthController'da.
    // Quiz oturumu yönetimi QuizSessionController'da.
    // İstatistikler StatisticsController'da.
    
    // Şifre sıfırlama talebi endpoint'i
    @PostMapping("/password-reset/request")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        try {
            // E-posta adresine sahip kullanıcıyı bul
            User user = userService.findUserByEmail(requestDto.getEmail());
            if (user == null) {
                // Güvenlik nedeniyle başarılı yanıt döndürürüz,
                // böylece saldırganlar hangi e-postaların sistemde kayıtlı olduğunu öğrenemez
                return ResponseEntity.ok("Eğer bu e-posta adresine sahip bir hesap varsa, şifre sıfırlama talimatları gönderildi.");
            }
            
            // Token oluştur ve kullanıcıya kaydet
            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            user.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(24)); // 24 saat geçerli
            userService.saveUser(user);
            
            // Frontend base URL
            String baseUrl = "http://localhost:5173";
            // Update to use password-reset endpoint, but keep reset-password as alternative endpoint for backward compatibility
            String resetLink = baseUrl + "/password-reset?token=" + token;
            System.out.println("UserController: Oluşturulan şifre sıfırlama linki: " + resetLink);
            
            // Şifre sıfırlama maili gönder
            mailService.sendPasswordResetMail(user.getEmail(), resetLink);
            
            return ResponseEntity.ok("E-posta adresinize şifre sıfırlama talimatları gönderildi.");
            
        } catch (Exception e) {
            System.err.println("UserController: Şifre sıfırlama talebi sırasında hata: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Şifre sıfırlama talebi sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
        }
    }
    
    // Şifre sıfırlama tamamlama endpoint'i
    @PutMapping("/{id}/toggle-activation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> toggleUserActivation(@PathVariable("id") int userId) {
        try {
            User updatedUser = userService.toggleUserActivation(userId);
            String status = updatedUser.isEnabled() ? "aktif" : "pasif";
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Kullanıcı başarıyla " + status + " hale getirildi.");
            response.put("status", updatedUser.isEnabled() ? "active" : "inactive");
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Kullanıcı durumu güncellenirken bir hata oluştu."));
        }
    }
    
    @PostMapping("/password-reset/complete")
    public ResponseEntity<String> completePasswordReset(@Valid @RequestBody PasswordResetCompleteDto resetDto) {
        try {
            // Şifrelerin eşleştiğini kontrol et
            if (!resetDto.getNewPassword().equals(resetDto.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("Şifreler eşleşmiyor.");
            }
            
            // Token'a sahip kullanıcıyı bul
            User user = userService.findUserByResetPasswordToken(resetDto.getToken());
            if (user == null) {
                return ResponseEntity.badRequest().body("Geçersiz veya süresi dolmuş token.");
            }
            
            // Token'in süresinin dolup dolmadığını kontrol et
            if (user.getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Token'in süresi dolmuş. Lütfen yeni bir şifre sıfırlama talebi oluşturun.");
            }
            
            // Yeni şifreyi kaydet ve token'i temizle
            user.setPassword(passwordEncoder.encode(resetDto.getNewPassword()));
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiryDate(null);
            user.setUpdatedDate(LocalDateTime.now());
            userService.saveUser(user);
            
            // Şifre sıfırlama başarı maili gönder
            mailService.sendPasswordResetSuccessMail(user.getEmail());
            
            return ResponseEntity.ok("Şifreniz başarıyla sıfırlandı. Artık yeni şifrenizle giriş yapabilirsiniz.");
            
        } catch (Exception e) {
            System.err.println("UserController: Şifre sıfırlama tamamlama sırasında hata: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Şifre sıfırlama işlemi sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
        }
    }
}