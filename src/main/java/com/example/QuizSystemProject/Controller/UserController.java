package com.example.QuizSystemProject.Controller; 
import com.example.QuizSystemProject.dto.*; 
import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Service.UserService;
import com.example.QuizSystemProject.Service.MailService;
import jakarta.validation.Valid; 
import org.springframework.security.crypto.password.PasswordEncoder;


import org.springframework.http.HttpStatus; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; 
import java.util.UUID; 

import java.util.List; 
import java.util.Map;
import java.util.HashMap;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication; 
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Collections; 


@RestController
@RequestMapping("/api/users") 
public class UserController {

    private final UserService userService; 
    private final PasswordEncoder passwordEncoder; 
    private final MailService mailService; 

  
    public UserController(UserService userService, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
            System.out.println("UserController: Tüm kullanıcılar listeleniyor.");

            List<UserResponse> users = userService.getAllUsers();

            System.out.println("UserController: " + users.size() + " kullanıcı bulundu.");
            return ResponseEntity.ok(users);
        }
    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable("roleName") String roleName) {
            System.out.println("UserController: Rolü '" + roleName + "' olan kullanıcılar listeleniyor.");

            List<UserResponse> users = userService.getUsersByRole(roleName);

            System.out.println("UserController: Rolü '" + roleName + "' olan " + users.size() + " kullanıcı bulundu.");
            return ResponseEntity.ok(users);
        }


    @GetMapping("/profile")
    public ResponseEntity<UserDetailsResponse> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("UserController: Mevcut kullanıcı profili isteniyor - Kullanıcı: " + username);
        
        UserDetailsResponse userDetailsResponse = userService.getUserByUsername(username);
        
        System.out.println("UserController: Kullanıcı profili bulundu - Kullanıcı: " + username);
        return ResponseEntity.ok(userDetailsResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsResponse> getUserById(@PathVariable("id") int id) {
        System.out.println("UserController: Kullanıcı detayları isteniyor - ID: " + id);

        UserDetailsResponse userDetailsResponse = userService.getUserById(id);

        System.out.println("UserController: Kullanıcı bulundu - ID: " + id);
        return ResponseEntity.ok(userDetailsResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDetailsResponse> updateUser(@PathVariable("id") int id, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
            System.out.println("UserController: Kullanıcı güncelleniyor - ID: " + id);

            User updatedUser = userService.updateUser(id, userUpdateRequest);

            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(updatedUser);

            System.out.println("UserController: Kullanıcı başarıyla güncellendi - ID: " + updatedUser.getId());
            return ResponseEntity.ok(userDetailsResponse);
        }
    
    @PutMapping("/{id}/role")
    public ResponseEntity<UserDetailsResponse> changeUserRole(@PathVariable("id") int id, @Valid @RequestBody RoleChangeRequest roleChangeRequest) {
            System.out.println("UserController: Kullanıcı rolü değiştiriliyor - ID: " + id + ", Yeni Rol: " + roleChangeRequest.getNewRole());

            User updatedUser = userService.changeUserRole(id, roleChangeRequest);

            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(updatedUser);

            System.out.println("UserController: Kullanıcı rolü başarıyla değiştirildi - ID: " + updatedUser.getId() + ", Yeni Rol: " + updatedUser.getRole());
            return ResponseEntity.ok(userDetailsResponse);
        }

    @PutMapping("/{id}/password")
    public ResponseEntity<UserDetailsResponse> changeUserPassword(@PathVariable("id") int id, @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
            System.out.println("UserController: Kullanıcı parolası değiştiriliyor - ID: " + id);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            boolean isAdminAction = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            System.out.println("UserController: Parola değiştirme isteği - Kullanıcı ID: " + id + ", ADMIN işlemi mi?: " + isAdminAction);


            User updatedUser = userService.changeUserPassword(
                    id,
                    passwordChangeRequest, 
                    isAdminAction 
            );

            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(updatedUser);

            System.out.println("UserController: Kullanıcı parolası başarıyla değiştirildi - ID: " + updatedUser.getId());
            return ResponseEntity.ok(userDetailsResponse);
        }

    @PostMapping("/teachers/{userId}/review")
    public ResponseEntity<UserDetailsResponse> reviewTeacherRegistration(
            @PathVariable("userId") int userId,
            @RequestParam("approve") boolean approve) {
        System.out.println("UserController: Öğretmen kayıt talebi inceleniyor - ID: " + userId + ", Onay: " + approve);
        User updatedUser = userService.reviewTeacherRequest(userId, approve);
        UserDetailsResponse responseDto = new UserDetailsResponse(updatedUser);
        System.out.println("UserController: Öğretmen kayıt talebi başarıyla işlendi - ID: " + updatedUser.getId() + ", Onay durumu: " + approve);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/teachers/pending")
    public ResponseEntity<List<UserResponse>> getPendingTeacherRegistrations() {
        System.out.println("UserController: Bekleyen öğretmen kayıt talepleri listeleniyor.");
        List<UserResponse> pendingTeachers = userService.getPendingTeacherRequests();
        System.out.println("UserController: " + pendingTeachers.size() + " adet bekleyen öğretmen kaydı bulundu.");
        return ResponseEntity.ok(pendingTeachers);
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<UserDetailsResponse> changeUserEmail(@PathVariable("id") int id, @Valid @RequestBody EmailChangeRequest emailChangeRequest) {
            System.out.println("UserController: Kullanıcı e-postası değiştiriliyor - ID: " + id + ", Yeni Email: " + emailChangeRequest.getNewEmail());


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


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable("id") int id) {
            System.out.println("UserController: Kullanıcı siliniyor (mantıksal) - ID: " + id);

            userService.softDeleteUser(id);

            System.out.println("UserController: Kullanıcı başarıyla silindi (mantıksal olarak pasif yapıldı) - ID: " + id);
            return ResponseEntity.noContent().build();
        }

@PutMapping("/{id}/review-teacher-request")
public ResponseEntity<UserDetailsResponse> reviewTeacherRequest(@PathVariable("id") int id, @RequestParam("approve") boolean approve) {
           
            System.out.println("UserController: Öğretmen isteği gözden geçiriliyor - Kullanıcı ID: " + id + ", Onay: " + approve);

            User updatedUser = userService.reviewTeacherRequest(id, approve);

            UserDetailsResponse userDetailsResponse = new UserDetailsResponse(updatedUser);

            System.out.println("UserController: Öğretmen isteği başarıyla işlendi - Kullanıcı ID: " + id + ", Onay: " + approve);
            return ResponseEntity.ok(userDetailsResponse);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("UserController: Kullanıcı şifre değiştirme isteği - Kullanıcı: " + username);
        
        try {
            User user = userService.findUserByUsername(username);
            
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                System.out.println("UserController: Mevcut şifre doğru değil - Kullanıcı: " + username);
                return ResponseEntity.badRequest().body("Mevcut şifre doğru değil.");
            }
            
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setUpdatedDate(LocalDateTime.now());
            userService.saveUser(user);
            
            System.out.println("UserController: Şifre başarıyla değiştirildi - Kullanıcı: " + username);
            
            try {
                mailService.sendPasswordChangeNotification(user.getEmail());
                System.out.println("UserController: Şifre değişikliği bildirimi gönderildi - E-posta: " + user.getEmail());
            } catch (Exception mailEx) {
                System.err.println("UserController: Şifre değişikliği bildirimi gönderilemedi - Hata: " + mailEx.getMessage());
            }
            
            return ResponseEntity.ok("Şifreniz başarıyla değiştirildi. E-posta adresinize bir bildirim gönderildi.");
            
        } catch (Exception e) {
            System.err.println("UserController: Şifre değiştirme sırasında hata: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Şifre değiştirme sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
        }
    }
    
    @PostMapping("/change-email")
    public ResponseEntity<String> changeEmail(@Valid @RequestBody ChangeEmailRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("UserController: Kullanıcı email değiştirme isteği - Kullanıcı: " + username);
        
        try {
            User user = userService.findUserByUsername(username);
            
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                System.out.println("UserController: Şifre doğru değil - Kullanıcı: " + username);
                return ResponseEntity.badRequest().body("Şifre doğru değil.");
            }
            
            if (userService.existsByEmail(request.getNewEmail()) && 
                    !user.getEmail().equals(request.getNewEmail())) {
                System.out.println("UserController: Email adresi zaten kullanımda - Email: " + request.getNewEmail());
                return ResponseEntity.badRequest().body("Bu email adresi zaten kullanımda.");
            }
            
            String token = UUID.randomUUID().toString();
            user.setConfirmationToken(token);
            user.setConfirmationTokenExpiryDate(LocalDateTime.now().plusHours(24));
            
            user.setPendingEmail(request.getNewEmail());
            System.out.println("UserController: Doğrulama bekleyen yeni e-posta adresi kaydedildi: " + request.getNewEmail());
            
            userService.saveUser(user);
            
            try {
                String baseUrl = "http://localhost:5173";
                mailService.sendEmailChangeVerification(request.getNewEmail(), token, baseUrl);
                System.out.println("UserController: Email değişikliği doğrulama maili gönderildi - E-posta: " + request.getNewEmail());
            } catch (Exception mailEx) {
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

    @PostMapping("/password-reset/request")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        try {
            User user = userService.findUserByEmail(requestDto.getEmail());
            if (user == null) {
                return ResponseEntity.ok("Eğer bu e-posta adresine sahip bir hesap varsa, şifre sıfırlama talimatları gönderildi.");
            }
            
            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            user.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(24)); // 24 saat geçerli
            userService.saveUser(user);
            
            String baseUrl = "http://localhost:5173";
            String resetLink = baseUrl + "/password-reset?token=" + token;
            System.out.println("UserController: Oluşturulan şifre sıfırlama linki: " + resetLink);
            
            mailService.sendPasswordResetMail(user.getEmail(), resetLink);
            
            return ResponseEntity.ok("E-posta adresinize şifre sıfırlama talimatları gönderildi.");
            
        } catch (Exception e) {
            System.err.println("UserController: Şifre sıfırlama talebi sırasında hata: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Şifre sıfırlama talebi sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
        }
    }
    
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
            if (!resetDto.getNewPassword().equals(resetDto.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("Şifreler eşleşmiyor.");
            }
            
            User user = userService.findUserByResetPasswordToken(resetDto.getToken());
            if (user == null) {
                return ResponseEntity.badRequest().body("Geçersiz veya süresi dolmuş token.");
            }
            
            if (user.getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Token'in süresi dolmuş. Lütfen yeni bir şifre sıfırlama talebi oluşturun.");
            }
            
            user.setPassword(passwordEncoder.encode(resetDto.getNewPassword()));
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiryDate(null);
            user.setUpdatedDate(LocalDateTime.now());
            userService.saveUser(user);
            
            mailService.sendPasswordResetSuccessMail(user.getEmail());
            
            return ResponseEntity.ok("Şifreniz başarıyla sıfırlandı. Artık yeni şifrenizle giriş yapabilirsiniz.");
            
        } catch (Exception e) {
            System.err.println("UserController: Şifre sıfırlama tamamlama sırasında hata: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Şifre sıfırlama işlemi sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
        }
    }
}