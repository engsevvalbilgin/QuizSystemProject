package com.example.QuizSystemProject.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.QuizSystemProject.Model.Admin;
import com.example.QuizSystemProject.Model.Student;
import com.example.QuizSystemProject.Model.Teacher;
import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.dto.UserCreationRequest; // Assuming a DTO for user creation
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
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Bağımlılıkların enjekte edildiği constructor
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Kullanıcı Oluşturma Metodu ---
    public User createUser(UserCreationRequest creationRequest) {
        logger.info("UserService: Yeni kullanıcı oluşturuluyor - Email: {}", creationRequest.getEmail());

        // E-posta adresinin zaten kullanımda olup olmadığını kontrol et
        if (userRepository.findByEmail(creationRequest.getEmail()).isPresent()) {
            logger.warn("UserService: Kullanıcı oluşturma başarısız - E-posta adresi zaten kullanımda: {}", creationRequest.getEmail());
            throw new DuplicateEmailException("E-posta adresi zaten kullanımda: " + creationRequest.getEmail());
        }

        User newUser;
        String role = creationRequest.getRole();
        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
            newUser = new Admin();
        } else if ("ROLE_TEACHER".equalsIgnoreCase(role)) {
            newUser = new Teacher();
        } else if ("ROLE_STUDENT".equalsIgnoreCase(role)) {
            newUser = new Student();
        } else {
            logger.warn("UserService: Geçersiz rol belirtildi: {}. Varsayılan olarak STUDENT atanıyor.", role);
            newUser = new Student(); // Default to student or throw error
            role = "ROLE_STUDENT";
        }

        newUser.setName(creationRequest.getName());
        newUser.setSurname(creationRequest.getSurname());
        newUser.setEmail(creationRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(creationRequest.getPassword()));
        newUser.setRole(role.toUpperCase()); // Ensure role is stored in uppercase
        newUser.setAge(creationRequest.getAge());
        newUser.setCreatedDate(LocalDateTime.now());
        newUser.setUpdatedDate(LocalDateTime.now());
        newUser.setActive(true); // Yeni kullanıcılar varsayılan olarak aktif
        newUser.setEnabled(true); // E-posta doğrulaması yoksa varsayılan olarak etkin
                                 // Eğer e-posta doğrulaması varsa bu false olmalı ve doğrulama süreci başlatılmalı

        User savedUser = userRepository.save(newUser);
        logger.info("UserService: Kullanıcı başarıyla oluşturuldu - ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    // --- Kullanıcı Yönetimi Metotları (Genellikle Admin yetkisi gerektirir) ---

    // Tüm kullanıcıları listeleme (Admin yetkisi gerektirecek)
    public List<UserResponse> getAllUsers() {
        logger.info("UserService: Tüm kullanıcılar getiriliyor.");
        List<User> users = userRepository.findAll();
        return users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());
    }

    // Kullanıcıları role göre listeleme (Admin yetkisi gerektirecek)
    public List<UserResponse> getUsersByRole(String role) {
        logger.info("UserService: Rolü '{}' olan kullanıcılar getiriliyor.", role);
        String upperCaseRole = role.toUpperCase();
        boolean isValidRole = upperCaseRole.equals("ROLE_ADMIN") ||
                              upperCaseRole.equals("ROLE_TEACHER") ||
                              upperCaseRole.equals("ROLE_STUDENT");

        if (!isValidRole) {
            logger.warn("UserService: Geçersiz rol belirtildi: {}", role);
            throw new IllegalArgumentException("Geçersiz rol belirtildi: " + role);
        }

        List<User> users = userRepository.findAllByRole(upperCaseRole);
        logger.info("UserService: Rolü '{}' olan {} kullanıcı bulundu.", upperCaseRole, users.size());
        return users.stream()
                   .map(UserResponse::new)
                   .collect(Collectors.toList());
    }


    // ID'ye göre kullanıcı getirme (Admin veya kullanıcı kendi profilini getirebilir)
    public UserDetailsResponse getUserById(int userId) {
        logger.info("UserService: Kullanıcı getiriliyor - ID: {}", userId);
        User user = userRepository.findById(userId)
                                .orElseThrow(() -> {
                                    logger.warn("UserService: Kullanıcı bulunamadı - ID: {}", userId);
                                    return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                });
        logger.info("UserService: Kullanıcı bulundu - ID: {}, Kullanıcı Adı: {}", userId, user.getUsername());
        return new UserDetailsResponse(user);
    }

    // Kullanıcı bilgilerini güncelleme (Admin yetkisi gerektirecek, veya kullanıcı kendi bilgilerini güncelleyebilir - rol, email ve parola hariç)
    public User updateUser(int userId, UserUpdateRequest updateRequest) {
        logger.info("UserService: Kullanıcı güncelleniyor - ID: {}", userId);
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> {
                                              logger.warn("UserService: Güncellenecek kullanıcı bulunamadı - ID: {}", userId);
                                              return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                          });

        boolean updated = false;
        if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty() && !updateRequest.getName().trim().equals(userToUpdate.getName())) {
            userToUpdate.setName(updateRequest.getName().trim());
            updated = true;
        }
        if (updateRequest.getSurname() != null && !updateRequest.getSurname().trim().isEmpty() && !updateRequest.getSurname().trim().equals(userToUpdate.getSurname())) {
            userToUpdate.setSurname(updateRequest.getSurname().trim());
            updated = true;
        }
        if (updateRequest.getAge() >= 0 && updateRequest.getAge() != userToUpdate.getAge()) { // DTO'da @Min(0) olmalı
            userToUpdate.setAge(updateRequest.getAge());
            updated = true;
        }

        if (updated) {
            userToUpdate.setUpdatedDate(LocalDateTime.now());
            User updatedUser = userRepository.save(userToUpdate);
            logger.info("UserService: Kullanıcı başarıyla güncellendi - ID: {}", updatedUser.getId());
            return updatedUser;
        } else {
            logger.info("UserService: Kullanıcı için güncelleme yapılmadı (sağlanan veriler mevcutla aynı) - ID: {}", userId);
            return userToUpdate; // Değişiklik yoksa kaydetmeye gerek yok
        }
    }

    // Kullanıcı rolünü değiştirme (Sadece Admin yetkisi gerektirecek)
    public User changeUserRole(int userId, RoleChangeRequest roleChangeRequest) {
        logger.info("UserService: Kullanıcı rolü değiştiriliyor - ID: {}, Yeni Rol: {}", userId, roleChangeRequest.getNewRole());
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> {
                                              logger.warn("UserService: Rolü değiştirilecek kullanıcı bulunamadı - ID: {}", userId);
                                              return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                          });

        String newRole = roleChangeRequest.getNewRole().toUpperCase();
        boolean isValidRole = newRole.equals("ROLE_ADMIN") ||
                              newRole.equals("ROLE_TEACHER") ||
                              newRole.equals("ROLE_STUDENT");

        if (!isValidRole) {
            logger.warn("UserService: Geçersiz rol belirtildi: {}", newRole);
            throw new IllegalArgumentException("Geçersiz rol belirtildi: " + newRole);
        }

        if (userToUpdate.getRole().equals(newRole)) {
            logger.info("UserService: Kullanıcının rolü zaten {}. Güncelleme yapılmadı - ID: {}", newRole, userId);
            return userToUpdate;
        }

        userToUpdate.setRole(newRole);
        userToUpdate.setUpdatedDate(LocalDateTime.now());
        User updatedUser = userRepository.save(userToUpdate);
        logger.info("UserService: Kullanıcı rolü başarıyla değiştirildi - ID: {}, Yeni Rol: {}", updatedUser.getId(), updatedUser.getRole());
        return updatedUser;
    }


    // Kullanıcının parolasını değiştirme (Admin yetkisi gerektirecek veya kullanıcı kendi parolasını değiştirebilir)
    public User changeUserPassword(int userId, PasswordChangeRequest passwordChangeRequest, boolean isAdminAction) {
        logger.info("UserService: Kullanıcı parolası değiştiriliyor - ID: {}", userId);
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> {
                                              logger.warn("UserService: Parolası değiştirilecek kullanıcı bulunamadı - ID: {}", userId);
                                              return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                          });

        if (!isAdminAction) {
            logger.info("UserService: Kullanıcı kendi parolasını değiştiriyor - ID: {}", userId);
            if (passwordChangeRequest.getCurrentPassword() == null || !passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), userToUpdate.getPassword())) {
                logger.warn("UserService: Parola değiştirme başarısız - Mevcut parola yanlış veya sağlanmadı - ID: {}", userId);
                throw new org.springframework.security.authentication.BadCredentialsException("Mevcut parola yanlış veya sağlanmadı!");
            }
            logger.info("UserService: Mevcut parola doğrulandı - ID: {}", userId);
        } else {
            logger.info("UserService: ADMIN, kullanıcı parolasını değiştiriyor - ID: {} (Mevcut parola kontrolü atlanıyor)", userId);
        }

        String encodedNewPassword = passwordEncoder.encode(passwordChangeRequest.getNewPassword());
        logger.debug("UserService: Yeni parola şifrelendi - ID: {}", userId);

        userToUpdate.setPassword(encodedNewPassword);
        userToUpdate.setUpdatedDate(LocalDateTime.now());
        User updatedUser = userRepository.save(userToUpdate);
        logger.info("UserService: Kullanıcı parolası başarıyla değiştirildi - ID: {}", updatedUser.getId());
        return updatedUser;
    }

    // Kullanıcının e-posta adresini değiştirme (Admin yetkisi gerektirecek veya kullanıcı kendi e-postasını değiştirebilir)
    public User changeUserEmail(int userId, EmailChangeRequest emailChangeRequest, boolean isAdminAction) {
        logger.info("UserService: Kullanıcı e-postası değiştiriliyor - ID: {}, Yeni Email: {}", userId, emailChangeRequest.getNewEmail());
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> {
                                              logger.warn("UserService: E-postası değiştirilecek kullanıcı bulunamadı - ID: {}", userId);
                                              return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                          });

        String newEmail = emailChangeRequest.getNewEmail().trim();

        if (userToUpdate.getEmail().equalsIgnoreCase(newEmail)) {
            logger.info("UserService: Belirtilen e-posta adresi mevcut e-posta ile aynı. Güncelleme yapılmadı - ID: {}", userId);
            return userToUpdate;
        }

        Optional<User> existingUserWithNewEmail = userRepository.findByEmail(newEmail);
        // Assuming User.getId() returns int, and userId is now int.
        if (existingUserWithNewEmail.isPresent() && existingUserWithNewEmail.get().getId() != userId) {
            logger.warn("UserService: E-posta değiştirme başarısız - '{}' e-posta adresi zaten başka bir kullanıcı tarafından kullanımda.", newEmail);
            throw new DuplicateEmailException("'" + newEmail + "' e-posta adresi zaten kullanımda.");
        }

        logger.info("UserService: E-posta adresi '{}' -> '{}' olarak değiştiriliyor - ID: {}", userToUpdate.getEmail(), newEmail, userId);
        userToUpdate.setEmail(newEmail);
        userToUpdate.setEnabled(false); // Require re-verification for new email
        logger.info("UserService: Kullanıcının etkinlik durumu 'false' olarak ayarlandı (e-posta doğrulaması gerekli) - ID: {}", userId);
        
        logger.debug("UserService: TODO - Yeni e-posta için doğrulama maili gönderme mantığı eklenecek - ID: {}", userId);

        userToUpdate.setUpdatedDate(LocalDateTime.now());
        User updatedUser = userRepository.save(userToUpdate);
        logger.info("UserService: Kullanıcı e-postası başarıyla değiştirildi - ID: {}", updatedUser.getId());
        return updatedUser;
    }


    // Kullanıcıyı silme (mantıksal silme: isActive = false yapma) (Admin yetkisi gerektirecek)
    public void softDeleteUser(int userId) {
        logger.info("UserService: Kullanıcı siliniyor (mantıksal) - ID: {}", userId);
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> {
                                              logger.warn("UserService: Mantıksal olarak silinecek kullanıcı bulunamadı - ID: {}", userId);
                                              return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                          });

        if (!userToUpdate.isActive()) {
            logger.info("UserService: Kullanıcı zaten pasif durumda - ID: {}", userId);
            return; // Zaten pasifse işlem yapma
        }

        userToUpdate.setActive(false);
        userToUpdate.setUpdatedDate(LocalDateTime.now());
        userRepository.save(userToUpdate);
        logger.info("UserService: Kullanıcı başarıyla mantıksal olarak silindi (pasif yapıldı) - ID: {}", userToUpdate.getId());
    }

    // Öğretmen olma isteğini gözden geçirme (Admin yetkisi gerektirecek)
    public User reviewTeacherRequest(int userId, boolean approve) {
        logger.info("UserService: Öğretmen isteği gözden geçiriliyor - Kullanıcı ID: {}, Onay: {}", userId, approve);
        User userToReview = userRepository.findById(userId)
                                           .orElseThrow(() -> {
                                               logger.warn("UserService: Öğretmen isteği için kullanıcı bulunamadı - ID: {}", userId);
                                               return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                           });

        if (approve) {
            if (!"ROLE_TEACHER".equals(userToReview.getRole())) {
                logger.info("UserService: Öğretmen isteği ONAYLANDI. Kullanıcının rolü TEACHER olarak değiştiriliyor - ID: {}", userId);
                userToReview.setRole("ROLE_TEACHER");
                // If the user was a student, you might want to convert them to a Teacher entity if you have specific fields for Teacher
                // For single table inheritance, changing role might be enough, or you might need to re-fetch/re-save as Teacher type.
                // For now, just setting the role string. Ensure your User entity can handle this change correctly.
                userToReview.setUpdatedDate(LocalDateTime.now());
                User updatedUser = userRepository.save(userToReview);
                logger.info("UserService: Kullanıcı rolü başarıyla TEACHER olarak ayarlandı - ID: {}", updatedUser.getId());
                
                return updatedUser;
            } else {
                logger.info("UserService: Kullanıcı zaten bir öğretmen. Değişiklik yapılmadı - ID: {}", userId);
                return userToReview;
            }
        } else {
            logger.info("UserService: Öğretmen isteği REDDEDİLDİ - Kullanıcı ID: {}. Kullanıcının rolü değiştirilmedi.", userId);
            
            
            return userToReview;
        }
    }

    // --- Template'teki diğer User metotları ---
    // signIn, logIn, logOut -> AuthenticationService ve Spring Security'ye taşındı.
    // showUserDetails -> Genellikle DTO'lar aracılığıyla Controller katmanında veriyi sunma işidir. GetUserById metodu veriyi çeker.
}

