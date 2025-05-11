package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.NotNull; // Boş olamaz kontrolü için
import jakarta.validation.constraints.Size; // Uzunluk kontrolü için

import java.util.Set; // Set importu (seçilen şıkların ID'leri için)

// Bu bir DTO (Data Transfer Object) sınıfıdır. Entity değildir.
// Öğrencinin bir soruya verdiği cevabı taşır.
public class AnswerSubmissionRequest {

    @NotNull(message = "Soru ID'si boş olamaz") // Hangi soruya cevap verildiği bilgisi zorunlu
    private Long questionId;

    @Size(max = 5000, message = "Cevap metni çok uzun") // Metin tabanlı cevaplar için, boş olabilir
    private String submittedAnswerText; // Açık uçlu veya kısa cevaplı sorular için metin cevabı

    // Çoktan seçmeli sorularda öğrencinin seçtiği şıkların ID'leri
    // Boş olabilir (eğer metin tabanlı bir soruya cevap veriliyorsa)
    // Set kullanmak daha mantıklı (sıra önemli değil, tekrar eden ID olmaz)
    private Set<Long> selectedOptionIds;


    // DTO'lar için genellikle argümansız constructor ve getter/setter metotları yeterlidir.

    public AnswerSubmissionRequest() {
    }

    // Alanları alan constructor (isteğe bağlı)
    public AnswerSubmissionRequest(Long questionId, String submittedAnswerText, Set<Long> selectedOptionIds) {
        this.questionId = questionId;
        this.submittedAnswerText = submittedAnswerText;
        this.selectedOptionIds = selectedOptionIds;
    }


    // Getter ve Setter Metotları
    // IDE ile otomatik oluşturabilirsiniz.

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getSubmittedAnswerText() {
        return submittedAnswerText;
    }

    public void setSubmittedAnswerText(String submittedAnswerText) {
        this.submittedAnswerText = submittedAnswerText;
    }

    public Set<Long> getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(Set<Long> selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }

    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}