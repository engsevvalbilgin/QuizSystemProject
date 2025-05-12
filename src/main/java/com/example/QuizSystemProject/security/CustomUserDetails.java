package com.example.QuizSystemProject.security; // Paket adınızı kontrol edin

import com.example.QuizSystemProject.Model.User; // Kendi User Entity'mizi import edin
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Spring Security UserDetails

import java.util.Collection; // Collection için

// Spring Security'nin UserDetails arayüzünü implemente ediyoruz
// Bu sınıf, SecurityContext'te kullanıcı bilgilerini tutacak.
public class CustomUserDetails implements UserDetails {

    private int id; // Kullanıcının veritabanı ID'si
    private String username; // Kullanıcı adı
    private String password; // Şifre (null olabilir eğer parola bilgisine JWT sonrası ihtiyacımız yoksa)
    private boolean enabled; // Hesap etkin mi?
    private boolean accountNonExpired; // Hesap süresi dolmamış mı?
    private boolean credentialsNonExpired; // Şifre süresi dolmamış mı?
    private boolean accountNonLocked; // Hesap kilitli değil mi?
    private Collection<? extends GrantedAuthority> authorities; // Kullanıcının rolleri/yetkileri

    // Kendi User Entity objemizden CustomUserDetails oluşturacak constructor
    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.id = user.getId(); // ID'yi set ediyoruz!
        this.username = user.getUsername();
        this.password = user.getPassword(); // Parola şifrelenmiş olmalı
        this.enabled = user.isEnabled();
        // isAccountNonExpired, isCredentialsNonExpired, isAccountNonLocked için mantık
        // Sizin UserDetailsServiceImpl'deki gibi true veya user.isActive() kullanabilirsiniz.
        // UserDetailsServiceImpl'deki User objesi oluştururken kullandığınız mantığı buraya taşıyın.
        this.accountNonExpired = true; // UserDetailsServiceImpl'deki gibi true
        this.credentialsNonExpired = true; // UserDetailsServiceImpl'deki gibi true
        this.accountNonLocked = user.isActive(); // UserDetailsServiceImpl'deki gibi user.isActive()
        this.authorities = authorities;
    }

    // --- Get Metotları ---

    // Spring Security arayüz metotları
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password; // Eğer password null set edilirse, null döner
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // --- Ekstra Get Metodu (Kullanıcı ID'si için) ---
    public int getId() {
        return id;
    }
}