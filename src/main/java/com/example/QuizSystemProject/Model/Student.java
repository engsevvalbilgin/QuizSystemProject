package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student extends User {

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "student_id", unique = true)
    private String studentId;

    @JoinColumn(name="quiz_id")
    private List<Quiz> quizzes;

	@Override
	protected void showUserDetails() {
		// TODO Auto-generated method stub
		
	}
	public static Student createStudent() {
		return new Student();
		
	}
	}
