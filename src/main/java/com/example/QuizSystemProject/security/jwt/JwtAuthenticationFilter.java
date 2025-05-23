package com.example.QuizSystemProject.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // --- Filtre Metodunun Implementasyonu ---

    // Her gelen HTTP isteği için bu metod çalışır (OncePerRequestFilter sayesinde her istekte sadece bir kez).
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        // Public endpoint'leri kontrol et
        if (isPublicEndpoint(request.getMethod(), requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        
        // 2. Authorization başlığını kontrol et
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Yetkilendirme başlığı eksik veya geçersiz");
            return;
        }

        // 3. Token'ı ayıkla ve doğrula
        try {
            String jwt = authHeader.substring(7);
            String username = jwtUtil.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Log successful authentication
                    logger.info("Authenticated user: " + username + " with roles: " + userDetails.getAuthorities());
                } else {
                    logger.warn("Invalid JWT token for user: " + username);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Geçersiz token");
                    return;
                }
            } else if (username == null) {
                logger.warn("JWT token does not contain a valid username");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Geçersiz token içeriği");
                return;
            }
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            // Log the error and send appropriate response
            logger.error("Authentication error: " + e.getMessage(), e);
            
            if (e instanceof UsernameNotFoundException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Kullanıcı bulunamadı");
            } else if (e instanceof io.jsonwebtoken.ExpiredJwtException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token süresi dolmuş");
            } else if (e instanceof io.jsonwebtoken.JwtException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Geçersiz token");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Bir hata oluştu: " + e.getMessage());
            }
        }
    }
    
    private boolean isPublicEndpoint(String method, String requestURI) {
        // Sadece gerçekten public olan endpoint'leri tanımla
        return requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/api/teachers/register") ||
               requestURI.startsWith("/api/users/password-reset/") ||
               requestURI.startsWith("/h2-console/") ||
               requestURI.equals("/api/auth/logout") ||
               method.equalsIgnoreCase("OPTIONS");
    }
}