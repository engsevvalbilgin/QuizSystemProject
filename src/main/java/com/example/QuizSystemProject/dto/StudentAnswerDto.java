package com.example.QuizSystemProject.dto;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class StudentAnswerDto {
    private int questionId;
    private List<Integer> selectedOptionIds; // Çoktan seçmeli sorular için seçilen şık ID'leri
    private String textAnswer; // Açık uçlu sorular için (answerText ile uyumlu olması için)
    private AnswerType answerType = AnswerType.MULTIPLE_CHOICE; // Varsayılan cevap tipi
    private float score = 0; // Soru için kazanılan puan
    private String aiExplanation; // AI tarafından verilen açıklama (açık uçlu sorular için)
    
    // Eski alan için uyumluluk metodları
    public String getAnswerText() {
        return textAnswer;
    }
    
    public void setAnswerText(String answerText) {
        this.textAnswer = answerText;
    }
    
    // Geriye dönük uyumluluk için
    public Integer getSelectedOptionId() {
        return selectedOptionIds != null && !selectedOptionIds.isEmpty() ? selectedOptionIds.get(0) : null;
    }
    
    public void setSelectedOptionId(Integer selectedOptionId) {
        if (selectedOptionId != null) {
            this.selectedOptionIds = new ArrayList<>();
            this.selectedOptionIds.add(selectedOptionId);
        } else {
            this.selectedOptionIds = null;
        }
    }
}
