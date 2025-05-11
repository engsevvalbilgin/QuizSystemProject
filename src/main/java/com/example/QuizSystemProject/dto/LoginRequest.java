package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.NotBlank; // Boş olamaz kontrolü için

// Bu bir DTO (Data Transfer Object) sınıfıdır. Entity değildir.
// Sadece API isteklerinde/yanıtlarında veri taşımak için kullanılır.
public class LoginRequest {

    // Kullanıcı adı veya e-posta olabilir
    @NotBlank(message = "Kullanıcı adı veya email boş olamaz")
    private String usernameOrEmail;

    @NotBlank(message = "Şifre boş olamaz")
    private String password; // Parola, DTO'da ham olarak gelir.

    // DTO'lar için genellikle argümansız constructor ve getter/setter metotları yeterlidir.

    public LoginRequest() {
    }

    // Alanları alan constructor (isteğe bağlı)
    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    // Getter ve Setter Metotları
    // IDE ile otomatik oluşturabilirsiniz.

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}