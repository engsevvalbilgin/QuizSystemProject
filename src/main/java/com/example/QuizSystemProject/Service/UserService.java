package com.example.QuizSystemProject.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.dto.EmailChangeRequest;
import com.example.QuizSystemProject.dto.PasswordChangeRequest;
import com.example.QuizSystemProject.dto.RoleChangeRequest;
import com.example.QuizSystemProject.dto.UserDetailsResponse;
import com.example.QuizSystemProject.dto.UserResponse;
import com.example.QuizSystemProject.dto.UserUpdateRequest;
import com.example.QuizSystemProject.exception.DuplicateEmailException;
import com.example.QuizSystemProject.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Collectors importu


@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir
@Transactional // Bu annotation'ı sınıf seviyesine koyarak tüm public metotlara uygulayabiliriz.
public class UserService {

    // Bu servisin ihtiyaç duyacağı Repository'ler ve diğer Servisler
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Parola işlemleri için

    // Bağımlılıkların enjekte edildiği constructor
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Kullanıcı Yönetimi Metotları (Genellikle Admin yetkisi gerektirir) ---
    // Sizin Admin.java template'indeki addUser*, updateUser*, showAllUsers* metotlarının mantığı burada.

 // Tüm kullanıcıları listeleme (Admin yetkisi gerektirecek)
    public List<UserResponse> getAllUsers() { // Dönüş tipi List<UserResponse> olarak değişti
        System.out.println("UserService: Tüm kullanıcılar getiriliyor.");

        // Repository'den tüm User Entity'lerini çek
        List<User> users = userRepository.findAll();

        // Çekilen User Entity'lerini UserResponse DTO'larına dönüştür ve liste olarak döndür
        return users.stream()
                    .map(UserResponse::new) // Her User Entity'sini UserResponse DTO'sunun constructor'ı ile dönüştür
                    .collect(Collectors.toList()); // Sonuçları liste olarak topla
    }

 // Kullanıcıları role göre listeleme (Admin yetkisi gerektirecek)
    // Dönüş tipi List<UserResponse> olarak değişti
    public List<UserResponse> getUsersByRole(String role) {
        System.out.println("UserService: Rolü '" + role + "' olan kullanıcılar getiriliyor.");

        // 1. Gelen rol değerini doğrula (Geçerli bir rol mü?)
        // Tıpkı changeUserRole metodundaki gibi kontrol edelim.
        boolean isValidRole = role.equals("ROLE_ADMIN") ||
                              role.equals("ROLE_TEACHER") ||
                              role.equals("ROLE_STUDENT");

        if (!isValidRole) {
            System.err.println("UserService: Geçersiz rol belirtildi: " + role);
            throw new IllegalArgumentException("Geçersiz rol belirtildi: " + role); // Geçersiz rol için hata fırlat
        }

        // 2. Repository'deki findAllByRole metodunu kullanarak kullanıcıları role göre filtrele
        List<User> users = userRepository.findAllByRole(role);

        System.out.println("UserService: Rolü '" + role + "' olan " + users.size() + " kullanıcı bulundu.");

        // 3. Çekilen User Entity'lerini UserResponse DTO'larına dönüştür ve liste olarak döndür
        return users.stream()
                   .map(UserResponse::new) // Her User Entity'sini UserResponse DTO'sunun constructor'ı ile dönüştür
                   .collect(Collectors.toList()); // Sonuçları liste olarak topla
    }


