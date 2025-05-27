package com.example.QuizSystemProject.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.UserRepository;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT token işlemleri için yardımcı sınıf
 */
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * JWT token'dan kullanıcı adını alır
     * 
     * @param token JWT token
     * @return Kullanıcı adı
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * JWT token'dan öğrenci ID'sini alır
     * 
     * @param token JWT token
     * @return Öğrenci ID'si
     * @throws RuntimeException if the user ID cannot be extracted from the token or found in the database
     */
    public int getStudentIdFromToken(String token) {
        try {
            // First try to get the user ID from the token claims
            Claims claims = getAllClaimsFromToken(token);
            Object userId = claims.get("userId");
            
            // If userId is found in the token, return it
            if (userId != null) {
                try {
                    if (userId instanceof Integer) {
                        return (Integer) userId;
                    } else {
                        return Integer.parseInt(userId.toString());
                    }
                } catch (NumberFormatException e) {
                    // Continue to fallback method if parsing fails
                }
            }
            
            // Fallback: If user ID is not in the token, try to get it from the username
            String username = getUsernameFromToken(token);
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
                
            if (user != null) {
                return user.getId();
            }
            
            throw new RuntimeException("Kullanıcı veritabanında bulunamadı: " + username);
            
        } catch (Exception e) {
            throw new RuntimeException("Token'dan kullanıcı ID'si alınamadı: " + e.getMessage(), e);
        }
    }

    /**
     * Token'ın geçerlilik süresini kontrol eder
     * 
     * @param token JWT token
     * @return Token geçerli mi
     */
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    // Private metotlar
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Token işlenirken bir hata oluştu: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a signing key from the secret key.
     * Handles both Base64-encoded and plain text keys.
     * @return Key object for signing tokens
     */
    private Key getSigningKey() {
        try {
            // First try to decode as Base64
            byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            // If decoding fails, use the key as-is (plain text)
            System.out.println("Using plain text JWT secret key");
            return Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
}
