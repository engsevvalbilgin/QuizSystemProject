package com.example.QuizSystemProject.dto;

import java.util.List;
import com.example.QuizSystemProject.Model.User; // User modelini ekliyoruz
import com.example.QuizSystemProject.dto.UserDto; // UserDto import ediyoruz

// Bu DTO, API yanıtlarında başarılı kimlik doğrulama (login) sonucunu taşır.
// Kullanıcı bilgileri ve erişim token'ı içerir.
public class AuthResponseDto {

    private int userId;
    private String username;
    private List<String> roles; // Kullanıcının rolleri (örn: ["ROLE_STUDENT", "ROLE_ADMIN"])
    private String token; // JWT access token
    private String refreshToken; // Refresh token for getting new access tokens
    private UserDto user; // Complete user object

    // İsteğe bağlı olarak token tipi (örn: "Bearer") veya token'ın son kullanma süresi de eklenebilir.
    // private String tokenType = "Bearer";
    

    // JPA için argümansız constructor (Spring genellikle buna ihtiyaç duymaz ama iyi practice'dir)
    

    // Constructor with all fields
    public AuthResponseDto(int userId, String username, List<String> roles, String token, String refreshToken, UserDto user) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }
    
    // Constructor without user (for backward compatibility)
    public AuthResponseDto(int userId, String username, List<String> roles, String token, String refreshToken) {
        this(userId, username, roles, token, refreshToken, null);
    }
    
    // Constructor without refreshToken (for backward compatibility)
    public AuthResponseDto(int userId, String username, List<String> roles, String token) {
        this(userId, username, roles, token, null);
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }

    // Eğer tokenType eklediyseniz:
    // public String getTokenType() { return tokenType; }
    // public void setTokenType(String tokenType) { this.tokenType = tokenType; }



    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}