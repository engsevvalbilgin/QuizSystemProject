package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;
}
