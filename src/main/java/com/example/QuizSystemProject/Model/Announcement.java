package com.example.QuizSystemProject.Model;
 // Paket adınızın doğru olduğundan emin olun

import jakarta.persistence.*; // JPA anotasyonları için
import java.time.LocalDateTime; // Tarih/saat için
import java.util.Objects; // equals/hashCode için

@Entity // Bu sınıfın bir JPA Entity'si olduğunu belirtir
@Table(name = "announcements")
// Veritabanındaki tablonun adı 'announcements' olacak
public class Announcement {

    @Id // Birincil anahtar
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Otomatik artan ID
    private Long id; // Long tipinde ID

    @Column(nullable = false, length = 100) // Boş olamaz, maks 100 karakter
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // Boş olamaz, daha uzun metin için TEXT tipi
    private String content;

    @Column(nullable = false) // Boş olamaz
    private LocalDateTime date; // Duyuru tarihi (oluşturulma tarihi olabilir)

    // Duyuruyu kimin yayınladığını belirten ilişki
    // Sizin template'inizdeki 'publisherId' alanına karşılık gelir
     // İlişkili User objesi
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;
    // JPA için argümansız constructor
    public Announcement() {
    }

    // Alanları alan constructor (ID ve date otomatik yönetilebilir)
    // Publisher, ilişki kurulduktan sonra set edilmeli veya constructor'a dahil edilmeli
    public Announcement(String title, String content, Admin publisher) {
        this.title = title;
        this.content = content;
        this.date = LocalDateTime.now(); // Duyuru oluşturulduğunda otomatik tarih atayalım
        this.admin = publisher;
    }


    // Getter ve Setter Metotları
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } // ID setter'ı genellikle kullanılmaz

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Admin getAdmin() { return this.getAdmin(); }
    public void setAdmin(Admin publisher) { this.admin=publisher; }

    // equals() ve hashCode() (ID üzerinden)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Announcement that = (Announcement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString() (Debugging için)
    @Override
    public String toString() {
        return "Announcement{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", date=" + date +
               ", publisher=" + (getAdmin() != null ? getAdmin().getUsername() : "null") + // Publisher username'ini gösterelim, tüm User objesini değil
               '}';
    }
}

