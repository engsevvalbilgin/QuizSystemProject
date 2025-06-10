package com.example.QuizSystemProject.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.QuizSystemProject.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:900000}") 
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-expiration:604800000}") 
    private long refreshTokenExpiration;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ROLES_CLAIM = "roles";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

   
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

   
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    
    private Key getSigningKey() {
        try {
          
            byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            
            System.out.println("Using plain text JWT secret key");
            return Keys.hmacShaKeyFor(secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    
    public String generateToken(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES_CLAIM, userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        
        if (userDetails instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) userDetails;
           
            com.example.QuizSystemProject.Model.User userEntity = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + user.getUsername()));
            claims.put("userId", userEntity.getId());
        }
        
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    
    private String createToken(Map<String, Object> claims, String subject, long expirationMillis) {
        if (claims == null) {
            claims = new HashMap<>();
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("Expiration must be greater than 0");
        }
        
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token: " + e.getMessage(), e);
        }
    }

    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

   
    public boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null || token.trim().isEmpty() || userDetails == null) {
            return false;
        }
        
        try {
            final String username = extractUsername(token);
            final boolean isValid = (username != null && 
                                  username.equals(userDetails.getUsername()) && 
                                  !isTokenExpired(token));
            
            if (!isValid) {
                System.err.println("Token validation failed for user: " + userDetails.getUsername() + 
                                 ", token username: " + username + 
                                 ", is expired: " + isTokenExpired(token));
            }
            
            return isValid;
        } catch (Exception ex) {
            System.err.println("Error validating token: " + ex.getMessage());
            return false;
        }
    }
    
   
    public boolean validateRefreshToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            System.err.println("Refresh token is null or empty");
            return false;
        }
        
        try {
            Claims claims = extractAllClaims(token);
            
            
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
                System.err.println("Invalid token type: " + tokenType);
                return false;
            }
            
         
            boolean notExpired = !isTokenExpired(token);
            if (!notExpired) {
                System.err.println("Refresh token has expired");
            }
            return notExpired;
            
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            System.err.println("Refresh token expired: " + ex.getMessage());
            return false;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            System.err.println("Invalid refresh token: " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            System.err.println("Unexpected error validating refresh token: " + ex.getMessage());
            return false;
        }
    }
    
   
    public String generateRefreshToken(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES_CLAIM, userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        
        return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }
}