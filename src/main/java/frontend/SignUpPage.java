package com.quizprojesi2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignUpPage extends JFrame {
    private JTextField usernameField, nameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleComboBox;
    private JButton signUpButton, backButton;

    public SignUpPage() {
        setTitle("Quiz Sistemi - Kayıt Ol");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("YENİ KULLANICI KAYDI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("Ad Soyad:"), gbc);
        
        gbc.gridx = 1;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Kullanıcı Adı:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("E-posta:"), gbc);
        
        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Şifre:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Şifre Tekrar:"), gbc);
        
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Rol:"), gbc);
        
        gbc.gridx = 1;
        String[] roles = {"Öğrenci", "Öğretmen"};
        roleComboBox = new JComboBox<>(roles);
        panel.add(roleComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        signUpButton = new JButton("Kayıt Ol");
        backButton = new JButton("Geri Dön");
        
        buttonPanel.add(signUpButton);
        buttonPanel.add(backButton);
        panel.add(buttonPanel, gbc);

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    // Kayıt işlemi yapılacak
                    JOptionPane.showMessageDialog(SignUpPage.this, 
                        "Kayıt başarılı! Giriş yapabilirsiniz.", 
                        "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    new LoginPage().setVisible(true);
                    dispose();
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginPage().setVisible(true);
                dispose();
            }
        });

        add(panel);
    }

    private boolean validateForm() {
        String name = nameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Tüm alanları doldurunuz!", 
                "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                "Şifreler uyuşmuyor!", 
                "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                "Şifre en az 6 karakter olmalıdır!", 
                "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
}