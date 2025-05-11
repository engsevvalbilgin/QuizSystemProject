package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@DiscriminatorValue("ADMIN") // Inheritance kullanacaksan
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User {

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Announcement> announcements;

    public void addAnnouncement() {
        this.announcements.add(new Announcement());
    }

    public void showUserDetails() {
        System.out.println("Name: " + getName());
        System.out.println("Surname: " + getSurname());
        System.out.println("Age: " + getAge());
        System.out.println("Email: " + getEmail());
        System.out.println("Password: " + getPassword());
        System.out.println("Username: " + getUsername());
    }

    // Diğer işlevlerin içi doldurulabilir
}
