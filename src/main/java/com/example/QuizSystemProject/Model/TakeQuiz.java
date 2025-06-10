package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "take_quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TakeQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    public int calculateDuration() {
        if (startTime == null || endTime == null) return 0;
        long diffMillis = endTime.getTime() - startTime.getTime();
        return (int) (diffMillis / (1000 * 60));    
    }
}
