package com.example.QuizSystemProject.security;

import com.example.QuizSystemProject.Model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;


public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(authorities, "Authorities cannot be null");
        
        this.user = user;
        this.authorities = authorities;
        
        System.out.println("CustomUserDetails created for user: " + user.getUsername() + 
                         " with roles: " + authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
    
  
    public User getUser() {
        return user;
    }
    
   
    public int getId() {
        return user.getId();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomUserDetails)) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return user.getId() == that.user.getId();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user.getId());
    }
    
    @Override
    public String toString() {
        return "CustomUserDetails{" +
               "id=" + user.getId() +
               ", username='" + user.getUsername() + '\'' +
               ", roles=" + authorities +
               '}';
    }
}