  /// ID'ye göre kullanıcı getirme (Admin veya kullanıcı kendi profilini getirebilir)
    // Dönüş tipi UserDetailsResponse olarak değişti, artık Optional dönmeyecek
    public UserDetailsResponse getUserById(Long userId) {
        System.out.println("UserService: Kullanıcı getiriliyor - ID: " + userId);

        // Repository'den kullanıcıyı ID'ye göre bul
        User user = userRepository.findById(userId)
                                // Eğer kullanıcı bulunamazsa, UserNotFoundException fırlat
                                // Lambda ifadesi () -> new UserNotFoundException(...) şeklinde olmalı
                                .orElseThrow(() -> new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı."));

        System.out.println("UserService: Kullanıcı bulundu - Kullanıcı Adı: " + user.getUsername());

        // Bulunan User Entity'sini UserDetailsResponse DTO'suna dönüştür ve döndür
        return new UserDetailsResponse(user);
    }

    // Kullanıcı bilgilerini güncelleme (Admin yetkisi gerektirecek, veya kullanıcı kendi bilgilerini güncelleyebilir - rol hariç)
    // Sizin Admin.java template'indeki updateUser metodunun mantığı burada.
 // Kullanıcı bilgilerini güncelleme (Admin yetkisi gerektirecek, veya kullanıcı kendi bilgilerini güncelleyebilir - rol, email ve parola hariç)
    // UserUpdateRequest DTO'su kullanılacak
    public User updateUser(Long userId, UserUpdateRequest updateRequest) { // Metot imzası UserUpdateRequest alacak şekilde değişti
        System.out.println("UserService: Kullanıcı güncelleniyor - ID: " + userId);

        // 1. Güncellenmek istenen kullanıcıyı ID'ye göre bul
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı."));

        // 2. UserUpdateRequest DTO'sundan gelen verilerle kullanıcı Entity'sini güncelle
        // Not: Bu metod rol, parola veya email gibi alanları güncellememeli.
        // Bu alanlar için ayrı change... metotları kullanılmalı. isActive alanı softDelete tarafından yönetiliyor.
        if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
            userToUpdate.setName(updateRequest.getName().trim());
        }
        if (updateRequest.getSurname() != null && !updateRequest.getSurname().trim().isEmpty()) {
            userToUpdate.setSurname(updateRequest.getSurname().trim());
        }
         // Yaş (int olduğu için null kontrolü yerine 0 veya negatif olmayan kontrolü yapılabilir, ama DTO validasyonu zaten var)
         if (updateRequest.getAge() >= 0) { // DTO'da @Min(0) olduğu için >=0 kontrolü yeterli
             userToUpdate.setAge(updateRequest.getAge());
         }
        // username ve email alanlarının güncellenmesi:
        // Bu alanlar benzersizlik kontrolü gerektirdiği ve hassas olduğu için,
        // ayrı changeUserEmail/changeUserUsername metotları (eğer username değiştirme eklenirse)
        // veya bu metod içinde detaylı kontrol ile yapılmalıdır.
        // Basitlik için şimdilik sadece ad, soyad, yaş güncelleniyor.

        // 3. Güncellenme tarihini set et
        userToUpdate.setUpdatedDate(LocalDateTime.now());

        // 4. Güncellenen kullanıcıyı veritabanına kaydet
        User updatedUser = userRepository.save(userToUpdate);

        System.out.println("UserService: Kullanıcı başarıyla güncellendi - ID: " + updatedUser.getId());

        // 5. Güncellenen kullanıcı Entity'sini döndür
        // Controller katmanı bu Entity'yi UserDetailsResponse DTO'suna dönüştürecektir.
        return updatedUser;
    }

 // Kullanıcı rolünü değiştirme (Sadece Admin yetkisi gerektirecek)
    // RoleChangeRequest DTO'su kullanılacak
    public User changeUserRole(Long userId, RoleChangeRequest roleChangeRequest) { // Metot imzası RoleChangeRequest alacak şekilde
        System.out.println("UserService: Kullanıcı rolü değiştiriliyor - ID: " + userId + ", Yeni Rol: " + roleChangeRequest.getNewRole());

        // 1. Rolü değiştirilmek istenen kullanıcıyı ID'ye göre bul
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı."));

        // 2. Yeni rol değerini DTO'dan al
        String newRole = roleChangeRequest.getNewRole();

        // 3. Yeni rol değerini doğrula (Geçerli bir rol mü?)
        // Bizim rollerimiz "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_STUDENT".
        // Gelen rolün bu değerlerden biri olup olmadığını kontrol edelim.
        boolean isValidRole = newRole.equals("ROLE_ADMIN") ||
                              newRole.equals("ROLE_TEACHER") ||
                              newRole.equals("ROLE_STUDENT");

        if (!isValidRole) {
            System.err.println("UserService: Geçersiz rol belirtildi: " + newRole);
            throw new IllegalArgumentException("Geçersiz rol belirtildi: " + newRole); // Geçersiz rol için hata fırlat
        }


        // 4. Kullanıcının rolünü güncelle
        userToUpdate.setRole(newRole);

        // 5. Güncellenme tarihini set et
        userToUpdate.setUpdatedDate(LocalDateTime.now());

        // 6. Güncellenen kullanıcıyı veritabanına kaydet
        User updatedUser = userRepository.save(userToUpdate);

        System.out.println("UserService: Kullanıcı rolü başarıyla değiştirildi - ID: " + updatedUser.getId() + ", Yeni Rol: " + updatedUser.getRole());

        // 7. Güncellenen kullanıcı Entity'sini döndür
        return updatedUser;
    }


