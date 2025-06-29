package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "idx_username", columnList = "username", unique = true),
        @Index(name = "idx_email", columnList = "email", unique = true)
    })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private int id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "surname", nullable = false, length = 50)
    private String surname;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    @Size(max = 100) 
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "enabled", nullable = false, columnDefinition = "boolean default false")
    private boolean enabled = false;
    
    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;
    
    @Column(name = "created_date", nullable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "updated_date", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private LocalDateTime updatedDate = LocalDateTime.now();
    
    @Column(name = "confirmation_token", length = 255)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String confirmationToken;
    
    @Column(name = "confirmation_token_expiry_date")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private LocalDateTime confirmationTokenExpiryDate;
    
    @Column(name = "reset_password_token", length = 100)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String resetPasswordToken;
    
    @Column(name = "reset_password_token_expiry_date")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private LocalDateTime resetPasswordTokenExpiryDate;
    
    @Column(name = "pending_email", length = 100)
    private String pendingEmail;
    
    @Column(name = "refresh_token", length = 500)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String refreshToken;


    

    
    public User(String name, String surname, int age, String email, String username, String password, String role) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.email = email;
        this.username = username;
        this.password = password; 
        this.role = role;
        this.enabled = false; 
        this.isActive = true; 
        
    }


    
    public String getPendingEmail() {
        return pendingEmail;
    }
    
    public void setPendingEmail(String pendingEmail) {
        this.pendingEmail = pendingEmail;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; } 

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; } 
    public void setPassword(String password) { this.password = password; } 

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }

    
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    
    public String getConfirmationToken() { return confirmationToken; }
    public void setConfirmationToken(String confirmationToken) { this.confirmationToken = confirmationToken; }

    public LocalDateTime getConfirmationTokenExpiryDate() { return confirmationTokenExpiryDate; }
    public void setConfirmationTokenExpiryDate(LocalDateTime confirmationTokenExpiryDate) { this.confirmationTokenExpiryDate = confirmationTokenExpiryDate; }

    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public LocalDateTime getResetPasswordTokenExpiryDate() { return resetPasswordTokenExpiryDate; }
    public void setResetPasswordTokenExpiryDate(LocalDateTime resetPasswordTokenExpiryDate) { this.resetPasswordTokenExpiryDate = resetPasswordTokenExpiryDate; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }


    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    
    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", role='" + role + '\'' +
               ", enabled=" + enabled +
               ", isActive=" + isActive +
               ", confirmationToken=" + (confirmationToken != null ? "present" : "null") + 
               ", confirmationTokenExpiryDate=" + confirmationTokenExpiryDate +
               ", resetPasswordToken=" + (resetPasswordToken != null ? "present" : "null") + 
               ", resetPasswordTokenExpiryDate=" + resetPasswordTokenExpiryDate +
               '}';
    }

	public void logIn(User user) {
		
		
	}

	public void signIn(User user) {
		
		
	}

	public void logOut(User user) {
		
		
	}

	protected void showUserDetails() {}

	
}
