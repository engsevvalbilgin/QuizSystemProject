package com.example.QuizSystemProject.Repository;


import com.example.QuizSystemProject.Model.Announcement; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 

@Repository 
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    

    
    

}

