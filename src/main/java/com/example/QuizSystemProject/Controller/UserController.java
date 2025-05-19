package com.example.QuizSystemProject.Controller; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.dto.*; // İleride oluşturulacak tüm ilgili DTO'ları import edin (veya tek tek)
import com.example.QuizSystemProject.Model.User; // User Entity'sini döndürmek için (DTO kullanmak daha iyi olabilir)
import com.example.QuizSystemProject.Service.UserService; // UserService'i import edin
import jakarta.validation.Valid; // Girdi doğrulama için
import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için

import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.web.bind.annotation.*; // Web anotasyonları için (@RestController, @RequestMapping, @GetMapping vb.)

import java.util.List; // List importu
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


@RestController // Bu sınıfın bir REST Controller olduğunu belirtir
@RequestMapping("/api/users") // Bu controller altındaki tüm endpoint'lerin "/api/users" ile başlayacağını belirtir
public class UserController {

    private final UserService userService; // Kullanıcı yönetimi iş mantığı servisi

    // UserService bağımlılığının enjekte edildiği constructor
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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

    // --- Diğer Endpoint'ler ---
    // Kimlik doğrulama (login, register vb.) AuthController'da.
    // Quiz oturumu yönetimi QuizSessionController'da.
    // İstatistikler StatisticsController'da.
}