package com.example.QuizSystemProject.Repository;

import com.example.QuizSystemProject.Model.QuestionType; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 

import java.util.Optional; 

@Repository 

public interface QuestionTypeRepository extends JpaRepository<QuestionType, Integer> {

    

    
    Optional<QuestionType> findByTypeName(String typeName);

}

