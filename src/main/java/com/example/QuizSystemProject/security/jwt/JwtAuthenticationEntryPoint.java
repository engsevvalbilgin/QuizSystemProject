package com.example.QuizSystemProject.security.jwt; // Paket adınızın doğru olduğundan emin olun

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException; // Kimlik doğrulama hataları için
import org.springframework.security.web.AuthenticationEntryPoint; // AuthenticationEntryPoint arayüzü
import org.springframework.stereotype.Component; // Spring bileşeni olarak işaretlemek için

import java.io.IOException; // G/Ç hataları için

@Component // Spring'e bu sınıfın bir bileşen olduğunu belirtir
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint { // AuthenticationEntryPoint arayüzünü implemente ediyoruz

    // Kimlik doğrulaması yapılmamış bir kullanıcı güvenli bir kaynağa erişmeye çalıştığında bu metod çağrılır.
    @Override
    public void commence(
            HttpServletRequest request, // Gelen HTTP isteği
            HttpServletResponse response, // Giden HTTP yanıtı
            AuthenticationException authException // Kimlik doğrulama sırasında oluşan hata
    ) throws IOException, ServletException {
        // Genellikle bu durumda 401 Unauthorized (Yetkisiz) yanıtı döndürülür.
        System.out.println("JwtAuthenticationEntryPoint: Kimlik doğrulama başarısız. 401 Unauthorized yanıtı döndürülüyor.");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        // İsteğe bağlı olarak yanıt gövdesine daha detaylı bir hata mesajı veya JSON objesi de yazılabilir.
    }
}