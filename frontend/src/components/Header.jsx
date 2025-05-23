// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\components\Header.jsx
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import LogoutButton from './LogoutButton';

function Header() {
  const navigate = useNavigate();
  const isAuthenticated = !!localStorage.getItem('token'); // Check if token exists

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
            {!isAuthenticated ? (
              <>
                <li>
                  <Link to="/login">Giriş Yap</Link> {/* <-- Login linki */}
                </li>
                <li>
                  <Link to="/register">Kaydol</Link> {/* <-- Kaydol linki */}
                </li>
              </>
            ) : (
              <>
                <li>
                  <LogoutButton />
                </li>
              </>
            )}
          </ul>
        </nav>
      </div>
    </header>
  );
}

export default Header; // Komponenti dışarıya aktarıyoruz