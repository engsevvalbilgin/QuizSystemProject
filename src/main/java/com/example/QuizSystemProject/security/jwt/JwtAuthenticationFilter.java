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
        String method = request.getMethod();
        
        logger.debug("Processing request: " + method + " " + requestURI);
        
        // Skip authentication for OPTIONS requests (preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("Skipping authentication for OPTIONS request");
            filterChain.doFilter(request, response);
            return;
        }
        
        // Public endpoint'leri kontrol et
        if (isPublicEndpoint(method, requestURI)) {
            logger.debug("Public endpoint accessed: " + requestURI);
            // Özellikle refresh-token endpoint'i için ek başlık ayarları
            if (requestURI.equals("/api/auth/refresh-token") && method.equals("POST")) {
                logger.debug("Refresh token endpoint accessed");
                response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, X-XSRF-TOKEN");
                response.setHeader("Access-Control-Expose-Headers", "Authorization, X-XSRF-TOKEN");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            }
            filterChain.doFilter(request, response);
            return;
        }
        
        // Special handling for logout endpoint
        if (requestURI.equals("/api/auth/logout") && method.equals("POST")) {
            logger.debug("Logout endpoint accessed");
            // Clear the authentication
            SecurityContextHolder.clearContext();
            // Clear any existing tokens
            response.setHeader("Authorization", "");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: " + (authHeader != null ? "present" : "missing"));
        
        // 2. Authorization başlığını kontrol et
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for request: " + method + " " + requestURI);
            // For API endpoints, return 401 Unauthorized
            if (requestURI.startsWith("/api/")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Yetkilendirme başlığı eksik veya geçersiz");
                return;
            }
            // For non-API endpoints, continue the filter chain
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Token'ı ayıkla ve doğrula
        try {
            String jwt = authHeader.substring(7);
            logger.debug("JWT token extracted");
            
            String username = jwtUtil.extractUsername(jwt);
            logger.debug("Extracted username from token: " + username);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Loading user details for: " + username);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    logger.debug("Token is valid for user: " + username);
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
                logger.error("User not found: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Kullanıcı bulunamadı");
            } else if (e instanceof io.jsonwebtoken.ExpiredJwtException) {
                logger.warn("JWT token expired: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token süresi dolmuş");
            } else if (e instanceof io.jsonwebtoken.MalformedJwtException) {
                logger.warn("Malformed JWT token: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Geçersiz token formatı");
            } else if (e instanceof io.jsonwebtoken.security.SignatureException) {
                logger.warn("Invalid JWT signature: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Geçersiz token imzası");
            } else {
                logger.error("Unexpected authentication error: " + e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Kimlik doğrulama başarısız: " + e.getMessage());
            }
            
            // Clear the security context to ensure no authentication remains
            SecurityContextHolder.clearContext();
        }
    }
    
    private boolean isPublicEndpoint(String method, String requestURI) {
        // Public endpoint'lerin listesi
        return requestURI.equals("/api/auth/register") ||
               requestURI.equals("/api/auth/login") ||
               requestURI.equals("/api/auth/forgot-password") ||
               requestURI.startsWith("/api/auth/reset-password") || // Bu endpoint path variable alabilir
               requestURI.startsWith("/api/auth/verify-email") ||   // Bu da query param alabilir
               requestURI.equals("/api/auth/refresh-token") ||
               requestURI.equals("/api/teachers/register") ||
               requestURI.startsWith("/api/users/password-reset/") ||
               requestURI.startsWith("/h2-console/") ||
               method.equalsIgnoreCase("OPTIONS");
    }
}
