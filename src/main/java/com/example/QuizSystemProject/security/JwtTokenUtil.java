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


@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;
    
    @Autowired
    private UserRepository userRepository;

    
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public int getStudentIdFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Object userId = claims.get("userId");
            
            if (userId != null) {
                try {
                    if (userId instanceof Integer) {
                        return (Integer) userId;
                    } else {
                        return Integer.parseInt(userId.toString());
                    }
                } catch (NumberFormatException e) {
                }
            }
            
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

    
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

   
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
    
   
    private Key getSigningKey() {
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
       
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
