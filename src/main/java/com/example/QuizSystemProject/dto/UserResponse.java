package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.User; // Entity'den dönüşüm için User Entity'sini import edin
import java.time.LocalDateTime;
// Bu DTO, API yanıtlarında temel kullanıcı bilgilerini taşır (örn: kullanıcı listeleri).
// Hassas bilgiler (parola gibi) dahil EDİLMEZ.
public class UserResponse {

    private int id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String role; // Kullanıcı rolü
    private boolean isActive; // Hesabın aktif (silinmemiş) olup olmadığı
    private boolean enabled; // Hesabın etkinleştirilmiş (e-posta doğrulaması yapılmış) olup olmadığı
    private LocalDateTime createdAt; // Kullanıcının oluşturulma tarihi

    // Yaş, oluşturulma/güncellenme tarihleri gibi alanları liste DTO'suna dahil etmeyebiliriz
    // veya isteğe bağlı olarak ekleyebiliriz. Şimdilik temel alanları alalım.

    // JPA için argümansız constructor (Spring genellikle buna ihtiyaç duymaz ama iyi practice'dir)
    public UserResponse() {
    }

    // User Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    // Service veya Controller katmanında Entity'i bu constructor'a vererek DTO objesi oluşturabiliriz
    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.isActive = user.isActive(); // Getter metodu isActive()
        this.enabled = user.isEnabled(); // Getter metodu isEnabled()
        this.createdAt = user.getCreatedDate(); // Kullanıcının oluşturulma tarihi
    }

    // Getter ve Setterlar (Setterlar API'den veri almadığı için zorunlu değildir ama eklenebilir)
    // IDE ile otomatik oluşturabilirsiniz.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // Yanıt DTO'larında setterlar genellikle kullanılmaz ama bulunabilir

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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}