package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.Question;
import java.util.List;
import java.util.stream.Collectors;

// Bu DTO, API yanıtlarında temel soru bilgilerini taşır (öğretmenler için)
// TeacherOptionResponse kullanarak doğru cevap bilgisini de içerir
public class TeacherQuestionResponse {

    private int id;
    private int number; // Quiz içindeki soru numarası
    private String questionSentence; // Soru metni
    private String questionType; // Soru tipi adı (örn: "Çoktan Seçmeli", "Açık Uçlu")
    private int points; // Soru puanı

    private List<TeacherOptionResponse> options; // Çoktan seçmeli sorular için şıklar (doğru cevap bilgisiyle)

    // JPA için argümansız constructor
    public TeacherQuestionResponse() {
    }

    // Question Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public TeacherQuestionResponse(Question question) {
        this.id = question.getId();
        this.number = question.getNumber();
        this.questionSentence = question.getQuestionSentence();
        // QuestionType ilişkisinden sadece tip adını alalım
        this.questionType = question.getType() != null ? question.getType().getTypeName() : null;
        // Soru puanını alalım
        this.points = question.getPoints();

        // Güvenli bir şekilde options listesini kontrol edip dönüştürelim
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
             this.options = question.getOptions().stream()
                                   .map(TeacherOptionResponse::new) // Her Option Entity'sini TeacherOptionResponse DTO'suna dönüştür
                                   .collect(Collectors.toList());
        } else {
             this.options = List.of(); // Şık yoksa boş liste döndür
        }
    }

    // Getter ve Setterlar
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getQuestionSentence() { return questionSentence; }
    public void setQuestionSentence(String questionSentence) { this.questionSentence = questionSentence; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public List<TeacherOptionResponse> getOptions() { return options; }
    public void setOptions(List<TeacherOptionResponse> options) { this.options = options; }
}
