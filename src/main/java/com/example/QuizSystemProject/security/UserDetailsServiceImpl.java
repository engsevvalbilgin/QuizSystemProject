package com.quizland.QuizSystemProject.security; // Paket adınızın doğru olduğundan emin olun

import com.quizland.QuizSystemProject.model.User; // Kendi User Entity'mizi import edin
import com.quizland.QuizSystemProject.repository.UserRepository; // UserRepository'yi import edin
import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.security.core.GrantedAuthority; // Rolleri temsil etmek için
import org.springframework.security.core.authority.SimpleGrantedAuthority; // GrantedAuthority implementasyonu için
import org.springframework.security.core.userdetails.UserDetails; // Spring Security'nin kullanıcı detayları arayüzü
import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService arayüzü
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Kullanıcı bulunamadığında fırlatılacak hata
import org.springframework.stereotype.Service; // Service bileşeni olarak işaretlemek için
import com.quizland.QuizSystemProject.security.CustomUserDetails;
import java.util.Collection; // Koleksiyonlar için
import java.util.List; // Liste için
import java.util.stream.Collectors; // Akış işlemleri için

@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir (otomatik tanıma için)
public class UserDetailsServiceImpl implements UserDetailsService { // UserDetailsService arayüzünü implemente ediyoruz

    private final UserRepository userRepository; // Kullanıcı verilerini çekmek için Repository bağımlılığı

    // UserRepository bağımlılığının enjekte edildiği constructor
    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- UserDetailsService Arayüz Metodunun Implementasyonu ---

    // Spring Security tarafından kimlik doğrulama sırasında çağrılır.
    // Parametre olarak kullanıcının girdiği kullanıcı adını (veya e-postayı) alır.
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("UserDetailsServiceImpl: Kullanıcı yükleniyor - Username/Email: " + usernameOrEmail);

        User user = userRepository.findByUsername(usernameOrEmail)
                    .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> {
                        System.out.println("UserDetailsServiceImpl: Kullanıcı bulunamadı - Username/Email: " + usernameOrEmail);
                        return new UsernameNotFoundException("Kullanıcı bulunamadı: " + usernameOrEmail);
                    }));

        System.out.println("UserDetailsServiceImpl: Kullanıcı bulundu - Kullanıcı Adı: " + user.getUsername());

        String role = user.getRole();
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        /*
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled(),      // Hesabın etkin olup olmadığı (e-posta doğrulaması vb.)
            true,                  // accountNonExpired (Hesap süresi dolmamış kabul edelim)
            true,                  // credentialsNonExpired (Parola süresi dolmamış kabul edelim)
            user.isActive(),       // accountNonLocked (Hesap kilitli mi? user.isActive() buna map ediliyor)
            authorities
        );
        */
        return new CustomUserDetails(user, authorities);

    }

    // NOT: userRepository'ye findByUsername ve findByEmail metodlarının eklenmesi gerekebilir.
    // Veya AuthenticationService'de yaptığımız gibi, findByUsernameOrEmail metodunu UserRepository'de tanımlayabiliriz.
    // Eğer findByUsername ve findByEmail metodları zaten UserRepository'de varsa, bu kod çalışacaktır.
}