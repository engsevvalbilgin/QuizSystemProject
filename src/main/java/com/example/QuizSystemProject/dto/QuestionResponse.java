package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.Question; // Entity'den dönüşüm için Question Entity'sini import edin
import com.example.QuizSystemProject.Model.TestQuestion;

import java.util.List; // Şık listesi için
import java.util.stream.Collectors; // Akış işlemleri için

// Bu DTO, API yanıtlarında temel soru bilgilerini taşır (örn: quiz detaylarındaki sorular).
// Doğru cevap bilgisi dahil EDİLMEZ. Şıkları (OptionResponse) içerebilir.
public class QuestionResponse {

    private int id;
    private int number; // Quiz içindeki soru numarası
    private String questionSentence; // Soru metni
    private String questionType; // Soru tipi adı (örn: "Çoktan Seçmeli", "Açık Uçlu")

    private List<OptionResponse> options; // Çoktan seçmeli sorular için şıklar (İleride tanımlanacak OptionResponse DTO'su)

    // JPA için argümansız constructor
    public QuestionResponse() {
    }

    // Question Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public QuestionResponse(Question question) {
        this.id = question.getId();
        this.number = question.getNumber();
        this.questionSentence = question.getQuestionSentence();
        // QuestionType ilişkisinden sadece tip adını alalım
        this.questionType = question.getType() != null ? question.getType().getTypeName() : null;

        // Eğer soru çoktan seçmeli ise şıkları DTO'ya dönüştürüp ekleyelim
        if (((TestQuestion)question).getOptions() != null && !((TestQuestion)question).getOptions().isEmpty()) {
             this.options = ((TestQuestion)question).getOptions().stream()
                                   .map(OptionResponse::new) // Her Option Entity'sini OptionResponse DTO'suna dönüştür
                                   .collect(Collectors.toList());
        } else {
             this.options = List.of(); // Şık yoksa boş liste döndür
        }
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getQuestionSentence() { return questionSentence; }
    public void setQuestionSentence(String questionSentence) { this.questionSentence = questionSentence; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public List<OptionResponse> getOptions() { return options; }
    public void setOptions(List<OptionResponse> options) { this.options = options; }


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}