package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.User; // Import the User Entity
import org.springframework.data.jpa.repository.JpaRepository; // Import JpaRepository
import org.springframework.stereotype.Repository; // Import the Repository annotation

import java.util.List; // List import
import java.util.Optional; // Optional import

@Repository // Indicates that this interface is a Spring Data JPA Repository
// Extends JpaRepository to inherit basic CRUD methods
// <User, Integer>: The first parameter is the Entity type (User), the second is the type of its Primary Key (Integer).
public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring Data JPA automatically provides methods like save(), findById(), findAll(), delete(), etc.

    // Needs based on analysis of user-related templates (Authentication, User Management, Roles):
    // Find a User by their username (Username is unique)
    // Used during login and registration check
	
    Optional<User> findByUsername(String username); // Use Optional<User> as user might not exist

    // Find a User by their email (Email is unique)
    // Used during registration check and potentially password reset
    Optional<User> findByEmail(String email); // Use Optional<User> as user might not exist

    // Hem kullanıcı adı hem de email ile arama yapan özel metot (login için kullanılabilir)
    Optional<User> findByUsernameOrEmail(String username, String email);
    // Find all Users by their role (e.g., "ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN")
    // Used for listing users by role (e.g., show all teachers)
    List<User> findAllByRole(String role);

    // Find all active users (isActive = true)
    List<User> findAllByIsActiveTrue();

    // Find all inactive users (isActive = false)
    List<User> findAllByIsActiveFalse();

    // You can combine conditions as well, e.g.:
    List<User> findAllByRoleAndIsActiveTrue(String role); // Find all active users with a specific role
    // --- Yeni Metot: Confirmation Token ile Kullanıcı Bulma ---
    Optional<User> findByConfirmationToken(String confirmationToken);
    
    // --- Yeni Metot: Reset Password Token ile Kullanıcı Bulma ---
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    
    // Kullanıcı adına veya email'e göre arama yaparken büyük/küçük harf duyarlılığını kapatmak için
    // Aşağıdaki gibi JPQL sorguları yazılabilir:
    // @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    // Optional<User> findByUsernameIgnoreCase(@Param("username") String username);
    // @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    // Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    // @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) OR LOWER(u.email) = LOWER(:email)")
    // Optional<User> findByUsernameOrEmailIgnoreCase(@Param("username") String username, @Param("email") String email);
    
    Optional<User> findById(int Id);
}

