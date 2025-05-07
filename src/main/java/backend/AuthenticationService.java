package backend;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;

public class AuthenticationService {
    private String sessionId;
    private Date lastLoginDate;
    private boolean isActive;
    
    public AuthenticationService() {
        this.sessionId = null;
        this.lastLoginDate = null;
        this.isActive = false;
    }   
    
    public User login(String email, String password) {
        // Örnek: Basit kontrol ve session set etme
        this.sessionId = "session_" + System.currentTimeMillis();
        this.lastLoginDate = new Date();
        this.isActive = true;
        return new User();  
    }
    
    public boolean signIn(User user) {
        this.sessionId = "session_" + System.currentTimeMillis();
        this.isActive = true;
        return true;
    }
    
    public boolean logout(int userId) {
        this.sessionId = null;
        this.isActive = false;
        return true;
    }
    
    public boolean resetPassword(String email) {
        // Şifre sıfırlama işlemi simülasyonu
        return true;
    }
    
    public boolean verifyEmail(String token) {
        // Token doğrulama simülasyonu
        return true;
    }
    
    public boolean isLoggedIn(int userId) {
        return this.isActive;
    }
    
    public Map<String, Object> getSessionDetails(String sessionId) {
        Map<String, Object> details = new HashMap<>();
        details.put("sessionId", this.sessionId);
        details.put("lastLoginDate", this.lastLoginDate);
        details.put("isActive", this.isActive);
        return details;
    }
}

