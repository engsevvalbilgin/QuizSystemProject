package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.DiscriminatorValue; 
@Entity

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("Student")
public class Student extends User {

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "student_id", unique = true)
    private int studentId;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TakeQuiz> takenQuizzes = new ArrayList<>();

	@Override
	protected void showUserDetails() {
		
		
	}
	public static Student createStudent() {
		return new Student();
		
	}

    public List<TakeQuiz> getTakenQuizzes() {
        return takenQuizzes;
    }

    public void setTakenQuizzes(List<TakeQuiz> takenQuizzes) {
        this.takenQuizzes = takenQuizzes;
    }

    public void addTakenQuiz(TakeQuiz takeQuiz) {
        this.takenQuizzes.add(takeQuiz);
        takeQuiz.setStudent(this);
    }

    public void removeTakenQuiz(TakeQuiz takeQuiz) {
        this.takenQuizzes.remove(takeQuiz);
        takeQuiz.setStudent(null);
    }
}
