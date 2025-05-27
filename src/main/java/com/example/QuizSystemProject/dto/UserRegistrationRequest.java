package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.Email; // Email formatı için
import jakarta.validation.constraints.Min; // Minimum değer için
import jakarta.validation.constraints.NotBlank; // Boş olamaz kontrolü için
import jakarta.validation.constraints.Size; // Uzunluk kontrolü için

// Bu bir DTO (Data Transfer Object) sınıfıdır. Entity değildir.
// Kullanıcı kayıt isteği için kullanılır ve gerekli alanları taşır.
public class UserRegistrationRequest {

    @NotBlank(message = "Kullanıcı adı boş olamaz") // Kullanıcı adı alanı boş veya sadece boşluklardan oluşamaz
    @Size(min = 3, max = 50, message = "Kullanıcı adı 3 ile 50 karakter arasında olmalı") // Uzunluk kısıtlaması
    private String username;

    @NotBlank(message = "Parola boş olamaz") // Parola alanı boş olamaz
    @Size(min = 6, message = "Parola en az 6 karakter olmalı") // Minimum uzunluk kısıtlaması
    private String password; // Parola, DTO'da ham (plain) olarak gelir, Service'te şifrelenir.

    @NotBlank(message = "Email boş olamaz") // Email alanı boş olamaz
    @Email(message = "Geçerli bir email formatı girin") // Geçerli email formatı kontrolü
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz") // Maksimum uzunluk
    private String email;

    @NotBlank(message = "Ad boş olamaz")
    @Size(max = 50, message = "Ad 50 karakterden uzun olamaz")
    private String name;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(max = 50, message = "Soyad 50 karakterden uzun olamaz")
    private String surname;

    // Yaş alanı ve validasyon kuralı (pozitif sayı olmalı)
    @Min(value = 0, message = "Yaş negatif olamaz") // Minimum 0 olmalı
    // Eğer yaş alanı zorunlu ise ve primitive int yerine Integer kullansaydık @NotNull da ekleyebilirdik.
    private int age; // Yaş alanı

    @NotBlank(message = "Okul ismi boş olamaz")
    @Size(max = 100, message = "Okul ismi 100 karakterden uzun olamaz")
    private String schoolName; // Öğrencinin okul ismi

    // --- Constructorlar ---
    // Argümansız constructor (Spring'in JSON'ı objeye dönüştürmesi için gerekli)
    public UserRegistrationRequest() {
    }

    // Alanları alan constructor (isteğe bağlı, testlerde veya manuel oluştururken faydalı)
    public UserRegistrationRequest(String username, String password, String email, String name, String surname, int age, String schoolName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.schoolName = schoolName;
    }

    // --- Getter ve Setter Metotları ---
    // IDE ile otomatik oluşturabilirsiniz.

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}