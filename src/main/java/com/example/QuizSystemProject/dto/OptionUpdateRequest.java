package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OptionUpdateRequest {

    // ID'yi DTO'ya dahil etmek genellikle iyi bir practice değildir,
    // çünkü ID genellikle URL yolundan gelir. Ancak, eğer şıklar listesi gönderiliyorsa
    // hangi şıkkın güncellendiğini belirtmek için DTO'da ID'ye ihtiyaç duyulabilir.
    // Şimdilik, ID'yi DTO'ya eklemeyelim ve Service katmanında ID'ye göre şıkkı bulup güncelleyelim.
    // @NotNull(message = "Şık ID'si boş olamaz")
    // private Long id;

    @NotBlank(message = "Şık metni boş olamaz")
    @Size(max = 500, message = "Şık metni 500 karakterden uzun olamaz")
    private String text;

    @NotNull(message = "Doğru cevap bilgisi boş olamaz")
    private Boolean isCorrect;

    // Getter ve Setterlar
    public OptionUpdateRequest() {}

    // Eğer ID'yi eklediyseniz:
    // public Long getId() { return id; }
    // public void setId(Long id) { this.id = id; }


    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Boolean isCorrect() { return isCorrect; }
    public void setCorrect(Boolean correct) { isCorrect = correct; }
}