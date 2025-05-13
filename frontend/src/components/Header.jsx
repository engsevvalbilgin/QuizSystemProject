// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\components\Header.jsx
import React from 'react';
import { Link } from 'react-router-dom'; // <-- Navigasyon linkleri için Link komponentini import et

function Header() {
  return (
    <header className="app-header"> {/* CSS için class ekledik */}
      <div className="container"> {/* İçeriği ortalamak ve genişliği sınırlamak için */}
        <h1>
          <Link to="/"> {/* Başlık tıklandığında anasayfaya (/) gidecek */}
            QuizLand {/* <-- Web sitesi başlığı */}
          </Link>
        </h1>
        <nav> {/* Navigasyon linkleri için */}
          <ul>
            <li>
              <Link to="/login">Giriş Yap</Link> {/* <-- Login linki */}
            </li>
            <li>
              <Link to="/register">Kaydol</Link> {/* <-- Kaydol linki */}
            </li>
            {/* TODO: Kullanıcı giriş yapmışsa "Giriş Yap / Kaydol" yerine "Profil / Çıkış Yap" gibi linkler göstereceğiz */}
            {/* TODO: Kullanıcının rolüne göre Admin / Öğretmen Paneli linklerini göstereceğiz */}
          </ul>
        </nav>
      </div>
    </header>
  );
}

export default Header; // Komponenti dışarıya aktarıyoruz