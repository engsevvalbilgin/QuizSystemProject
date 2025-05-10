import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Entity
public class QuestionAnswer {

    @Id
    private Long id;  // Add the @Id annotation to this field or any other field you wish to use as the primary key
    @ManyToOne
    @JoinColumn(name = "quiz_id") // Assuming the foreign key column is 'quiz_id'
    private Quiz quiz;
    // Other fields, constructors, getters, and setters

}
