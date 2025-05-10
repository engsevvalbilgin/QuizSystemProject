package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;  // Buradaki Id doğru olacak şekilde değiştirdim

/**
 *
 * @author hp
 */
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
    // Diğer alanlar ve getter/setter metodları buraya eklenebilir
}
