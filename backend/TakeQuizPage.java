package com.quizprojesi2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TakeQuizPage extends JFrame {
    private JLabel quizTitleLabel, questionLabel, timerLabel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionGroup;
    private JButton nextButton;
    private int currentQuestionIndex = 0;
    private String[] questions = {
        "What is the capital of France?",
        "Which language is used for Android development?",
        "What is 2 + 2?"
    };
    private String[][] options = {
        {"Paris", "London", "Berlin", "Rome"},
        {"Java", "Python", "Swift", "C#"},
        {"3", "4", "5", "6"}
    };
    private int[] correctAnswers = {0, 0, 1}; // index of correct options
    private int score = 0;

    public TakeQuizPage() {
        setTitle("Take Quiz");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        quizTitleLabel = new JLabel("Quiz Time!", SwingConstants.CENTER);
        quizTitleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(quizTitleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(6, 1));
        questionLabel = new JLabel();
        centerPanel.add(questionLabel);

        optionButtons = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionGroup.add(optionButtons[i]);
            centerPanel.add(optionButtons[i]);
        }

        add(centerPanel, BorderLayout.CENTER);

        nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAnswer();
                currentQuestionIndex++;
                if (currentQuestionIndex < questions.length) {
                    loadQuestion(currentQuestionIndex);
                } else {
                    showResult();
                }
            }
        });

        add(nextButton, BorderLayout.SOUTH);

        loadQuestion(currentQuestionIndex);
        setVisible(true);
    }

    private void loadQuestion(int index) {
        questionLabel.setText("Q" + (index + 1) + ": " + questions[index]);
        optionGroup.clearSelection();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options[index][i]);
        }
    }

    private void checkAnswer() {
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected() && i == correctAnswers[currentQuestionIndex]) {
                score++;
            }
        }
    }

    private void showResult() {
        JOptionPane.showMessageDialog(this, "Quiz Over! Your score is: " + score + "/" + questions.length);
        dispose();
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new TakeQuizPage());
//    }
}
