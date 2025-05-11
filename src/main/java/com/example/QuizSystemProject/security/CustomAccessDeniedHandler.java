package com.quizland.QuizSystemProject.security; // Paket adınızı kontrol edin

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException; // Yetkilendirme hatası için
import org.springframework.security.web.access.AccessDeniedHandler; // AccessDeniedHandler arayüzü
import org.springframework.stereotype.Component; // Spring bileşeni olarak işaretlemek için

import java.io.IOException;

@Component // Spring'e bu sınıfın bir bileşen olduğunu belirtir
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // Yetkilendirme (Yetki/Rol) başarısız olduğunda Spring Security tarafından çağrılır.
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        System.err.println("CustomAccessDeniedHandler: Yetkilendirme başarısız. Erişim engellendi! Mesaj: " + accessDeniedException.getMessage());

        // 403 Forbidden yanıtı dön
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 durumu
        response.setContentType("application/json"); // JSON formatı
        response.getWriter().write("{\"error\": \"Erişim Reddedildi\", \"message\": \"" + accessDeniedException.getMessage() + "\"}"); // Yanıt gövdesi
        response.getWriter().flush();
    }
}