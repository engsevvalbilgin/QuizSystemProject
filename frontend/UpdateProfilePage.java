package com.quizprojesi2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UpdateProfilePage extends JFrame {
    private JTextField nameField, usernameField, emailField;
    private JPasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    private JButton updateButton, cancelButton;

    public UpdateProfilePage() {
        setTitle("Profil Güncelleme");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("PROFİL BİLGİLERİNİ GÜNCELLE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
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
        panel.add(new JLabel("Mevcut Şifre:"), gbc);
        
        gbc.gridx = 1;
        currentPasswordField = new JPasswordField(20);
        panel.add(currentPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Yeni Şifre:"), gbc);
        
        gbc.gridx = 1;
        newPasswordField = new JPasswordField(20);
        panel.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Şifre Tekrar:"), gbc);
        
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        updateButton = new JButton("Güncelle");
        cancelButton = new JButton("İptal");
        
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        // Demo verileri yükle
        loadDemoData();

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    // Güncelleme işlemi yapılacak
                    JOptionPane.showMessageDialog(UpdateProfilePage.this, 
                        "Profil bilgileriniz başarıyla güncellendi!", 
                        "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    new TeacherDashboard().setVisible(true);
                    dispose();
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TeacherDashboard().setVisible(true);
                dispose();
            }
        });

        add(panel);
    }

    private void loadDemoData() {
        // Burada gerçekte veritabanından kullanıcı bilgileri çekilecek
        nameField.setText("Ahmet Yılmaz");
        usernameField.setText("ahmet_yilmaz");
        emailField.setText("ahmet@example.com");
    }

    private boolean validateForm() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Mevcut şifre kontrolü (gerçekte veritabanından kontrol edilecek)
        if (!currentPassword.equals("admin123")) {
            JOptionPane.showMessageDialog(this, 
                "Mevcut şifreniz yanlış!", 
                "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Yeni şifre alanları dolu mu kontrolü
        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, 
                    "Yeni şifreler uyuşmuyor!", 
                    "Hata", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(this, 
                    "Yeni şifre en az 6 karakter olmalıdır!", 
                    "Hata", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
}