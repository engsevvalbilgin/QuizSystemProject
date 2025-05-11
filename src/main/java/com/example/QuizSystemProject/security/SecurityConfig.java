package com.example.QuizSystemProject.security; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.security.jwt.JwtAuthenticationEntryPoint; // Yeni AuthenticationEntryPoint'i import edin
import com.example.QuizSystemProject.security.jwt.JwtAuthenticationFilter; // JwtAuthenticationFilter'ı import edin

import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // JWT filtresini eklemek için

// AntPathRequestMatcher importu hala gerekli değil


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // --- Bean Tanımları ---

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // JWT filtre bağımlılığını enjekte et
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // JWT Authentication Entry Point bağımlılığını enjekte et
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    // Constructor (JWT filtre ve Entry Point bağımlılıkları ile)
    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF Korumasını Devre Dışı Bırak
            .csrf(csrf -> csrf.disable())

            // Exception Handling (Kimlik doğrulama hataları için Entry Point'i set et)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

            // HTTP İstek Yetkilendirme Kuralları
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // --- Herkese Açık Endpoint'ler ---
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll() // DİKKAT: Üretimde kapatın veya yetkilendirin!
                    // Swagger UI endpoint'lerine erişim izni (API dokümantasyonu için, ileride eklenebilir)
                    // .requestMatchers("/v3/api-docs/**").permitAll()
                    // .requestMatchers("/swagger-ui/**").permitAll()
                    // .requestMatchers("/swagger-ui.html").permitAll()


                    // --- Rol Bazlı Kısıtlamalar (Daha Spesifikten Genele Doğru Sırala) ---
                    .requestMatchers(HttpMethod.GET, "/api/statistics/overall").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/users/{id}/role").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/users/{id}/review-teacher-request").hasRole("ADMIN")

                    .requestMatchers(HttpMethod.POST, "/api/quizzes").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/quizzes/{id}").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/quizzes/{id}").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/quizzes/{quizId}/questions/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/quizzes/{quizId}/questions/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/quizzes/{quizId}/questions/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/quizzes/{quizId}/questions/{questionId}/options/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/quizzes/{quizId}/questions/{questionId}/options/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/quizzes/{quizId}/questions/{questionId}/options/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/statistics/quizzes/**").hasAnyRole("TEACHER", "ADMIN")

                    .requestMatchers(HttpMethod.POST, "/api/sessions/start/**").hasRole("STUDENT")
                    .requestMatchers(HttpMethod.POST, "/api/sessions/{sessionId}/answer").hasRole("STUDENT")
                    .requestMatchers(HttpMethod.POST, "/api/sessions/{sessionId}/complete").hasRole("STUDENT")

                    .requestMatchers(HttpMethod.GET, "/api/statistics/students/{studentId}/overall").hasAnyRole("TEACHER", "ADMIN") // Şimdilik Teacher/Admin
                     .requestMatchers(HttpMethod.GET, "/api/statistics/students/{studentId}/quizzes/**").hasAnyRole("TEACHER", "ADMIN") // Şimdilik Teacher/Admin
                     .requestMatchers(HttpMethod.GET, "/api/statistics/students/{studentId}/average-score").hasAnyRole("TEACHER", "ADMIN") // Şimdilik Teacher/Admin
                     .requestMatchers(HttpMethod.GET, "/api/sessions/student/**").hasAnyRole("TEACHER", "ADMIN") // Şimdilik Teacher/Admin
                     .requestMatchers(HttpMethod.GET, "/api/sessions/{sessionId}").authenticated() // Kimlik doğrulanmış herkes (şimdilik)


                    // Quiz Listeleme ve Detayları (Herkese açık)
                    .requestMatchers(HttpMethod.GET, "/api/quizzes").permitAll()
                     .requestMatchers(HttpMethod.GET, "/api/quizzes/{id}").permitAll()
                     .requestMatchers(HttpMethod.GET, "/api/quizzes/{quizId}/questions").permitAll()


                    // --- Varsayılan Kural ---
                    // Yukarıdaki daha spesifik kurallarla eşleşmeyen DİĞER TÜM İSTEKLER (API dışı dahil) kimlik doğrulaması gerektirir.
                    .anyRequest().authenticated() // En genel kural, kimlik doğrulaması gerektirir.

            )

            // Oturum Yönetimi Yapılandırması (Stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // JWT Kimlik Doğrulama Filtresini Ekle
            // Bu filtre, Spring Security'nin UsernamePasswordAuthenticationFilter'ından ÖNCE çalışmalıdır.
            // Gelen istekteki token'ı yakalar ve doğrular.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


            // H2 Konsolu için frame options ayarı (gerekli olabilir)
             // .headers(headers -> headers.frameOptions().sameOrigin()) // Eğer hala hata alıyorsanız yorum satırından çıkarın

            // HttpBasic veya FormLogin devre dışı bırakılabilir eğer sadece token tabanlı auth kullanılacaksa
            // .httpBasic(httpBasic -> httpBasic.disable())
            // .formLogin(formLogin -> formLogin.disable());


        // Yapılandırılan HTTP güvenliğini build et ve döndür
        return http.build();
    }

    // AuthenticationProvider Bean'i, PasswordEncoder Bean'i, JwtAuthenticationFilter Bean'i, JwtAuthenticationEntryPoint Bean'i
    // @Configuration sınıfı içinde tanımlandığı için Spring tarafından otomatik yönetilir.


    // ... Diğer Bean'ler ve Ayarlar ...
}