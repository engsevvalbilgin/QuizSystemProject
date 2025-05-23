package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.User; // Entity'den dönüşüm için User Entity'sini import edin
import com.example.QuizSystemProject.Model.Student; // Student spesifik alanlar için
import com.example.QuizSystemProject.Model.Teacher; // Teacher spesifik alanlar için
import java.time.LocalDateTime; // Tarih/saat için

// Bu DTO, API yanıtlarında bir kullanıcının detaylı bilgilerini taşır.
// UserResponse'a göre daha fazla alan içerir, hassas bilgiler (parola gibi) dahil EDİLMEZ.
public class UserDetailsResponse {

    private int id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String role; // Kullanıcı rolü
    private boolean isActive; // Hesabın aktif (silinmemiş) olup olmadığı
    private boolean enabled; // Hesabın etkinleştirilmiş (e-posta doğrulaması yapılmış) olup olmadığı

    private int age; // Kullanıcı yaşı
    private LocalDateTime createdDate; // Oluşturulma tarihi
    private LocalDateTime updatedDate; // Son güncellenme tarihi
    
    private String schoolName; // Öğrencinin okulu (Öğrenci rolü için)
    
    // Teacher (Ogretmen) rolü için alanlar
    private String subject; // Öğretmenin dersi/branşı
    private String graduateSchool; // Mezun olduğu okul
    private String diplomaNumber; // Diploma numarası

    // JPA için argümansız constructor
    public UserDetailsResponse() {
    }

    // User Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public UserDetailsResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.isActive = user.isActive(); // Getter metodu isActive()
        this.enabled = user.isEnabled(); // Getter metodu isEnabled()
        this.age = user.getAge();
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();
        
        // Student tipindeki kullanıcılar için okul bilgisini ekle
        if (user instanceof Student) {
            this.schoolName = ((Student) user).getSchoolName();
        }
        
        // Teacher tipindeki kullanıcılar için öğretmen bilgilerini ekle
        if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            this.subject = teacher.getSubject();
            this.graduateSchool = teacher.getGraduateSchool();
            this.diplomaNumber = teacher.getDiplomaNumber();
        }
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    
    // Teacher-specific field getters and setters
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getGraduateSchool() { return graduateSchool; }
    public void setGraduateSchool(String graduateSchool) { this.graduateSchool = graduateSchool; }
    
    public String getDiplomaNumber() { return diplomaNumber; }
    public void setDiplomaNumber(String diplomaNumber) { this.diplomaNumber = diplomaNumber; }

    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}