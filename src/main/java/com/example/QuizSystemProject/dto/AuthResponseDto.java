package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import java.util.List; // Rol listesi için

// Bu DTO, API yanıtlarında başarılı kimlik doğrulama (login) sonucunu taşır.
// Kullanıcı bilgileri ve erişim token'ı içerir.
public class AuthResponseDto {

    private Long userId;
    private String username;
    private List<String> roles; // Kullanıcının rolleri (örn: ["ROLE_STUDENT", "ROLE_ADMIN"])
    private String token; // JWT veya başka bir erişim token'ı

    // İsteğe bağlı olarak token tipi (örn: "Bearer") veya token'ın son kullanma süresi de eklenebilir.
    // private String tokenType = "Bearer";
    // private Long expiresIn;

    // JPA için argümansız constructor (Spring genellikle buna ihtiyaç duymaz ama iyi practice'dir)
    public AuthResponseDto() {
    }

    // Temel alanları alan constructor
    public AuthResponseDto(Long userId, String username, List<String> roles, String token) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.token = token;
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    // Eğer tokenType eklediyseniz:
    // public String getTokenType() { return tokenType; }
    // public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    // Eğer expiresIn eklediyseniz:
    // public Long getExpiresIn() { return expiresIn; }
    // public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}