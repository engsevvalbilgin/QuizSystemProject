package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.*;
// Option Entity'sini import edin (seçilen şıklar için)

import java.util.List; // List importu

import java.util.stream.Collectors; // Akış işlemleri için

// Bu DTO, API yanıtlarında bir öğrencinin belirli bir soruya verdiği cevabın detaylarını taşır.
// Doğru cevap bilgisi (isCorrect) dahil edilir.
public class AnswerAttemptResponse {

    private int id;
    private int questionId; // Hangi soruya cevap verildiği
    private int questionNumber; // Sorunun quiz içindeki numarası
    private String questionSentence; // Sorunun metni

    private String submittedAnswerText; // Metin tabanlı cevap (Açık uçlu vb.)
    private List<String> selectedOptionTexts; // Çoktan seçmeli sorularda seçilen şıkların metinleri

    private boolean isCorrect; // Bu cevabın doğru olup olmadığı


    // JPA için argümansız constructor
    public AnswerAttemptResponse() {
    }

    // AnswerAttempt Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public AnswerAttemptResponse(AnswerAttempt attempt) {
        this.id = attempt.getId();

        // İlişkili Question'dan bilgileri alalım (null kontrolü önemli)
        if (attempt.getQuestion() != null) {
            this.questionId = attempt.getQuestion().getId();
            this.questionNumber = attempt.getQuestion().getNumber();
            this.questionSentence = attempt.getQuestion().getQuestionSentence();
        } else {
             // İlişkili soru yoksa boş veya default değerler
             this.questionId = -1;
             this.questionNumber = 0;
             this.questionSentence = "Bilinmeyen Soru";
        }


        this.submittedAnswerText = attempt.getSubmittedAnswerText(); // Metin cevabı

        // Eğer seçilen şıklar varsa, metinlerini toplayalım
        if (attempt.getSelectedOptions() != null && !attempt.getSelectedOptions().isEmpty()) {
            this.selectedOptionTexts = attempt.getSelectedOptions().stream()
                                           .map(Option::getText) // Option Entity'sinin metin alanını al
                                           .collect(Collectors.toList());
        } else {
            this.selectedOptionTexts = List.of(); // Seçili şık yoksa boş liste
        }

        this.isCorrect = attempt.isCorrect(); // Cevabın doğruluğu bilgisini dahil et
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }

    public String getQuestionSentence() { return questionSentence; }
    public void setQuestionSentence(String questionSentence) { this.questionSentence = questionSentence; }

    public String getSubmittedAnswerText() { return submittedAnswerText; }
    public void setSubmittedAnswerText(String submittedAnswerText) { this.submittedAnswerText = submittedAnswerText; }

    public List<String> getSelectedOptionTexts() { return selectedOptionTexts; }
    public void setSelectedOptionTexts(List<String> selectedOptionTexts) { this.selectedOptionTexts = selectedOptionTexts; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}