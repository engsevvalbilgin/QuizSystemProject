package com.quizprojesi2;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ViewResultsPage extends JFrame {
    private JTextArea resultsTextArea;
    private JButton backButton;

    public ViewResultsPage() {
        setTitle("Sonuçlar");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Başlık paneli
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Quiz Sonuçları", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);

        // Sonuçları gösteren metin alanı
        resultsTextArea = new JTextArea();
        resultsTextArea.setEditable(false);
        resultsTextArea.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(resultsTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buton paneli
        JPanel buttonPanel = new JPanel();
        backButton = new JButton("Geri Dön");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        buttonPanel.add(backButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new StudentDashboard().setVisible(true);
                dispose();
            }
        });

        // Placeholder for quiz results
        displayResults();

        add(panel);
    }

    private void displayResults() {
        // Here, you can fetch the results from the database or some model
        String sampleResults = "Quiz 1: 80%\nQuiz 2: 95%\nQuiz 3: 70%\n";
        resultsTextArea.setText(sampleResults);
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new ViewResultsPage().setVisible(true));
//    }
}
