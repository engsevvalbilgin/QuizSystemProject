package com.quizprojesi2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ViewQuizzesPage extends JFrame {
    private JTable quizzesTable;
    private JButton viewButton, editButton, deleteButton, backButton;

    public ViewQuizzesPage() {
        setTitle("Quizleri Görüntüle");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tablo başlığı
        JLabel titleLabel = new JLabel("OLUŞTURDUĞUM QUIZLER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Tablo verileri (demo amaçlı)
        String[] columnNames = {"ID", "Quiz Adı", "Açıklama", "Soru Sayısı", "Oluşturulma Tarihi"};
        Object[][] data = {
            {1, "Matematik Testi", "Temel matematik bilgileri", 10, "2023-05-15"},
            {2, "Tarih Quiz", "Osmanlı tarihi", 15, "2023-06-20"},
            {3, "Bilgisayar Bilimleri", "Programlama temelleri", 20, "2023-07-10"}
        };

        // Quiz tablosu
        quizzesTable = new JTable(data, columnNames);
        quizzesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(quizzesTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        viewButton = new JButton("Görüntüle");
        editButton = new JButton("Düzenle");
        deleteButton = new JButton("Sil");
        backButton = new JButton("Geri Dön");
        
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = quizzesTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(ViewQuizzesPage.this, 
                        "Lütfen bir quiz seçiniz!", 
                        "Uyarı", JOptionPane.WARNING_MESSAGE);
                } else {
                    int quizId = (int) quizzesTable.getValueAt(selectedRow, 0);
                    viewQuizDetails(quizId);
                }
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = quizzesTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(ViewQuizzesPage.this, 
                        "Lütfen bir quiz seçiniz!", 
                        "Uyarı", JOptionPane.WARNING_MESSAGE);
                } else {
                    int quizId = (int) quizzesTable.getValueAt(selectedRow, 0);
                    editQuiz(quizId);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = quizzesTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(ViewQuizzesPage.this, 
                        "Lütfen bir quiz seçiniz!", 
                        "Uyarı", JOptionPane.WARNING_MESSAGE);
                } else {
                    int confirm = JOptionPane.showConfirmDialog(
                        ViewQuizzesPage.this, 
                        "Seçili quizi silmek istediğinize emin misiniz?", 
                        "Silme Onayı", 
                        JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        int quizId = (int) quizzesTable.getValueAt(selectedRow, 0);
                        deleteQuiz(quizId);
                    }
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TeacherDashboard().setVisible(true);
                dispose();
            }
        });

        add(mainPanel);
    }

    private void viewQuizDetails(int quizId) {
        // Quiz detaylarını görüntüleme işlemi
        JOptionPane.showMessageDialog(this, 
            "Quiz #" + quizId + " detayları görüntülenecek.", 
            "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editQuiz(int quizId) {
        // Quiz düzenleme işlemi
        JOptionPane.showMessageDialog(this, 
            "Quiz #" + quizId + " düzenlenecek.", 
            "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteQuiz(int quizId) {
        // Quiz silme işlemi
        JOptionPane.showMessageDialog(this, 
            "Quiz #" + quizId + " silindi.", 
            "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }
}