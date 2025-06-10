package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.User; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 

import java.util.List; 
import java.util.Optional; 

@Repository 
public interface UserRepository extends JpaRepository<User, Integer> {

    
    
	
    Optional<User> findByUsername(String username); 

    
    Optional<User> findByEmail(String email); 

    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    
    List<User> findAllByRole(String role);

    
    List<User> findAllByIsActiveTrue();

    
    List<User> findAllByIsActiveFalse();

    
    List<User> findAllByRoleAndIsActiveTrue(String role); 
    
    Optional<User> findByConfirmationToken(String confirmationToken);
    
    
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    
    
    
    Optional<User> findById(int Id);
}

