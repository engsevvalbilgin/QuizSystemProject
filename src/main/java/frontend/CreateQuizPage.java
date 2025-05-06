package com.quizprojesi2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CreateQuizPage extends JFrame {
    private JTextField quizTitleField;
    private JTextArea quizDescriptionArea;
    private JButton addQuestionButton, saveQuizButton, cancelButton;
    private JPanel questionsPanel;
    private List<QuestionPanel> questionPanels;

    public CreateQuizPage() {
        setTitle("Quiz Oluştur");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Quiz Bilgileri Paneli
        JPanel quizInfoPanel = new JPanel(new BorderLayout(5, 5));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.add(new JLabel("Quiz Başlığı:"));
        quizTitleField = new JTextField(30);
        titlePanel.add(quizTitleField);

        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Quiz Açıklaması:"), BorderLayout.NORTH);
        quizDescriptionArea = new JTextArea(3, 30);
        quizDescriptionArea.setLineWrap(true);
        quizDescriptionArea.setWrapStyleWord(true);
        descPanel.add(new JScrollPane(quizDescriptionArea), BorderLayout.CENTER);

        quizInfoPanel.add(titlePanel, BorderLayout.NORTH);
        quizInfoPanel.add(descPanel, BorderLayout.CENTER);

        mainPanel.add(quizInfoPanel, BorderLayout.NORTH);

        // Sorular Paneli
        questionPanels = new ArrayList<>();
        questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        addQuestion(); // Başlangıçta bir soru ekle

        // Buton Paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addQuestionButton = new JButton("Soru Ekle");
        saveQuizButton = new JButton("Quiz'i Kaydet");
        cancelButton = new JButton("İptal");

        buttonPanel.add(addQuestionButton);
        buttonPanel.add(saveQuizButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Event Listeners
        addQuestionButton.addActionListener(e -> addQuestion());

        saveQuizButton.addActionListener(e -> {
            if (validateQuiz()) {
                saveQuiz();
                JOptionPane.showMessageDialog(this, "Quiz başarıyla kaydedildi!");
                new TeacherDashboard().setVisible(true); // Bu sınıf tanımlı olmalı
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            new TeacherDashboard().setVisible(true);
            dispose();
        });

        add(mainPanel);
    }

    private void addQuestion() {
        QuestionPanel qp = new QuestionPanel(questionPanels.size() + 1);
        questionPanels.add(qp);
        questionsPanel.add(qp);
        questionsPanel.revalidate();
        questionsPanel.repaint();
    }

    private boolean validateQuiz() {
        if (quizTitleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Quiz başlığı boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (questionPanels.isEmpty()) {
            JOptionPane.showMessageDialog(this, "En az bir soru eklemelisiniz!", "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        for (QuestionPanel qp : questionPanels) {
            if (!qp.isValidInput()) return false;
        }

        return true;
    }

    private void saveQuiz() {
        // Quiz bilgilerini al
        String title = quizTitleField.getText();
        String description = quizDescriptionArea.getText();

        // Gerçek uygulamada burada veritabanına kaydedilir
        System.out.println("Quiz Kaydedildi: " + title + " - " + description);
    }

    private void renumberQuestions() {
        for (int i = 0; i < questionPanels.size(); i++) {
            questionPanels.get(i).setBorder(BorderFactory.createTitledBorder("Soru #" + (i + 1)));
        }
    }

    // İç sınıf: Soru Paneli
    private class QuestionPanel extends JPanel {
        private JTextField questionField;
        private JRadioButton[] optionButtons;
        private JTextField[] optionFields;
        private JButton removeButton;

        public QuestionPanel(int questionNumber) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Soru #" + questionNumber));

            JPanel centerPanel = new JPanel(new BorderLayout());

            // Soru Alanı
            JPanel questionTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            questionTextPanel.add(new JLabel("Soru:"));
            questionField = new JTextField(40);
            questionTextPanel.add(questionField);

            centerPanel.add(questionTextPanel, BorderLayout.NORTH);

            // Seçenekler
            JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            ButtonGroup group = new ButtonGroup();
            optionButtons = new JRadioButton[4];
            optionFields = new JTextField[4];

            for (int i = 0; i < 4; i++) {
                JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                optionButtons[i] = new JRadioButton();
                group.add(optionButtons[i]);
                optionPanel.add(optionButtons[i]);

                optionFields[i] = new JTextField(30);
                optionPanel.add(optionFields[i]);
                optionsPanel.add(optionPanel);
            }

            optionButtons[0].setSelected(true); // İlk seçenek varsayılan doğru cevap

            centerPanel.add(optionsPanel, BorderLayout.CENTER);
            add(centerPanel, BorderLayout.CENTER);

            // Soru silme butonu
            removeButton = new JButton("X");
            removeButton.addActionListener(e -> {
                questionPanels.remove(this);
                questionsPanel.remove(this);
                questionsPanel.revalidate();
                questionsPanel.repaint();
                renumberQuestions();
            });

            add(removeButton, BorderLayout.EAST);
        }

        public boolean isValidInput() {
            if (questionField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(CreateQuizPage.this, "Soru boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            for (JTextField tf : optionFields) {
                if (tf.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(CreateQuizPage.this, "Tüm seçenekler doldurulmalı!", "Hata", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

            return true;
        }

        public int getCorrectAnswerIndex() {
            for (int i = 0; i < optionButtons.length; i++) {
                if (optionButtons[i].isSelected()) return i;
            }
            return -1;
        }
    }

    // Test etmek için main
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new CreateQuizPage().setVisible(true));
//    }
}
