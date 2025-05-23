package com.example.QuizSystemProject.security;

import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of Spring Security's UserDetailsService that loads user-specific data.
 */
@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Find the user by username or email (case-insensitive)
            User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElseThrow(() -> {
                        String errorMsg = "User not found with username/email: " + username;
                        System.err.println(errorMsg);
                        return new UsernameNotFoundException(errorMsg);
                    });

            // Log user loading for debugging
            System.out.println("Loading user: " + user.getUsername() + 
                             ", Type: " + user.getClass().getSimpleName() + 
                             ", Active: " + user.isActive());
            
            // Create and return our custom UserDetails implementation
            return new CustomUserDetails(user, getAuthorities(user));
            
        } catch (Exception e) {
            System.err.println("Error loading user " + username + ": " + e.getMessage());
            if (e instanceof UsernameNotFoundException) {
                throw e;
            }
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }
    
    private List<GrantedAuthority> getAuthorities(User user) {
        String role = determineUserRole(user);
        
        // Log user information for debugging
        System.out.println("UserDetailsServiceImpl: User ID: " + user.getId());
        System.out.println("UserDetailsServiceImpl: User Type: " + user.getClass().getSimpleName());
        System.out.println("UserDetailsServiceImpl: Granted Role: " + role);

        return List.of(new SimpleGrantedAuthority(role));
    }
    
    
    /**
     * Determine the role of the user based on their type
     */
    private String determineUserRole(User user) {
        // Debug: Print the actual class name and role
        String className = user.getClass().getName();
        String role = user.getRole();
        
        System.out.println("UserDetailsServiceImpl: User class: " + className);
        System.out.println("UserDetailsServiceImpl: User role from DB: " + role);
        
        // If the role is already set in the user entity, use it
        if (role != null && !role.isEmpty()) {
            // Ensure the role has the ROLE_ prefix if it's missing
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role.toUpperCase();
            }
            System.out.println("UserDetailsServiceImpl: Using role from user entity: " + role);
            return role;
        }
        
        // Fallback to class-based role determination if role field is not set
        if (user instanceof com.example.QuizSystemProject.Model.Admin) {
            System.out.println("UserDetailsServiceImpl: Determined role from class: ROLE_ADMIN");
            return "ROLE_ADMIN";
        } else if (user instanceof com.example.QuizSystemProject.Model.Teacher) {
            System.out.println("UserDetailsServiceImpl: Determined role from class: ROLE_TEACHER");
            return "ROLE_TEACHER";
        } else if (user instanceof com.example.QuizSystemProject.Model.Student) {
            System.out.println("UserDetailsServiceImpl: Determined role from class: ROLE_STUDENT");
            return "ROLE_STUDENT";
        }
        
        // If we can't determine the role, log a warning and default to USER
        System.err.println("Warning: Could not determine role for user: " + user.getUsername() + ", class: " + className);
        return "ROLE_USER";
    }
}