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

/**
 * Utility class for handling JWT token operations including generation, validation, and extraction of claims.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:900000}") // 15 minutes default
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshTokenExpiration;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ROLES_CLAIM = "roles";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    /**
     * Extracts the username from the given JWT token.
     * @param token JWT token
     * @return username from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the token.
     * @param token JWT token
     * @param claimsResolver function to extract the claim
     * @param <T> type of the claim
     * @return the claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the token.
     * @param token JWT token
     * @return Claims object containing all token claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Creates a signing key from the secret key.
     * Handles both Base64-encoded and plain text keys.
     * @return Key object for signing tokens
     */
    private Key getSigningKey() {
        try {
            // First try to decode as Base64
            byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            // If decoding fails, use the key as-is (plain text)
            System.out.println("Using plain text JWT secret key");
            return Keys.hmacShaKeyFor(secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Generates a new access token for the given user details.
     * @param userDetails user details
     * @return JWT token as string
     */
    /**
     * Generates a new access token for the given user details.
     * @param userDetails user details
     * @return JWT access token as string
     */
    public String generateToken(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES_CLAIM, userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        // Add user ID to claims if available (when UserDetails is an instance of User)
        if (userDetails instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) userDetails;
            // Get the actual user entity from the database to get the ID
            com.example.QuizSystemProject.Model.User userEntity = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + user.getUsername()));
            claims.put("userId", userEntity.getId());
        }
        
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    /**
     * Creates a new JWT token with the given claims and expiration.
     * @param claims token claims
     * @param subject token subject (usually username)
     * @param expiration expiration time in milliseconds
     * @return JWT token as string
     */
    /**
     * Creates a new JWT token with the given claims and expiration.
     * @param claims token claims
     * @param subject token subject (usually username)
     * @param expirationMillis expiration time in milliseconds
     * @return JWT token as string
     */
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

    /**
     * Extracts the expiration date from the token.
     * @param token JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if the token is expired.
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * Validates if the token is valid for the given user details.
     * @param token JWT token
     * @param userDetails user details
     * @return true if token is valid, false otherwise
     */
    /**
     * Validates if the token is valid for the given user details.
     * @param token JWT token to validate
     * @param userDetails user details to validate against
     * @return true if token is valid, false otherwise
     */
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
    
    /**
     * Validates if the provided token is a valid refresh token.
     * @param token JWT token to validate
     * @return true if the token is a valid refresh token, false otherwise
     */
    /**
     * Validates if the provided token is a valid refresh token.
     * @param token JWT token to validate
     * @return true if the token is a valid refresh token, false otherwise
     */
    public boolean validateRefreshToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            System.err.println("Refresh token is null or empty");
            return false;
        }
        
        try {
            Claims claims = extractAllClaims(token);
            
            // Check if token has the refresh type
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
                System.err.println("Invalid token type: " + tokenType);
                return false;
            }
            
            // Check if token is not expired
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
    
    /**
     * Generates a new refresh token for the given user details.
     * @param userDetails user details
     * @return JWT refresh token as string
     */
    /**
     * Generates a new refresh token for the given user details.
     * @param userDetails user details
     * @return JWT refresh token as string
     */
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