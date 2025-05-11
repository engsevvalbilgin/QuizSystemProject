package com.example.QuizSystemProject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import com.example.QuizSystemProject.Model.*;
@SpringBootApplication(scanBasePackages = "com.example.QuizSystemProject")
@EntityScan(basePackages = "com.example.QuizSystemProject.Model")
public class QuizSystemProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuizSystemProjectApplication.class, args);
   Quiz q=new Quiz();
    }
}
