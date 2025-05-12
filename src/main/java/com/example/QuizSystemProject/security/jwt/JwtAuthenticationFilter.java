package com.example.QuizSystemProject.security.jwt; // Paket adınızın doğru olduğundan emin olun

import jakarta.servlet.FilterChain; // Servlet filtre zinciri için
import jakarta.servlet.ServletException; // Servlet hataları için
import jakarta.servlet.http.HttpServletRequest; // HTTP istek objesi için
import jakarta.servlet.http.HttpServletResponse; // HTTP yanıt objesi için

import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Kimlik doğrulama token objesi için
import org.springframework.security.core.context.SecurityContextHolder; // Güvenlik bağlamını yönetmek için
import org.springframework.security.core.userdetails.UserDetails; // Kullanıcı detayları için
import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService arayüzü (bizim implementasyonumuz)
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // Kimlik doğrulama detaylarını ayarlamak için
import org.springframework.stereotype.Component; // Spring bileşeni olarak işaretlemek için
import org.springframework.web.filter.OncePerRequestFilter; // Her istekte sadece bir kez çalışmasını sağlamak için

import java.io.IOException; // G/Ç hataları için

@Component // Spring'e bu sınıfın bir bileşen olduğunu belirtir (otomatik tanıma ve Bean yönetimi için)
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Her istekte sadece bir kez çalışacak bir filtre extend ediyoruz

    private final JwtUtil jwtUtil; // JWT işlemleri için JwtUtil bağımlılığı
    private final UserDetailsService userDetailsService; // Kullanıcı detaylarını çekmek için UserDetailsService bağımlılığı

    // JwtUtil ve UserDetailsService bağımlılıklarının enjekte edildiği constructor
    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // --- Filtre Metodunun Implementasyonu ---

    // Her gelen HTTP isteği için bu metod çalışır (OncePerRequestFilter sayesinde her istekte sadece bir kez).
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, // Gelen HTTP isteği
            HttpServletResponse response, // Giden HTTP yanıtı
            FilterChain filterChain // Filtre zinciri (isteği bir sonraki filtreye veya hedefe iletmek için)
    ) throws ServletException, IOException {

        System.out.println("JwtAuthenticationFilter: doFilterInternal çalıştı. İstek URL: " + request.getRequestURI());

        // 1. Authorization başlığını al
        // HTTP başlığında genellikle "Authorization: Bearer <token>" formatında gelir.
        final String authHeader = request.getHeader("Authorization");
        final String jwt; // JWT token string'i
        final String username; // Token'dan çıkarılan kullanıcı adı

        // 2. Authorization başlığını kontrol et
        // Başlık yoksa veya "Bearer " ile başlamıyorsa, bu filtre ile ilgili bir JWT token yok demektir.
        // İsteği bir sonraki filtreye ilet.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JwtAuthenticationFilter: Authorization başlığı yok veya 'Bearer ' ile başlamıyor. Bir sonraki filtreye geçiliyor.");
            filterChain.doFilter(request, response);
            return; // Metodu sonlandır
        }

        // 3. Token'ı ayıkla
        // "Bearer " kısmını atlayarak token string'ini al.
        jwt = authHeader.substring(7);
        System.out.println("JwtAuthenticationFilter: JWT token bulundu: " + jwt);

        // 4. Token'dan kullanıcı adını (subject) çek
        try {
            username = jwtUtil.extractUsername(jwt);
            System.out.println("JwtAuthenticationFilter: Token'dan kullanıcı adı çıkarıldı: " + username);
        } catch (Exception e) {
            // Token'dan kullanıcı adı çekilirken bir hata olursa (token formatı bozuksa, imzası yanlışsa vb.)
            // Bu geçerli olmayan bir tokendır. Hata mesajını logla ve isteği bir sonraki filtreye ilet.
            System.err.println("JwtAuthenticationFilter: Token'dan kullanıcı adı çekilirken hata oluştu: " + e.getMessage());
             // İsteğe bağlı: Geçersiz token'a özel HTTP yanıtı (örn: 401 Unauthorized) dönebilirsiniz.
             // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             // return;
            filterChain.doFilter(request, response); // Hatalı token'ı Security'nin diğer filtreleri ele alabilir
            return; // Metodu sonlandır
        }


        // 5. Kullanıcı adı bulunduysa VE SecurityContextHolder'da zaten kimlik doğrulaması yapılmamışsa
        // SecurityContextHolder.getAuthentication() null ise, bu istek için kullanıcı kimliği henüz doğrulanmamış demektir.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("JwtAuthenticationFilter: Kullanıcı adı mevcut ve güvenlik bağlamı boş. Kullanıcı detayları yükleniyor.");

            // 6. UserDetailsService kullanarak kullanıcı detaylarını yükle
            // Veritabanından kullanıcıyı UserDetails objesi olarak çekeriz.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            System.out.println("JwtAuthenticationFilter: UserDetails yüklendi: " + userDetails.getUsername());

            // 7. Token'ı doğrula
            // Çektiğimiz kullanıcı detayları ile token'ın geçerliliğini (süresi dolmuş mu, kullanıcı adı eşleşiyor mu, imza doğru mu - JwtUtil içinde yapılır) kontrol et.
            if (jwtUtil.validateToken(jwt, userDetails)) {
                System.out.println("JwtAuthenticationFilter: Token geçerli. Güvenlik bağlamı ayarlanıyor.");

                // 8. Token geçerliyse, Spring Security için kimlik doğrulama objesi oluştur
                // UsernamePasswordAuthenticationToken, kimlik doğrulaması yapılmış bir kullanıcıyı temsil etmek için kullanılır.
                // Üç argümanlı constructor (userDetails, credentials=null, authorities), kullanıcının zaten doğrulanmış olduğunu belirtir.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, // Kimliği doğrulanan kullanıcı detayları
                        null, // Kimlik bilgileri (parola gibi). Token tabanlı auth'ta null olur.
                        userDetails.getAuthorities() // Kullanıcının rolleri/yetkileri
                );

                // İsteğin detaylarını (IP adresi vb.) kimlik doğrulama objesine ekle (isteğe bağlı ama iyi practice)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Güvenlik bağlamını (Security Context) güncelle
                // Bu satır, mevcut isteğin Spring Security tarafından kimliği doğrulanmış olarak kabul edilmesini sağlar.
                // Artık @PreAuthorize gibi yetkilendirme anotasyonları veya security context'e erişimler çalışacaktır.
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("JwtAuthenticationFilter: Güvenlik bağlamı güncellendi.");

            } else {
                System.out.println("JwtAuthenticationFilter: Token geçerli değil (kullanıcı adı eşleşmedi veya süresi doldu).");
            }
        }

        // 10. İsteği filtre zincirindeki bir sonraki filtreye veya hedef kaynağa ilet
        filterChain.doFilter(request, response);
    }
}