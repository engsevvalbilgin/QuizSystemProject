package com.example.QuizSystemProject.Controller;
import com.example.QuizSystemProject.Model.*;

import com.example.QuizSystemProject.*;
import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Service.UserService;
import com.example.QuizSystemProject.dto.EmailChangeRequest;
import com.example.QuizSystemProject.dto.PasswordChangeRequest;
import com.example.QuizSystemProject.dto.RoleChangeRequest;
import com.example.QuizSystemProject.dto.UserUpdateRequest;

import jakarta.validation.Valid; // Girdi doğrulama için
import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.web.bind.annotation.*; // Web anotasyonları için (@RestController, @RequestMapping, @GetMapping vb.)
import java.util.List; // List importu
import java.util.Optional; // Optional importu

// Şifre değiştirme hatası için (BadCredentialsException) Spring Security importu
import org.springframework.security.authentication.BadCredentialsException;
// Geçersiz rol gibi durumlar için
import java.lang.IllegalArgumentException;

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
    // (Sadece Admin yetkisi gerektirir)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        System.out.println("UserController: Tüm kullanıcılar listeleniyor.");
        // NOT: Gerçek implementasyonda, Service'ten tüm kullanıcıları çekme
        // ve DTO listesine dönüştürüp 200 OK yanıtı ile döndürme mantığı olacak.
        // Spring Security ile bu endpoint'in sadece ADMIN rolüne sahip kullanıcılar tarafından çağrılmasını sağlayacağız.

        // List<User> users = userService.getAllUsers();
        // return ResponseEntity.ok(users); // DTO listesi döndürülmeli

        return ResponseEntity.ok(List.of(Student.createStudent())); // Şimdilik simülasyon
    }

     // Kullanıcıları role göre listeleme
     // HTTP GET isteği ile "/api/users/role/{roleName}" adresine yapılan istekleri karşılar
     // (Sadece Admin yetkisi gerektirir)
     @GetMapping("/role/{roleName}")
     public ResponseEntity<List<User>> getUsersByRole(@PathVariable("roleName") String roleName) {
         System.out.println("UserController: Rolü '" + roleName + "' olan kullanıcılar listeleniyor.");
         // NOT: Gerçek implementasyonda, Service'ten role göre kullanıcıları çekme
         // ve DTO listesine dönüştürüp 200 OK yanıtı ile döndürme mantığı olacak.
         // Spring Security ile bu endpoint'in sadece ADMIN rolüne sahip kullanıcılar tarafından çağrılmasını sağlayacağız.

         // List<User> users = userService.getUsersByRole(roleName);
         // return ResponseEntity.ok(users); // DTO listesi döndürülmeli

         return ResponseEntity.ok(List.of(Student.createStudent())); // Şimdilik simülasyon
     }


    // ID'ye göre kullanıcı getirme
    // HTTP GET isteği ile "/api/users/{id}" adresine yapılan istekleri karşılar
    // (Admin veya kullanıcı kendi profilini getirebilir)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        System.out.println("UserController: Kullanıcı detayları isteniyor - ID: " + id);
        // NOT: Gerçek implementasyonda, Service'ten kullanıcıyı ID'ye göre çekme,
        // eğer varsa DTO'ya dönüştürüp 200 OK yanıtı döndürme, yoksa 404 Not Found döndürme mantığı olacak.
        // Spring Security ile bu endpoint'in ADMIN tarafından veya kullanıcının kendi ID'si için çağrılmasını sağlayacağız.

        // Optional<User> userOptional = userService.getUserById(id);
        // return userOptional.map(user -> ResponseEntity.ok(user)) // Eğer kullanıcı varsa 200 OK
        //                      .orElseGet(() -> ResponseEntity.notFound().build()); // Yoksa 404 Not Found

        return ResponseEntity.ok(Student.createStudent()); // Şimdilik simülasyon
    }

    // Kullanıcı bilgilerini güncelleme
    // HTTP PUT isteği ile "/api/users/{id}" adresine yapılan istekleri karşılar
    // (Admin yetkisi gerektirecek, veya kullanıcı kendi bilgilerini güncelleyebilir - rol hariç)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        System.out.println("UserController: Kullanıcı güncelleniyor - ID: " + id);
        // NOT: Gerçek implementasyonda, id ve DTO'daki bilgileri kullanarak Service'teki updateUser metodunu çağırma,
        // yetki kontrolü (kim güncelliyor, neyi güncelliyor?), güncellenen kullanıcıyı (veya DTO'sunu) döndürme mantığı olacak.

        // User updatedUser = userService.updateUser(
        //      id,
        //      userUpdateRequest.getName(),
        //      userUpdateRequest.getSurname(),
        //      userUpdateRequest.getAge(), // Eğer DTO'da varsa
        //      userUpdateRequest.getEmail(),
        //      userUpdateRequest.getUsername(),
        //      userUpdateRequest.isActive() // Admin sadece active durumunu güncelleyebilir
        // );
        // return ResponseEntity.ok(updatedUser);

        return ResponseEntity.ok(Student.createStudent()); // Şimdilik simülasyon
    }

     // Kullanıcı rolünü değiştirme
     // HTTP PUT isteği ile "/api/users/{id}/role" adresine yapılan istekleri karşılar
     // (Sadece Admin yetkisi gerektirecek)
     @PutMapping("/{id}/role")
     public ResponseEntity<User> changeUserRole(@PathVariable("id") Long id, @Valid @RequestBody RoleChangeRequest roleChangeRequest) {
         System.out.println("UserController: Kullanıcı rolü değiştiriliyor - ID: " + id + ", Yeni Rol: " + roleChangeRequest.getNewRole());
         // NOT: Gerçek implementasyonda, id ve DTO'daki yeni rolü kullanarak Service'teki changeUserRole metodunu çağırma,
         // yetki kontrolü (sadece Admin), güncellenen kullanıcıyı döndürme mantığı olacak.

         // User updatedUser = userService.changeUserRole(id, roleChangeRequest.getNewRole());
         // return ResponseEntity.ok(updatedUser);

         return ResponseEntity.ok(Student.createStudent()); // Şimdilik simülasyon
     }

    // Kullanıcının parolasını değiştirme
    // HTTP PUT isteği ile "/api/users/{id}/password" adresine yapılan istekleri karşılar
    // (Admin yetkisi gerektirecek veya kullanıcı kendi parolasını değiştirebilir)
    @PutMapping("/{id}/password")
    public ResponseEntity<User> changeUserPassword(@PathVariable("id") Long id, @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        System.out.println("UserController: Kullanıcı parolası değiştiriliyor - ID: " + id);
        // NOT: Gerçek implementasyonda, id ve DTO'daki eski/yeni parolaları kullanarak Service'teki changeUserPassword metodunu çağırma,
        // yetki kontrolü (Admin mi kendi mi?), güncellenen kullanıcıyı döndürme mantığı olacak.

        // boolean isAdminAction = ... // İsteği yapanın Admin olup olmadığı güvenlik bağlamından kontrol edilebilir
        // User updatedUser = userService.changeUserPassword(
        //      id,
        //      passwordChangeRequest.getCurrentPassword(), // Sadece kullanıcı kendi değiştiriyorsa gerekli
        //      passwordChangeRequest.getNewPassword(),
        //      isAdminAction
        // );
        // return ResponseEntity.ok(updatedUser);

         return ResponseEntity.ok(Student.createStudent()); // Şimdilik simülasyon
    }

     // Kullanıcının e-posta adresini değiştirme
     // HTTP PUT isteği ile "/api/users/{id}/email" adresine yapılan istekleri karşılar
     // (Admin yetkisi gerektirecek veya kullanıcı kendi e-postasını değiştirebilir)
    @PutMapping("/{id}/email")
    public ResponseEntity<User> changeUserEmail(@PathVariable("id") Long id, @Valid @RequestBody EmailChangeRequest emailChangeRequest) {
         System.out.println("UserController: Kullanıcı e-postası değiştiriliyor - ID: " + id + ", Yeni Email: " + emailChangeRequest.getNewEmail());
         // NOT: Gerçek implementasyonda, id ve DTO'daki yeni e-postayı kullanarak Service'teki changeUserEmail metodunu çağırma,
         // yetki kontrolü (Admin mi kendi mi?), güncellenen kullanıcıyı döndürme mantığı olacak.

         // boolean isAdminAction = ... // İsteği yapanın Admin olup olmadığı güvenlik bağlamından kontrol edilebilir
         // User updatedUser = userService.changeUserEmail(id, emailChangeRequest.getNewEmail(), isAdminAction);
         // return ResponseEntity.ok(updatedUser);

         return ResponseEntity.ok(Student.createStudent()); // Şimdilik simülasyon
     }


    // Kullanıcıyı silme (mantıksal silme: isActive = false yapma)
    // HTTP DELETE isteği ile "/api/users/{id}" adresine yapılan istekleri karşılar
    // (Sadece Admin yetkisi gerektirecek)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable("id") int id) {
        System.out.println("UserController: Kullanıcı siliniyor (mantıksal) - ID: " + id);
        // NOT: Gerçek implementasyonda, id kullanarak Service'teki softDeleteUser metodunu çağırma,
        // yetki kontrolü (sadece Admin), başarılı ise 204 No Content yanıtı döndürme mantığı olacak.

        // userService.softDeleteUser(id);
        // return ResponseEntity.noContent().build();

        return ResponseEntity.noContent().build(); // Şimdilik simülasyon
    }

    // Öğretmen olma isteğini gözden geçirme
    // HTTP PUT isteği ile "/api/users/{id}/review-teacher-request" adresine yapılan istekleri karşılar
    // (Sadece Admin yetkisi gerektirecek)
    @PutMapping("/{id}/review-teacher-request")
    public ResponseEntity<User> reviewTeacherRequest(@PathVariable("id") Long id, @RequestBody boolean approve) {
        // @RequestBody boolean: İstek gövdesindeki boolean değeri (true/false) alır.
        // Bunun için de ayrı bir DTO oluşturulabilir (örn: TeacherRequestReviewDto { boolean approve; }).
        System.out.println("UserController: Öğretmen isteği gözden geçiriliyor - Kullanıcı ID: " + id + ", Onay: " + approve);
        // NOT: Gerçek implementasyonda, id ve onay bilgisini kullanarak Service'teki reviewTeacherRequest metodunu çağırma,
        // yetki kontrolü (sadece Admin), güncellenen kullanıcıyı (eğer rolü değiştiyse) döndürme mantığı olacak.

        // boolean success = userService.reviewTeacherRequest(id, approve);
        // if (success) {
        //     // Başarılı ise güncellenmiş kullanıcıyı döndür veya sadece başarı mesajı dön.
        //     Optional<User> updatedUserOptional = userService.getUserById(id); // Güncellenmiş kullanıcıyı tekrar çekelim
        //     return updatedUserOptional.map(ResponseEntity::ok)
        //                                .orElseGet(() -> ResponseEntity.ok().body(null)); // Kullanıcı bulunamazsa boş body ile 200 OK
        // } else {
        //      // Hata yönetimi
        //      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Hata durumunda 400 Bad Request
        // }

        return ResponseEntity.ok(Student.createStudent()); // Şimdilik simülasyon
    }


    // --- Diğer Endpoint'ler ---
    // Kimlik doğrulama (login, register vb.) AuthController'da.
    // Quiz oturumu yönetimi QuizSessionController'da.
    // İstatistikler StatisticsController'da.
}

