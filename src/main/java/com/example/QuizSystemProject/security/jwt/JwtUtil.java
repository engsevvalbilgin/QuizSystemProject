package com.example.QuizSystemProject.security.jwt; // Paket adınızın doğru olduğundan emin olun


import org.springframework.security.core.GrantedAuthority; // GrantedAuthority için
import java.util.stream.Collectors; // Collectors için

import io.jsonwebtoken.Claims; // JWT Claim'leri için
import io.jsonwebtoken.Jwts; // JWT builder ve parser için
import io.jsonwebtoken.SignatureAlgorithm; // İmza algoritması için
import io.jsonwebtoken.io.Decoders; // Base64 decoding için
import io.jsonwebtoken.security.Keys; // Anahtar oluşturma için

import org.springframework.beans.factory.annotation.Value; // Değer enjekte etmek için
import org.springframework.security.core.userdetails.UserDetails; // Kullanıcı detayları için
import org.springframework.stereotype.Service; // Service bileşeni olarak işaretlemek için

import java.security.Key; // Güvenlik anahtarı için
import java.util.Date; // Tarih/saat için
import java.util.HashMap; // Map için
import java.util.Map; // Map arayüzü için
import java.util.function.Function; // Fonksiyonel arayüz için

@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir
public class JwtUtil {

    // application.properties dosyasından gizli JWT anahtarını enjekte et
    // Bu anahtar token'ları imzalamak ve doğrulamak için kullanılacaktır.
    @Value("${jwt.secret}") // application.properties'de bu isimde bir property tanımlayacağız
    private String SECRET_KEY;

    // Token'dan kullanıcı adını (subject) çekme
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // Claims nesnesinden subject'i çek
    }

    // Token'dan belirli bir claim'i çekme (Generic metod)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // Tüm claim'leri çek
        return claimsResolver.apply(claims); // Belirtilen fonksiyonu claim'lere uygula
    }

    // Token'dan tüm claim'leri çekme
    private Claims extractAllClaims(String token) {
        // Token'ı ayrıştır, imza anahtarını kullanarak doğrula ve claim'leri al
        return Jwts
                .parserBuilder() // Parser oluşturucu
                .setSigningKey(getSigningKey()) // İmza anahtarını set et
                .build() // Parser'ı build et
                .parseClaimsJws(token) // JWS (Signed JWT) olarak ayrıştır
                .getBody(); // Claim body'sini al
    }

    // Token'ı imzalama için kullanılacak güvenlik anahtarını oluşturma
    private Key getSigningKey() {
        // Base64 ile encode edilmiş gizli anahtarı byte dizisine dönüştür
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        // Bu byte dizisinden bir HMAC-SHA anahtarı oluştur
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- Token Oluşturma ---

    // UserDetails objesinden token oluşturma
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Kullanıcı rollerini claim olarak ekleyelim (isteğe bağlı ama faydalı)
        claims.put("roles", userDetails.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority) // GrantedAuthority objelerinden String rollerini al
                                     .collect(Collectors.toList())); // Liste olarak ekle

        // claims.put("userId", ((User) userDetails).getId()); // <-- BU SATIR ARTIK YORUM SATIRI VEYA SİLİNDİ!
                                                            // ClassCastException'a neden olan burasıydı.

        // NOT: Eğer ileride kullanıcı ID'sini JWT'ye eklemek isterseniz,
        // UserDetailsServiceImpl'in UserDetails yerine kendi User objenizi veya
        // özel bir UserDetails implementasyonu döndürmesi ve JWTUtil'un da buna uygun olması gerekir.
        // Şimdilik basitlik için bu satırı kapalı tutuyoruz.


        // İsteğe bağlı olarak yayıncı (issuer), hedef kitle (audience) gibi alanları claims objesine ekleyebilirsiniz.
        // claims.put("iss", "quizland");
        // claims.put("aud", "users");


        return createToken(claims, userDetails.getUsername());
    }

    // Claim'ler ve kullanıcı adı ile token oluşturma
    private String createToken(Map<String, Object> claims, String subject) {
        // JWT builder'ı kullanarak token oluştur
        return Jwts.builder()
                .setClaims(claims) // Claim'leri set et
                .setSubject(subject) // Konuyu (genellikle kullanıcı adı) set et
                .setIssuedAt(new Date(System.currentTimeMillis())) // Token oluşturulma zamanı
                // Son kullanma zamanını eklemek çok önemlidir! Güvenlik için token'ların bir ömrü olmalı.
                // Örn: 24 saat geçerlilik süresi (1000 ms * 60 s * 60 dk * 24 saat)
                 .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // <-- Geçerlilik süresi 24 saate çıkarıldı
                 // Not: Önceki süre: 10 saat (1000 * 60 * 60 * 10)


                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Anahtar ve algoritma ile imzala
                .compact(); // JWT string'ini oluştur
    }

    // --- Token Doğrulama ---

    // Token'ın süresinin dolup dolmadığını kontrol etme
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration); // Claims nesnesinden son kullanma zamanını çek
    }

    public boolean isTokenExpired(String token) {
        // extractExpiration(token) null dönebilir mi? Jwts parser hata fırlatır süresi geçmişse, bu method ondan sonra çağrılır.
        // Ancak yine de null kontrolü yapmak güvenli olabilir.
        Date expirationDate = extractExpiration(token);
        return expirationDate == null || expirationDate.before(new Date()); // Son kullanma zamanı yoksa veya geçmişse true dön
    }


    // Token'ın geçerli olup olmadığını kontrol etme
    // Kullanıcı adı eşleşiyor mu ve süresi dolmuş mu?
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    // Refresh token doğrulama
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    // Refresh token oluşturma (daha uzun süreli)
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .collect(Collectors.toList()));
        claims.put("type", "refresh");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7 gün geçerli
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}