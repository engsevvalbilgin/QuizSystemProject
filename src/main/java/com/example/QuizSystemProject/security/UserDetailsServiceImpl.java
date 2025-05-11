package com.example.QuizSystemProject.security; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.User; // Kendi User Entity'mizi import edin
import com.example.QuizSystemProject.Repository.UserRepository; // UserRepository'yi import edin
import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.security.core.GrantedAuthority; // Rolleri temsil etmek için
import org.springframework.security.core.authority.SimpleGrantedAuthority; // GrantedAuthority implementasyonu için
import org.springframework.security.core.userdetails.UserDetails; // Spring Security'nin kullanıcı detayları arayüzü
import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService arayüzü
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Kullanıcı bulunamadığında fırlatılacak hata
import org.springframework.stereotype.Service; // Service bileşeni olarak işaretlemek için

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

        // Veritabanında kullanıcıyı kullanıcı adına VEYA e-posta adresine göre bulmaya çalışalım.
        // Sizin AuthenticationService'deki login mantığına benzer bir arama yapıyoruz.
        User user = userRepository.findByUsername(usernameOrEmail) // Önce kullanıcı adına göre ara
                    .orElseGet(() -> userRepository.findByEmail(usernameOrEmail) // Bulamazsan email'e göre ara
                    .orElseThrow(() -> { // Hala bulamazsan hata fırlat
                        System.out.println("UserDetailsServiceImpl: Kullanıcı bulunamadı - Username/Email: " + usernameOrEmail);
                        return new UsernameNotFoundException("Kullanıcı bulunamadı: " + usernameOrEmail);
                    }));

        System.out.println("UserDetailsServiceImpl: Kullanıcı bulundu - Kullanıcı Adı: " + user.getUsername());

        // Kullanıcı bulunduğunda, bu kullanıcının bilgilerini Spring Security'nin beklediği
        // UserDetails formatına dönüştürmemiz gerekiyor.
        // Spring Security'nin kendi 'User' sınıfı UserDetails arayüzünü implemente eder ve bu dönüşüm için kullanışlıdır.

        // Kullanıcının rolünü Spring Security'nin GrantedAuthority formatına dönüştür
        // Her rol için bir SimpleGrantedAuthority objesi oluşturuyoruz.
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));

        // Kendi User Entity'mizdeki 'isActive' ve 'enabled' alanlarını
        // Spring Security'nin UserDetails arayüzündeki karşılıklarına map'liyoruz.
        // isActive -> accountNonLocked (Hesap kilitli değil)
        // enabled -> enabled (Hesap etkin)
        // accountNonExpired -> true (Hesap süresi dolmuş değil - bizde böyle bir alan yok, varsayılan true)
        // credentialsNonExpired -> true (Şifre süresi dolmuş değil - bizde böyle bir alan yok, varsayılan true)

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), // Spring Security için kullanıcı adı
            user.getPassword(), // Spring Security için parola (Entity'deki şifrelenmiş parola hash'i)
            user.isEnabled(), // enabled (E-posta doğrulaması yapılmış mı?)
            user.isActive(), // accountNonExpired (Hesap aktif mi? isActive'i kullanıyoruz)
            user.isActive(), // credentialsNonExpired (Şifre süresi dolmuş mu? isActive'i kullanıyoruz)
            user.isActive(), // accountNonLocked (Hesap kilitli mi? isActive'i kullanıyoruz)
            authorities // Kullanıcının rolleri (GrantedAuthority formatında)
        );
    }

    // NOT: userRepository'ye findByUsername ve findByEmail metodlarının eklenmesi gerekebilir.
    // Veya AuthenticationService'de yaptığımız gibi, findByUsernameOrEmail metodunu UserRepository'de tanımlayabiliriz.
    // Eğer findByUsername ve findByEmail metodları zaten UserRepository'de varsa, bu kod çalışacaktır.
}