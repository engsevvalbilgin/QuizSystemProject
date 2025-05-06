package com.quizprojesi2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, signUpButton;

    public LoginPage() {
        setTitle("Quiz Sistemi - Giriş");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("QUİZ SİSTEMİ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("Kullanıcı Adı:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Şifre:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginButton = new JButton("Giriş Yap");
        panel.add(loginButton, gbc);

        gbc.gridy = 4;
        signUpButton = new JButton("Kayıt Ol");
        panel.add(signUpButton, gbc);
 // Google Login Button
        gbc.gridy = 5;
        JButton googleLoginButton = new JButton("Google ile Giriş Yap");
        panel.add(googleLoginButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Normal giriş
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (authenticate(username, password)) {
                    openDashboard();
                } else {
                    JOptionPane.showMessageDialog(LoginPage.this, 
                        "Geçersiz kullanıcı adı veya şifre!", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Giriş kontrolü yapılacak
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                
                if (authenticate(username, password)) {
                    openDashboard();
                } else {
                    JOptionPane.showMessageDialog(LoginPage.this, 
                        "Geçersiz kullanıcı adı veya şifre!", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SignUpPage().setVisible(true);
                dispose();
            }
        });
        // Google login handler
        googleLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Redirect to Spring Boot Google login
                // Redirect to the login page with Google OAuth2
               openGoogleLogin();
            }

            private void openGoogleLogin() {
            //spring bootla+ apiyle google ile giriş    
            }
        });
        add(panel);
    }

    private boolean authenticate(String username, String password) {
        // Burada veritabanından kullanıcı doğrulaması yapılacak
        // Şimdilik demo amaçlı sabit değerler
        return username.equals("admin") && password.equals("admin123");
    }

    private void openDashboard() {
        // Kullanıcı rolüne göre farklı dashboard açılacak
        new TeacherDashboard().setVisible(true);
        // veya new StudentDashboard().setVisible(true);
        dispose();
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            new LoginPage().setVisible(true);
//        });
//    }
}