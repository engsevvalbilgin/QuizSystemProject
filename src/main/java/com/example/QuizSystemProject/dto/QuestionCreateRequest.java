package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid; // Nested DTO doğrulama için

import java.util.List; // Liste importu

public class QuestionCreateRequest {

    @NotNull(message = "Soru numarası boş olamaz")
    @Min(value = 1, message = "Soru numarası en az 1 olmalı")
    private Integer number;

    @NotBlank(message = "Soru metni boş olamaz")
    @Size(max = 5000, message = "Soru metni çok uzun") // Metin alanları için daha yüksek sınır
    private String questionSentence;

    @Size(max = 1000, message = "Doğru cevap metni çok uzun") // Opsiyonel, açık uçlu için
    private String correctAnswerText;

    @NotNull(message = "Soru tipi ID'si boş olamaz")
    private Long questionTypeId; // İlişkili QuestionType ID'si

    // Çoktan seçmeli sorular için şık listesi
    @Valid // Listedeki her OptionCreateRequest objesini de doğrula
    private List<OptionCreateRequest> options; // İleride tanımlanacak OptionCreateRequest DTO'sunu kullanırız

    // Getter ve Setterlar
    public QuestionCreateRequest() {}

    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }

    public String getQuestionSentence() { return questionSentence; }
    public void setQuestionSentence(String questionSentence) { this.questionSentence = questionSentence; }

    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }

    public Long getQuestionTypeId() { return questionTypeId; }
    public void setQuestionTypeId(Long questionTypeId) { this.questionTypeId = questionTypeId; }

    public List<OptionCreateRequest> getOptions() { return options; }
    public void setOptions(List<OptionCreateRequest> options) { this.options = options; }
}