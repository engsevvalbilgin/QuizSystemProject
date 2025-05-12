package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.*;

// Bu DTO, API yanıtlarında bir çoktan seçmeli soruya ait şık bilgilerini taşır.
// Doğru cevap bilgisi (isCorrect) dahil EDİLMEZ.
public class OptionResponse {

    private int id;
    private String text; // Şık metni

    // isCorrect alanı burada dahil edilmez, çünkü öğrencilere gösterilmemelidir.
    // Doğru cevap bilgisi, sadece öğretmen/admin ekranlarında veya ayrı bir cevap inceleme DTO'sunda yer alır.


    // JPA için argümansız constructor
    public OptionResponse() {
    }

    // Option Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public OptionResponse(Option option) {
        this.id = option.getId();
        this.text = option.getText();
        // isCorrect alanı buradan atlanıyor
    }

    // Getter ve Setterlar
    // IDE ile otomatik oluşturabilirsiniz.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    // isCorrect için getter/setter eklenmez


    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}