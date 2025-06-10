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

    

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Processing request: " + method + " " + requestURI);
        
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("Skipping authentication for OPTIONS request");
            filterChain.doFilter(request, response);
            return;
        }
        
        if (isPublicEndpoint(method, requestURI)) {
            logger.debug("Public endpoint accessed: " + requestURI);
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
        
        if (requestURI.equals("/api/auth/logout") && method.equals("POST")) {
            logger.debug("Logout endpoint accessed");
            SecurityContextHolder.clearContext();
            response.setHeader("Authorization", "");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: " + (authHeader != null ? "present" : "missing"));
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for request: " + method + " " + requestURI);
            if (requestURI.startsWith("/api/")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Yetkilendirme başlığı eksik veya geçersiz");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }
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
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
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
            
            SecurityContextHolder.clearContext();
        }
    }
    
    private boolean isPublicEndpoint(String method, String requestURI) {
        return requestURI.equals("/api/auth/register") ||
               requestURI.equals("/api/auth/login") ||
               requestURI.equals("/api/auth/forgot-password") ||
               requestURI.startsWith("/api/auth/reset-password") ||
               requestURI.startsWith("/api/auth/verify-email") ||
               requestURI.equals("/api/auth/refresh-token") ||
               requestURI.equals("/api/teachers/register") ||
               requestURI.startsWith("/api/users/password-reset/") ||
               requestURI.startsWith("/h2-console/") ||
               method.equalsIgnoreCase("OPTIONS");
    }
}
