package com.example.QuizSystemProject.security; 

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException; 
import org.springframework.security.web.access.AccessDeniedHandler; 
import org.springframework.stereotype.Component; 

import java.io.IOException;

@Component 
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        System.err.println("CustomAccessDeniedHandler: Yetkilendirme başarısız. Erişim engellendi! Mesaj: " + accessDeniedException.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
        response.setContentType("application/json"); 
        response.getWriter().write("{\"error\": \"Erişim Reddedildi\", \"message\": \"" + accessDeniedException.getMessage() + "\"}"); 
        response.getWriter().flush();
    }
}