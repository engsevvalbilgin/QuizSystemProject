package com.example.QuizSystemProject.security;

import com.example.QuizSystemProject.security.jwt.JwtAuthenticationEntryPoint;
import com.example.QuizSystemProject.security.jwt.JwtAuthenticationFilter;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

// CORS Yapılandırma Bean'i (Frontend'den gelecek çapraz kaynak isteklere izin verir)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // Çerezlerin ve kimlik doğrulama başlıklarının (Authorization gibi) gönderilmesine izin ver

        config.addAllowedOrigin("http://localhost:5173"); // Frontend adresiniz (Curl testinde kullandığımız port)

        config.addAllowedHeader("*"); // Tüm başlıklara izin ver (Authorization, Content-Type vb.)
        config.addAllowedMethod("*"); // Tüm HTTP metotlarına izin ver (GET, POST, PUT, DELETE, OPTIONS vb.)
        source.registerCorsConfiguration("/**", config); // Tüm (/**) URL desenlerine bu CORS konfigürasyonunu uygula
        return source;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .disable() // Disable CSRF for API endpoints
                // .ignoringRequestMatchers("/api/**") // Uncomment if you want to enable CSRF for non-API endpoints
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints
                .requestMatchers(
                    "/api/auth/**",
                    "/api/teachers/register",
                    "/api/teachers/login",
                    "/api/users/password-reset/**",
                    "/h2-console/**"
                ).permitAll()
                
                // Authenticated users - Spesifik endpoints önce gelmeli
                .requestMatchers(
                    "/api/users/profile",
                    "/api/users/change-password",
                    "/api/users/change-email"
                ).authenticated()
                
                // Admin only - Genel users/** pattern'i en sonda olmalı
                .requestMatchers(
                    "/api/statistics/overall"
                ).hasAuthority("ROLE_ADMIN")
                
                // Teacher and Admin - Quiz management
                .requestMatchers(HttpMethod.POST, "/api/quizzes").hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/quizzes/**").hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                .requestMatchers("/api/quizzes/{quizId}/questions").hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                .requestMatchers("/api/quizzes/{quizId}/questions/**").hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                
                // Student only
                .requestMatchers(
                    "/api/sessions/start/**",
                    "/api/sessions/{sessionId}/answer",
                    "/api/sessions/{sessionId}/complete"
                ).hasAuthority("ROLE_STUDENT")
                
                // Admin only - Genel users/** pattern'i
                .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")
                
                // Announcements - view allowed for all authenticated users, create/update/delete for admins
                .requestMatchers(HttpMethod.GET, "/api/announcements").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/announcements/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/announcements").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/announcements/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/announcements/**").hasAuthority("ROLE_ADMIN")
                
                // Quiz access for all authenticated users
                .requestMatchers(HttpMethod.GET, "/api/quizzes").authenticated()
                // Quiz detay endpoint'leri için daha geniş izin (/* veya /** pattern kullanımı)
                .requestMatchers(HttpMethod.GET, "/api/quizzes/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/quizzes/*/questions").authenticated()
                
                // Session access for related users
                .requestMatchers(
                    "/api/sessions/student/**",
                    "/api/sessions/{sessionId}"
                ).hasAuthority("ROLE_STUDENT")
                
                // Default - require authentication for all other requests
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class)
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }
}