 // Kullanıcının parolasını değiştirme (Admin yetkisi gerektirecek veya kullanıcı kendi parolasını değiştirebilir)
    // PasswordChangeRequest DTO'su kullanılacak
    // isAdminAction: İsteği yapan ADMIN ise true, kullanıcı kendi parolasını değiştiriyorsa false.
    public User changeUserPassword(Long userId, PasswordChangeRequest passwordChangeRequest, boolean isAdminAction) {
        System.out.println("UserService: Kullanıcı parolası değiştiriliyor - ID: " + userId);

        // 1. Parolası değiştirilmek istenen kullanıcıyı ID'ye göre bul
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı."));

        // 2. İsteği yapanın Admin olup olmadığını kontrol et (Mantık bu metodun içinde)
        //    Eğer kullanıcı kendi parolasını değiştiriyorsa (isAdminAction == false), mevcut parolasını doğrula.
        if (!isAdminAction) {
            System.out.println("UserService: Kullanıcı kendi parolasını değiştiriyor - ID: " + userId);
            // PasswordEncoder'ı kullanarak kullanıcının girdiği mevcut parolayı (ham)
            // veritabanındaki şifrelenmiş parola (hash) ile karşılaştır.
            if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), userToUpdate.getPassword())) {
                System.err.println("UserService: Parola değiştirme başarısız - Mevcut parola yanlis!");
                // Mevcut parola yanlışsa BadCredentialsException fırlat (Spring Security'de yaygın hata türü)
                throw new org.springframework.security.authentication.BadCredentialsException("Mevcut parola yanlis!");
            }
            System.out.println("UserService: Mevcut parola doğrulandı.");
        } else {
             System.out.println("UserService: ADMIN, kullanıcı parolasını değiştiriyor - ID: " + userId + " (Mevcut parola kontrolü atlanıyor)");
             // Admin değiştiriyorsa, mevcut parola kontrolü atlanır.
             // Admin yetkisi Spring Security katmanında kontrol edilmelidir.
        }

        // 3. Yeni parolayı şifrele
        String encodedNewPassword = passwordEncoder.encode(passwordChangeRequest.getNewPassword());
        System.out.println("UserService: Yeni parola şifrelendi.");


        // 4. Kullanıcının parolasını güncelle
        userToUpdate.setPassword(encodedNewPassword);

        // 5. Güncellenme tarihini set et
        userToUpdate.setUpdatedDate(LocalDateTime.now());

        // 6. Güncellenen kullanıcıyı veritabanına kaydet
        User updatedUser = userRepository.save(userToUpdate);

        System.out.println("UserService: Kullanıcı parolası başarıyla değiştirildi - ID: " + updatedUser.getId());

        // 7. Güncellenen kullanıcı Entity'sini döndür
        return updatedUser;
    }

 // Kullanıcının e-posta adresini değiştirme (Admin yetkisi gerektirecek veya kullanıcı kendi e-postasını değiştirebilir)
    // EmailChangeRequest DTO'su kullanılacak
    // isAdminAction: İsteği yapan ADMIN ise true, kullanıcı kendi e-postasını değiştiriyorsa false.
    public User changeUserEmail(Long userId, EmailChangeRequest emailChangeRequest, boolean isAdminAction) {
        System.out.println("UserService: Kullanıcı e-postası değiştiriliyor - ID: " + userId + ", Yeni Email: " + emailChangeRequest.getNewEmail());

        // 1. E-posta adresi değiştirilmek istenen kullanıcıyı ID'ye göre bul
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı."));

        // 2. Yeni e-posta adresini DTO'dan al
        String newEmail = emailChangeRequest.getNewEmail().trim(); // Başındaki/sonundaki boşlukları kaldır

        // 3. Yeni e-posta adresinin benzersiz olduğunu kontrol et
        // Kendi mevcut e-posta adresini girmiş olabilir, bu durumda hata vermemeliyiz.
        Optional<User> existingUserWithNewEmail = userRepository.findByEmail(newEmail);

        // Eğer bu email başka bir kullanıcı tarafından kullanılıyorsa VE bu kullanıcı şu an güncellediğimiz kullanıcı değilse
        if (existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getId().equals(userId)) {
            System.err.println("UserService: E-posta değiştirme başarısız - '" + newEmail + "' e-posta adresi zaten kullanımda.");
            throw new DuplicateEmailException("'" + newEmail + "' e-posta adresi zaten kullanımda."); // Çakışma hatası fırlat
        }

        // Eğer yeni email, kullanıcının mevcut email'inden farklıysa devam et
        if (!userToUpdate.getEmail().equalsIgnoreCase(newEmail)) {
             System.out.println("UserService: E-posta adresi '" + userToUpdate.getEmail() + "' -> '" + newEmail + "' olarak değiştiriliyor.");

             // 4. Kullanıcının e-posta adresini güncelle
             userToUpdate.setEmail(newEmail);

             // 5. E-posta doğrulama durumunu sıfırla (yeniden doğrulama gereksin)
             // ADMIN değiştirse bile, güvenlik için e-posta doğrulamasını sıfırlamak iyi bir practice olabilir.
             userToUpdate.setEnabled(false);
             System.out.println("UserService: Kullanıcının etkinlik durumu 'false' olarak ayarlandı (e-posta doğrulaması gerekli).");


             // TODO: E-posta doğrulama token'ı oluştur ve MailService kullanarak doğrulama maili gönder
             // Bu mantık MailService implemente edildiğinde ve User Entity'sine token alanı eklendiğinde tamamlanacak.
             System.out.println("UserService: TODO - Yeni e-posta için doğrulama maili gönderme mantığı eklenecek.");
             // userToUpdate.setConfirmationToken(generateNewConfirmationToken()); // Yeni token oluştur
             // mailService.sendConfirmationEmail(userToUpdate.getEmail(), userToUpdate.getConfirmationToken()); // Mail gönder

             // 6. Güncellenme tarihini set et
             userToUpdate.setUpdatedDate(LocalDateTime.now());

             // 7. Güncellenen kullanıcıyı veritabanına kaydet
             User updatedUser = userRepository.save(userToUpdate);

             System.out.println("UserService: Kullanıcı e-postası başarıyla değiştirildi - ID: " + updatedUser.getId());

            // 8. Güncellenen kullanıcı Entity'sini döndür
            return updatedUser;

        } else {
             System.out.println("UserService: Belirtilen e-posta adresi mevcut e-posta ile ayni. Güncelleme yapilmadi.");
             // E-posta değişmediyse mevcut kullanıcı objesini döndür
             return userToUpdate;
        }
    }


    // Kullanıcıyı silme (mantıksal silme: isActive = false yapma) (Admin yetkisi gerektirecek)
    // Sizin Admin.java template'indeki addUser, User.java template'indeki deleteUser metotlarının mantığı burada.
 // Kullanıcıyı silme (mantıksal silme: isActive = false yapma) (Admin yetkisi gerektirecek)
    public void softDeleteUser(Long userId) {
        System.out.println("UserService: Kullanıcı siliniyor (mantıksal) - ID: " + userId);

        // 1. Silinmek istenen kullanıcıyı ID'ye göre bul
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı."));

        // 2. Kullanıcının isActive alanını false yaparak mantıksal silme işlemini gerçekleştir
        userToUpdate.setActive(false);

        // 3. Güncellenme tarihini set et
        userToUpdate.setUpdatedDate(LocalDateTime.now());

        // 4. Güncellenen kullanıcıyı veritabanına kaydet
        userRepository.save(userToUpdate);

        System.out.println("UserService: Kullanıcı başarıyla mantıksal olarak silindi (pasif yapıldı) - ID: " + userToUpdate.getId());

        // Bu metod void döndürdüğü için bir şey return etmiyoruz.
    }

 // Öğretmen olma isteğini gözden geçirme (Admin yetkisi gerektirecek)
    // userId: İsteği yapan kullanıcının ID'si (isteği kimin yaptığını kontrol etmek için gerekebilir, ancak Security ile de kontrol edilebilir)
    // approve: İsteğin onaylanıp onaylanmadığı (true = onaylandı, false = reddedildi)
    public boolean reviewTeacherRequest(Long userId, boolean approve) {
        System.out.println("UserService: Öğretmen isteği gözden geçiriliyor - Kullanıcı ID: " + userId + ", Onay: " + approve);

        // 1. İsteği yapılan kullanıcıyı ID'ye göre bul
        // NOT: Normalde burada, kullanıcının gerçekten bir öğretmenlik "isteği" olup olmadığını da kontrol etmek gerekebilir.
        // Ancak şu an basit bir modelimiz olduğu için, sadece kullanıcıyı bulup devam ediyoruz.
        User userToReview = userRepository.findById(userId)
                                           .orElseThrow(() -> new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı."));

        // 2. İsteğin onaylanıp onaylanmadığına göre işlem yap
        if (approve) {
            System.out.println("UserService: Öğretmen isteği ONAYLANDI. Kullanıcının rolü TEACHER olarak değiştiriliyor - ID: " + userId);
            // İsteği onaylandıysa, kullanıcının rolünü ROLE_TEACHER olarak değiştir.
            // Daha önce yazdığımız changeUserRole metodunu tekrar kullanabiliriz.
            // Bunun için bir RoleChangeRequest objesi oluşturmamız gerekiyor.
             RoleChangeRequest teacherRoleRequest = new RoleChangeRequest("ROLE_TEACHER");
             // changeUserRole metodu kullanıcıyı bulup günceller ve kaydeder.
             // isAdminAction true geçiyoruz çünkü bu Admin eylemi. Ancak changeUserRole içindeki isAdminAction şifre/email özelindeydi.
             // Rol değişimi kendi içinde basit bir işlemdi. changeUserRole metodunu doğrudan kullanmak yerine,
             // rol değiştirme mantığını burada tekrar kullanmak daha temiz olabilir, çünkü changeUserRole metodu DTO bekliyor.
             // Veya changeUserRole metodunu role string ve isAdminAction/yetki kontrolüyle kullanabiliriz.
             // En basit yaklaşım, rol değiştirme mantığını burada doğrudan uygulamak.

             userToReview.setRole("ROLE_TEACHER"); // Rolü TEACHER olarak ayarla
             userToReview.setUpdatedDate(LocalDateTime.now()); // Güncellenme tarihini set et
             userRepository.save(userToReview); // Kullanıcıyı kaydet


             System.out.println("UserService: Kullanıcı rolü başarıyla TEACHER olarak ayarlandı - ID: " + userToReview.getId());
            // TODO: Kullanıcıya isteğinin onaylandığına dair e-posta gönderme (isteğe bağlı)

        } else {
            System.out.println("UserService: Öğretmen isteği REDDEDİLDİ - Kullanıcı ID: " + userId);
            // İsteği reddedildiyse, kullanıcının rolü değişmez (genellikle STUDENT olarak kalır).
            // Eğer bir "reddedildi" durumu saklamak istersek, User Entity'sine veya ayrı bir TeacherRequest Entity'sine alan eklememiz gerekir.
             // userToReview.setTeacherRequestStatus("DENIED"); // Eğer böyle bir alan olsaydı
             // userToReview.setUpdatedDate(LocalDateTime.now());
             // userRepository.save(userToReview);
            System.out.println("UserService: Kullanıcının rolü değiştirilmedi.");
             // TODO: Kullanıcıya isteğinin reddedildiğine dair e-posta gönderme (isteğe bağlı)
        }

        // İşlemin başarılı olduğunu belirtmek için true döndür.
        // Kullanıcı bulunamazsa zaten exception fırlatıldığı için buraya gelinmez.
        return true;
    }

    // --- Template'teki diğer User metotları ---
    // signIn, logIn, logOut -> AuthenticationService ve Spring Security'ye taşındı.
    // showUserDetails -> Genellikle DTO'lar aracılığıyla Controller katmanında veriyi sunma işidir. GetUserById metodu veriyi çeker.
}

