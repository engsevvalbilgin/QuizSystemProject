package com.quizprojesi2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TeacherDashboard extends JFrame {
    private JButton createQuizButton, viewQuizzesButton, updateProfileButton, logoutButton;

    public TeacherDashboard() {
        setTitle("Öğretmen Paneli");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        
        // Başlık paneli
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("ÖĞRETMEN PANELİ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);

        // Buton paneli
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        createQuizButton = new JButton("Quiz Oluştur");
        viewQuizzesButton = new JButton("Quizleri Görüntüle");
        updateProfileButton = new JButton("Profilimi Güncelle");
        logoutButton = new JButton("Çıkış Yap");

        // Buton stilleri
        Font buttonFont = new Font("Arial", Font.PLAIN, 16);
        createQuizButton.setFont(buttonFont);
        viewQuizzesButton.setFont(buttonFont);
        updateProfileButton.setFont(buttonFont);
        logoutButton.setFont(buttonFont);

        buttonPanel.add(createQuizButton);
        buttonPanel.add(viewQuizzesButton);
        buttonPanel.add(updateProfileButton);
        buttonPanel.add(logoutButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // Action listeners
        createQuizButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CreateQuizPage().setVisible(true);
                dispose();
            }
        });

        viewQuizzesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewQuizzesPage().setVisible(true);
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
                    TeacherDashboard.this, 
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