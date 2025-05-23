package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;

import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("ADMIN")
@Getter
@Setter
@NoArgsConstructor
public class Admin extends User {

    // Constructor that sets the role to ROLE_ADMIN
    public Admin(String name, String surname, int age, String email, String username, String password) {
        super(name, surname, age, email, username, password, "ROLE_ADMIN");
    }
    
    // No need for announcements list as it's now managed by User

    public void showUserDetails() {
        System.out.println("Name: " + getName());
        System.out.println("Surname: " + getSurname());
        System.out.println("Age: " + getAge());
        System.out.println("Email: " + getEmail());
        System.out.println("Username: " + getUsername());
        System.out.println("Role: " + getRole());
    }
}
