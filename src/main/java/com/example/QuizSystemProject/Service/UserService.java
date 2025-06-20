package com.example.QuizSystemProject.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.QuizSystemProject.Model.Admin;
import com.example.QuizSystemProject.Model.Student;
import com.example.QuizSystemProject.Model.Teacher;
import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.dto.UserCreationRequest;
import com.example.QuizSystemProject.dto.EmailChangeRequest;
import com.example.QuizSystemProject.dto.PasswordChangeRequest;
import com.example.QuizSystemProject.dto.RoleChangeRequest;
import com.example.QuizSystemProject.dto.UserDetailsResponse;
import com.example.QuizSystemProject.dto.UserResponse;
import com.example.QuizSystemProject.dto.UserUpdateRequest;
import com.example.QuizSystemProject.dto.TeacherRegistrationRequest; 
import com.example.QuizSystemProject.exception.DuplicateEmailException;
import com.example.QuizSystemProject.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService; 

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MailService mailService) { 
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService; 
    }

    public User createUser(UserCreationRequest creationRequest) {
        logger.info("UserService: Yeni kullanıcı oluşturuluyor - Email: {}", creationRequest.getEmail());

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
            newUser = new Student(); 
            role = "ROLE_STUDENT";
        }

        newUser.setName(creationRequest.getName());
        newUser.setSurname(creationRequest.getSurname());
        newUser.setEmail(creationRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(creationRequest.getPassword()));
        newUser.setRole(role.toUpperCase()); 
        newUser.setAge(creationRequest.getAge());
        newUser.setCreatedDate(LocalDateTime.now());
        newUser.setUpdatedDate(LocalDateTime.now());
        newUser.setActive(true); 
        newUser.setEnabled(true); 

        User savedUser = userRepository.save(newUser);
        logger.info("UserService: Kullanıcı başarıyla oluşturuldu - ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    public User submitTeacherRegistration(TeacherRegistrationRequest registrationRequest) {
        logger.info("UserService: Yeni öğretmen kaydı talebi - Email: {}", registrationRequest.getEmail());

        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            logger.warn("UserService: Öğretmen kaydı başarısız - E-posta adresi zaten kullanımda: {}", registrationRequest.getEmail());
            throw new DuplicateEmailException("E-posta adresi zaten kullanımda: " + registrationRequest.getEmail());
        }

        Teacher newTeacher = new Teacher();
        newTeacher.setName(registrationRequest.getName());
        newTeacher.setSurname(registrationRequest.getSurname());
        newTeacher.setEmail(registrationRequest.getEmail());
        newTeacher.setUsername(registrationRequest.getUsername()); 
        newTeacher.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newTeacher.setRole("ROLE_TEACHER"); 
        newTeacher.setAge(registrationRequest.getAge());

       
        newTeacher.setSubject(registrationRequest.getSubject());
        newTeacher.setGraduateSchool(registrationRequest.getGraduateSchool());
        newTeacher.setDiplomaNumber(registrationRequest.getDiplomaNumber());

        newTeacher.setCreatedDate(LocalDateTime.now());
        newTeacher.setUpdatedDate(LocalDateTime.now());
        newTeacher.setActive(true);
        newTeacher.setEnabled(false); 

        User savedTeacher = userRepository.save(newTeacher);
        logger.info("UserService: Öğretmen kayıt talebi başarıyla alındı - ID: {}, Email: {}", savedTeacher.getId(), savedTeacher.getEmail());
        String emailSubject = "Öğretmenlik Başvurunuz Alındı";
        String emailBody = "Merhaba " + savedTeacher.getName() + ",\n\nÖğretmenlik başvurunuz alınmıştır ve değerlendirme sürecindedir. " +
                           "Onaylandığında veya reddedildiğinde size bilgi verilecektir.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";
        sendTeacherStatusUpdateEmail(savedTeacher.getEmail(), emailSubject, emailBody, "kayıt beklemede");

        return savedTeacher;
    }

    @Transactional
    public User reviewTeacherRequest(int userId, boolean approve) {
        logger.info("UserService: Öğretmen başvurusu inceleniyor - ID: {}, Onay: {}", userId, approve);
        User teacherToReview = userRepository.findById(userId)
                                .orElseThrow(() -> 
                                    new UserNotFoundException("ID " + userId + " olan öğretmen bulunamadı.")
                                );

        if (!teacherToReview.getRole().equals("ROLE_TEACHER")) {
            logger.warn("UserService: Kullanıcı öğretmen değil - ID: {}", userId);
            throw new IllegalArgumentException("ID " + userId + " olan kullanıcı bir öğretmen değil.");
        }

        if (approve) {
            teacherToReview.setEnabled(true);
            teacherToReview.setUpdatedDate(LocalDateTime.now());
            String emailSubject = "Öğretmenlik Başvurunuz Onaylandı";
            String emailBody = "Merhaba " + teacherToReview.getName() + ",\n\nÖğretmenlik başvurunuz incelenmiş ve onaylanmıştır. Artık sisteme giriş yapabilir ve öğretmen fonksiyonlarını kullanabilirsiniz.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";
            sendTeacherStatusUpdateEmail(teacherToReview.getEmail(), emailSubject, emailBody, "onay");
            logger.info("UserService: Öğretmen başvurusu onaylandı - ID: {}", userId);
        } else {
            
            teacherToReview.setUpdatedDate(LocalDateTime.now()); 
            String emailSubject = "Öğretmenlik Başvurunuz Reddedildi";
            String emailBody = "Merhaba " + teacherToReview.getName() + ",\n\nÖğretmenlik başvurunuz incelenmiş ve maalesef reddedilmiştir.\n\nDaha fazla bilgi için lütfen iletişime geçin.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";
            sendTeacherStatusUpdateEmail(teacherToReview.getEmail(), emailSubject, emailBody, "ret");
            logger.info("UserService: Öğretmen başvurusu reddedildi - ID: {}", userId);
        }

        return userRepository.save(teacherToReview);
    }

    private void sendTeacherStatusUpdateEmail(String toEmail, String subject, String body, String actionTypeLog) {
        try {
            mailService.sendEmail(toEmail, subject, body);
            logger.info("UserService: Öğretmen başvuru {} e-postası gönderildi - Email: {}", actionTypeLog, toEmail);
        } catch (Exception e) {
            logger.error("UserService: Öğretmen başvuru {} e-postası gönderilemedi - Email: {}: {}", actionTypeLog, toEmail, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingTeacherRequests() {
        logger.info("UserService: Bekleyen öğretmen kayıt talepleri getiriliyor.");
        List<User> teachers = userRepository.findAllByRole("ROLE_TEACHER");
        List<UserResponse> pendingTeachers = teachers.stream()
                .filter(user -> !user.isEnabled())
                .filter(user -> user instanceof Teacher)
                .map(UserResponse::new)
                .collect(Collectors.toList());
        logger.info("UserService: {} adet bekleyen öğretmen kaydı bulundu.", pendingTeachers.size());
        return pendingTeachers;
    }

    @Transactional(readOnly = true)
    public UserDetailsResponse getUserById(int id) {
        logger.info("UserService: ID ile kullanıcı getiriliyor - ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı - ID: " + id));
            
        return convertToUserDetailsResponse(user);
    }
    
    
    public UserDetailsResponse getUserByUsername(String username) {
        logger.info("UserService: Kullanıcı adı ile kullanıcı getiriliyor - Kullanıcı Adı: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı - Kullanıcı Adı: " + username));
            
        return convertToUserDetailsResponse(user);
    }
    
    
    private UserDetailsResponse convertToUserDetailsResponse(User user) {
        if (user == null) {
            return null;
        }
        
        UserDetailsResponse response = new UserDetailsResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setEnabled(user.isEnabled());
        response.setAge(user.getAge());
        response.setCreatedDate(user.getCreatedDate());
        response.setUpdatedDate(user.getUpdatedDate());
        
        
        if (user instanceof Student) {
            Student student = (Student) user;
            response.setSchoolName(student.getSchoolName());
            logger.debug("UserService: Öğrencinin okul bilgisi eklendi - Kullanıcı ID: {}, Okul: {}", 
                    user.getId(), student.getSchoolName());
        }
        
        
        if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            response.setSubject(teacher.getSubject());
            response.setGraduateSchool(teacher.getGraduateSchool());
            response.setDiplomaNumber(teacher.getDiplomaNumber());
            logger.debug("UserService: Öğretmen özel bilgileri eklendi - Kullanıcı ID: {}, Ders: {}", 
                    user.getId(), teacher.getSubject());
        }
        
        return response;
    }

    
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        logger.info("UserService: Tüm kullanıcılar getiriliyor.");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    
    @Transactional(readOnly = true)
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

        userToUpdate.setRole(newRole);
        userToUpdate.setUpdatedDate(LocalDateTime.now());
        User savedUser = userRepository.save(userToUpdate);
        logger.info("UserService: Kullanıcı rolü başarıyla değiştirildi - ID: {}, Yeni Rol: {}", userId, newRole);
        return savedUser;
    }

    
    public User updateUser(int userId, UserUpdateRequest updateRequest) {
        logger.info("UserService: Kullanıcı güncelleniyor - ID: {}", userId);
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> {
                                              logger.warn("UserService: Güncellenecek kullanıcı bulunamadı - ID: {}", userId);
                                              return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                          });

     
        boolean hasUpdate = false;

        if (updateRequest.getName() != null && !updateRequest.getName().isEmpty()) {
            userToUpdate.setName(updateRequest.getName());
            hasUpdate = true;
        }

        if (updateRequest.getSurname() != null && !updateRequest.getSurname().isEmpty()) {
            userToUpdate.setSurname(updateRequest.getSurname());
            hasUpdate = true;
        }

        if (updateRequest.getAge() != null) {
            userToUpdate.setAge(updateRequest.getAge());
            hasUpdate = true;
        }

        if (hasUpdate) {
            userToUpdate.setUpdatedDate(LocalDateTime.now());
            userToUpdate = userRepository.save(userToUpdate);
            logger.info("UserService: Kullanıcı başarıyla güncellendi - ID: {}", userId);
        } else {
            logger.info("UserService: Güncellenecek bir bilgi bulunamadı - ID: {}", userId);
        }
        return userToUpdate;
    }

    
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
        userToUpdate.setPassword(encodedNewPassword);
        userToUpdate.setUpdatedDate(LocalDateTime.now());
        User savedUser = userRepository.save(userToUpdate);
        logger.info("UserService: Kullanıcı parolası başarıyla değiştirildi - ID: {}", userId);
        return savedUser;
        
    }
    
    public User changeUserEmail(int userId, EmailChangeRequest emailChangeRequest, boolean isAdminAction) {
        logger.info("UserService: Kullanıcı e-postası değiştiriliyor - ID: {}, Yeni E-posta: {}", userId, emailChangeRequest.getNewEmail());
        User userToUpdate = userRepository.findById(userId)
                                          .orElseThrow(() -> {
                                              logger.warn("UserService: E-postası değiştirilecek kullanıcı bulunamadı - ID: {}", userId);
                                              return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                          });

        
        if (!isAdminAction && !passwordEncoder.matches(emailChangeRequest.getPassword(), userToUpdate.getPassword())) {
            logger.warn("UserService: Parola doğrulanamadı - ID: {}", userId);
            throw new IllegalArgumentException("Parola hatalı.");
        }

        
        if (userRepository.findByEmail(emailChangeRequest.getNewEmail()).isPresent()) {
            logger.warn("UserService: E-posta değiştirme başarısız - E-posta adresi zaten kullanımda: {}", emailChangeRequest.getNewEmail());
            throw new DuplicateEmailException("E-posta adresi zaten kullanımda: " + emailChangeRequest.getNewEmail());
        }

        userToUpdate.setEmail(emailChangeRequest.getNewEmail());
        userToUpdate.setUpdatedDate(LocalDateTime.now());
        User savedUser = userRepository.save(userToUpdate);
        logger.info("UserService: Kullanıcı e-postası başarıyla değiştirildi - ID: {}, Yeni E-posta: {}", userId, emailChangeRequest.getNewEmail());
        return savedUser;
    }

    
    public void softDeleteUser(int userId) {
        logger.info("UserService: Kullanıcı siliniyor (soft delete) - ID: {}", userId);
        User userToDelete = userRepository.findById(userId)
                                          .orElseThrow(() -> {
                                              logger.warn("UserService: Silinecek kullanıcı bulunamadı - ID: {}", userId);
                                              return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
                                          });

        userToDelete.setActive(false);
        userToDelete.setUpdatedDate(LocalDateTime.now());
        userRepository.save(userToDelete);
        logger.info("UserService: Kullanıcı başarıyla silindi (soft delete) - ID: {}", userId);
    }
    
   
    public User findUserByUsername(String username) {
        logger.info("UserService: Kullanıcı adı ile kullanıcı entity'si getiriliyor - Kullanıcı Adı: {}", username);
        
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.warn("UserService: Kullanıcı bulunamadı - Kullanıcı Adı: {}", username);
                return new UserNotFoundException("Kullanıcı bulunamadı - Kullanıcı Adı: " + username);
            });
    }
    
    
    public boolean existsByEmail(String email) {
        logger.info("UserService: Email adresi kullanımda mı kontrol ediliyor - Email: {}", email);
        return userRepository.findByEmail(email).isPresent();
    }
    
   
    public User saveUser(User user) {
        logger.info("UserService: Kullanıcı kaydediliyor - ID: {}", user.getId());
        return userRepository.save(user);
    }
    
    
    public User findUserByEmail(String email) {
        logger.info("UserService: Email adresine göre kullanıcı aranıyor - Email: {}", email);
        return userRepository.findByEmail(email).orElse(null);
    }
    
   
    public User findUserByResetPasswordToken(String token) {
        logger.info("UserService: Şifre sıfırlama token'ina göre kullanıcı aranıyor - Token: {}", token);
        return userRepository.findByResetPasswordToken(token).orElse(null);
    }
    
    
    @Transactional
    public User toggleUserActivation(int userId) {
        logger.info("UserService: Kullanıcı aktiflik durumu değiştiriliyor - ID: {}", userId);
        
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.warn("UserService: Kullanıcı bulunamadı - ID: {}", userId);
                return new UserNotFoundException("ID " + userId + " olan kullanıcı bulunamadı.");
            });
        
        
        if (user.getRole().equals("ROLE_ADMIN")) {
            logger.warn("UserService: Admin kullanıcı devre dışı bırakılamaz - ID: {}", userId);
            throw new IllegalArgumentException("Admin kullanıcılar devre dışı bırakılamaz.");
        }
        
        
        boolean newStatus = !user.isEnabled();
        user.setEnabled(newStatus);
        user.setUpdatedDate(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        logger.info("UserService: Kullanıcı aktiflik durumu güncellendi - ID: {}, Yeni Durum: {}", 
                   userId, newStatus ? "Aktif" : "Pasif");
        
        return updatedUser;
    }
}