package com.quizprojesi2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StudentDashboard extends JFrame {
    private JButton takeQuizButton, viewResultsButton, updateProfileButton, logoutButton;

    public StudentDashboard() {
        setTitle("Öğrenci Paneli");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        
        // Başlık paneli
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("ÖĞRENCİ PANELİ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);

        // Buton paneli
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        takeQuizButton = new JButton("Quiz Çöz");
        viewResultsButton = new JButton("Sonuçları Görüntüle");
        updateProfileButton = new JButton("Profilimi Güncelle");
        logoutButton = new JButton("Çıkış Yap");

        // Buton stilleri
        Font buttonFont = new Font("Arial", Font.PLAIN, 16);
        takeQuizButton.setFont(buttonFont);
        viewResultsButton.setFont(buttonFont);
        updateProfileButton.setFont(buttonFont);
        logoutButton.setFont(buttonFont);

        buttonPanel.add(takeQuizButton);
        buttonPanel.add(viewResultsButton);
        buttonPanel.add(updateProfileButton);
        buttonPanel.add(logoutButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // Action listeners
        takeQuizButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TakeQuizPage().setVisible(true);
                dispose();
            }
        });

        viewResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewResultsPage().setVisible(true);
                dispose();
            }
        });

        updateProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new UpdateProfilePage().setVisible(true);
                dispose();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                    StudentDashboard.this, 
                    "Çıkış yapmak istediğinize emin misiniz?", 
                    "Çıkış", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    new LoginPage().setVisible(true);
                    dispose();
                }
            }
        });

        add(panel);
    }
}