package com.example.QuizSystemProject.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.QuizSystemProject.Model.Option;
import com.example.QuizSystemProject.Model.Question;

@Repository
public interface OptionRepository extends JpaRepository<Option, Integer> {

   
    List<Option> findByQuestion(Question question);
    
  
    @Query("SELECT o FROM Option o WHERE o.question.id IN :questionIds")
    List<Option> findByQuestionIdIn(@Param("questionIds") List<Integer> questionIds);
}
