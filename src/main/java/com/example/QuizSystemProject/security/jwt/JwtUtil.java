package com.example.QuizSystemProject.security.jwt; // Paket adınızın doğru olduğundan emin olun
import com.example.QuizSystemProject.Model.User; // Kendi User Entity'miz için
import org.springframework.security.core.GrantedAuthority; // GrantedAuthority için
import java.util.stream.Collectors; // Collectors için
import java.util.Collection; // Koleksiyonlar için (GrantedAuthority için gerekli olabilir)
import java.util.List; // List için
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
        // Varsayılan claim'ler (subject = kullanıcı adı gibi) dışında ek claim'ler ekleyebilirsiniz
        // Örneğin, kullanıcı ID'si, rolleri vb. claim olarak eklenebilir.
        Map<String, Object> claims = new HashMap<>();
        // Kullanıcı rollerini claim olarak ekleyelim (isteğe bağlı ama faydalı)
        claims.put("roles", userDetails.getAuthorities().stream()
                                       .map(GrantedAuthority::getAuthority) // GrantedAuthority objelerinden String rollerini al
                                       .collect(Collectors.toList())); // Liste olarak ekle
        claims.put("userId", ((User) userDetails).getId()); // Kendi UserDetails objemizde ID varsa ekleyebiliriz (UserDetailsServiceImpl'de eklememiştik, ama User entity'den alınabilir)
        // UserDetailsServiceImpl'deki dönüşümde User entity'den UserDetails oluştururken ID'yi eklemedik.
        // Eğer ID'yi claim'e eklemek isterseniz UserDetailsServiceImpl'i güncelleyip UserDetails objesine ID eklemeli
        // veya burada userDetails instanceof User kontrolü yapıp user.getId() çekmelisiniz.
        // Basitlik için şimdilik username ve rolleri ekleyelim.

        return createToken(claims, userDetails.getUsername());
    }

    // Claim'ler ve kullanıcı adı ile token oluşturma
    private String createToken(Map<String, Object> claims, String subject) {
        // JWT builder'ı kullanarak token oluştur
        return Jwts.builder()
                .setClaims(claims) // Claim'leri set et
                .setSubject(subject) // Konuyu (genellikle kullanıcı adı) set et
                // İsteğe bağlı olarak yayıncı (issuer), hedef kitle (audience) gibi alanlar set edilebilir.
                // setIssuedAt(new Date(System.currentTimeMillis())) // Oluşturulma zamanı
                // setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Son kullanma zamanı (örn: 10 saat)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Anahtar ve algoritma ile imzala
                .compact(); // JWT string'ini oluştur
    }

    // --- Token Doğrulama ---

    // Token'ın süresinin dolup dolmadığını kontrol etme
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration); // Claims nesnesinden son kullanma zamanını çek
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // Son kullanma zamanı şu anki zamandan önceyse süresi dolmuş demektir
    }

    // Token'ın geçerli olup olmadığını kontrol etme
    // Kullanıcı adı eşleşiyor mu ve süresi dolmuş mu?
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // Token'dan kullanıcı adını çek
        // Token'daki kullanıcı adı, UserDetails'deki kullanıcı adı ile aynı mı VE token'ın süresi dolmamış mı?
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}