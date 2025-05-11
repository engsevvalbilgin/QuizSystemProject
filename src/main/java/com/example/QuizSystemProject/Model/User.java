package com.example.QuizSystemProject.Model; // Paket adınızın doğru olduğundan emin olun
import jakarta.validation.constraints.Size;
import jakarta.persistence.*; // JPA anotasyonları için
import java.time.LocalDateTime; // Tarih/saat için modern Java API'si
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // equals/hashCode için

@Entity // Bu sınıfın bir JPA Entity'si olduğunu ve veritabanı tablosuna karşılık geldiğini belirtir
@Table(name = "users") // Veritabanındaki tablonun adı 'users' olacak
public abstract class User {

    @Id // Bu alanın birincil anahtar (Primary Key) olduğunu belirtir
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID'nin veritabanı tarafından otomatik artan olarak üretileceğini belirtir
    private int id; // JPA'de ID'ler için Long kullanmak yaygın ve önerilir

    @Column(nullable = false, length = 50) // Boş olamaz ve maksimum 50 karakter olabilir
    private String name;

    @Column(nullable = false, length = 50) // Boş olamaz ve maksimum 50 karakter olabilir
    private String surname;

    @Column(nullable = false) // Boş olamaz
    private int age; // Yaş

    @Column(nullable = false, unique = true, length = 100) // Boş olamaz, benzersiz olmalı ve maksimum 100 karakter olabilir
    private String email;

    @Column(nullable = false, unique = true, length = 50) // Boş olamaz, benzersiz olmalı ve maksimum 50 karakter olabilir
    private String username;

    @Column(nullable = false) // Boş olamaz. Parolanın hash'i burada saklanacak.
    // Length'i BCrypt gibi bir hash algoritması için yeterli uzunlukta ayarlamak gerekir.
    // BCrypt genellikle 60 karakterdir, biraz fazlasını verelim.
    @Size(max = 60) // Parola hash'inin maksimum uzunluğu
    private String password; // NOT: Bu alanda parolanın şifrelenmiş (hashed) hali saklanacak!

    @Column(nullable = false) // Boş olamaz. Kullanıcının rolü (Student, Teacher, Admin)
    private String role; // Örn: "ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN"

    @Column(nullable = false) // E-posta doğrulaması veya Admin tarafından devre dışı bırakılma durumu
    private boolean enabled = false; // Başlangıçta varsayılan olarak pasif (e-posta doğrulaması gerekebilir)

    // Oluşturulma tarihi (otomatik olarak veritabanında veya uygulamada ayarlanabilir)
    @Column(nullable = false)
    private LocalDateTime createdDate; // java.util.Date yerine LocalDateTime kullanıyoruz

    // Son güncellenme tarihi (otomatik olarak veritabanında veya uygulamada ayarlanabilir)
    @Column(nullable = false)
    private LocalDateTime updatedDate; // java.util.Date yerine LocalDateTime kullanıyoruz

    @Column(nullable = false) // Kullanıcının aktif mi (silinmemiş mi) durumu
    private boolean isActive = true; // Varsayılan olarak aktif

    // --- Yeni Alanlar: E-posta Doğrulama ve Parola Sıfırlama Tokenları ---

    @Column(nullable = true) // Bu token her zaman olmayacağı için boş bırakılabilir (nullable)
    private String confirmationToken; // E-posta doğrulama token'ı

    @Column(nullable = true) // Bu token'ın son kullanma tarihi (null olabilir)
    private LocalDateTime confirmationTokenExpiryDate; // E-posta doğrulama token'ının son kullanma tarihi

    @Column(nullable = true) // Parola sıfırlama token'ı (null olabilir)
    private String resetPasswordToken; // Parola sıfırlama token'ı

    @Column(nullable = true) // Parola sıfırlama token'ının son kullanma tarihi (null olabilir)
    private LocalDateTime resetPasswordTokenExpiryDate; // Parola sıfırlama token'ının son kullanma tarihi


    // JPA, argümansız (boş) bir constructor gerektirir
    public User() {
    }

    // Temel alanları alan constructor (Yeni alanlar hariç)
    // Güvenlik için parola DTO'dan gelip Service'te şifrelenmeli.
    public User(String name, String surname, int age, String email, String username, String password, String role) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.email = email;
        this.username = username;
        this.password = password; // NOT: Bu constructoru kullanırken parolanın şifrelenmiş hali gelmeli veya burada şifrelenmeli
        this.role = role;
        this.enabled = false; // Varsayılan
        this.isActive = true; // Varsayılan
        this.createdDate = LocalDateTime.now(); // Oluşturulma anı
        this.updatedDate = LocalDateTime.now(); // Güncellenme anı
        // Yeni token alanları bu constructor'da set edilmez, ilgili akışlarda set edilir.
    }


    // Getter ve Setter Metotları (Tüm alanlar için - yeni eklenenler dahil)
    // IDE ile otomatik olarak Generate Getters and Setters diyerek tüm alanlar için oluşturun.
    // Aşağıda yeni eklenenlerin getter/setterları örnek olarak verilmiştir.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // ID setter'ı genellikle kullanılmaz (otomatik üretilir)

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; } // Dikkat: Şifrelenmiş parola hash'ini döndürür
    public void setPassword(String password) { this.password = password; } // Dikkat: Buraya şifrelenmiş parola hash'i set edilmeli

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // --- Yeni Alanlar Getter/Setterları ---
    public String getConfirmationToken() { return confirmationToken; }
    public void setConfirmationToken(String confirmationToken) { this.confirmationToken = confirmationToken; }

    public LocalDateTime getConfirmationTokenExpiryDate() { return confirmationTokenExpiryDate; }
    public void setConfirmationTokenExpiryDate(LocalDateTime confirmationTokenExpiryDate) { this.confirmationTokenExpiryDate = confirmationTokenExpiryDate; }

    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public LocalDateTime getResetPasswordTokenExpiryDate() { return resetPasswordTokenExpiryDate; }
    public void setResetPasswordTokenExpiryDate(LocalDateTime resetPasswordTokenExpiryDate) { this.resetPasswordTokenExpiryDate = resetPasswordTokenExpiryDate; }


    // equals() ve hashCode() metotları (ID üzerinden yapılır)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString() metodu (Debugging için, hassas bilgileri dahil etmeyin)
    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", role='" + role + '\'' +
               ", enabled=" + enabled +
               ", isActive=" + isActive +
               ", confirmationToken=" + (confirmationToken != null ? "present" : "null") + // Token değerini gösterme
               ", confirmationTokenExpiryDate=" + confirmationTokenExpiryDate +
               ", resetPasswordToken=" + (resetPasswordToken != null ? "present" : "null") + // Token değerini gösterme
               ", resetPasswordTokenExpiryDate=" + resetPasswordTokenExpiryDate +
               '}';
    }

	public void logIn(User user) {
		// TODO Auto-generated method stub
		
	}

	public void signIn(User user) {
		// TODO Auto-generated method stub
		
	}

	public void logOut(User user) {
		// TODO Auto-generated method stub
		
	}

	protected abstract void showUserDetails();

	
}
