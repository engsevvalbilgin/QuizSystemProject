/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.QuizSystemProject.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;

/**
 *
 * @author hp
 */
@Entity
public class QuestionAnswer {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
}
