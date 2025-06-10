package com.example.QuizSystemProject.Model;

import jakarta.persistence.*; 
import java.time.LocalDateTime; 
import java.util.Objects; 

@Entity 
@Table(name = "announcements")
public class Announcement {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id; 

    @Column(nullable = false, length = 100) 
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") 
    private String content;

    @Column(nullable = false) 
    private LocalDateTime date; 

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; 
    
    public Announcement() {
    }

    public Announcement(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.date = LocalDateTime.now(); 
        this.user = user;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; } 

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Announcement that = (Announcement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Announcement{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", date=" + date +
               ", publisher=" + (getUser() != null ? getUser().getUsername() : "null") + 
               '}';
    }
}

