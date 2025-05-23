package com.example.QuizSystemProject.Repository;


import com.example.QuizSystemProject.Model.Option; // Import the Option Entity
import org.springframework.data.jpa.repository.JpaRepository; // Import JpaRepository
import org.springframework.stereotype.Repository; // Import the Repository annotation

@Repository // Indicates that this interface is a Spring Data JPA Repository
// Extends JpaRepository to inherit basic CRUD methods

public interface OptionRepository extends JpaRepository<Option, Integer> {

    // Spring Data JPA automatically provides methods like save(), findById(), findAll(), delete(), etc.

    // You can define custom query methods here if needed later.
    // For example:
    // List<Option> findByQuestion(Question question); // Find options for a specific question
    // List<Option, Integer>: The first parameter is the Entity type (Option), the second is the type of its Primary Key (Integer).


}
