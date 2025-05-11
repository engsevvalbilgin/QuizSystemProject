package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.Email; // Email formatı için
import jakarta.validation.constraints.Min; // Minimum değer için
import jakarta.validation.constraints.Size; // Uzunluk kontrolü için

// Bu DTO, kullanıcı bilgilerini güncellemek için kullanılır.
// Boş veya null değerlere izin vermek, kısmi güncellemeleri (patch gibi) kolaylaştırır.
// Ancak alanları zorunlu yapmak isterseniz @NotBlank veya @NotNull ekleyebilirsiniz.
public class UserUpdateRequest {

    @Size(max = 50, message = "Ad 50 karakterden uzun olamaz")
    private String name; // Boş veya null olabilir

    @Size(max = 50, message = "Soyad 50 karakterden uzun olamaz")
    private String surname; // Boş veya null olabilir

    @Min(value = 0, message = "Yaş negatif olamaz")
    private Integer age; // primitive int yerine Integer kullanıyoruz, böylece null olabilir

    @Email(message = "Geçerli bir email formatı girin") // Geçerli email formatı kontrolü
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz")
    private String email; // Boş veya null olabilir

    @Size(min = 3, max = 50, message = "Kullanıcı adı 3 ile 50 karakter arasında olmalı")
    private String username; // Boş veya null olabilir

    // isActive alanı genellikle Admin tarafından güncellenir, ama DTO'ya dahil edelim.
    // Boş veya null olabilir (eğer istekte bu alan gelmiyorsa)
    private Boolean isActive; // primitive boolean yerine Boolean kullanıyoruz, böylece null olabilir


    // Getter ve Setterlar
    public UserUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Boolean isActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }
}