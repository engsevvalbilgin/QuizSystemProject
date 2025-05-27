import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LogoutButton from './LogoutButton';

function Header() {
  const { token } = useAuth();
  const isAuthenticated = token !== null;

  const colors = {
    primary: '#4CAF50', // Yeşil
    secondary: '#FFC107', // Sarı (kullanılmıyor ama örnek olsun diye duruyor)
    accent: '#03A9F4', // Açık Mavi (kullanılmıyor)
    text: '#333',
    white: '#ffffff',
    grayLight: '#ececec',
    grayDark: '#666',
  };

  const commonNavLinkStyles = {
    color: colors.grayDark,
    fontWeight: '500',
    padding: '8px 12px',
    borderRadius: '5px',
    transition: 'all 0.3s ease',
    textDecoration: 'none', // Link olduğu için
  };

  const primaryNavBtnStyles = {
    backgroundColor: colors.primary,
    color: colors.white,
    padding: '8px 15px',
    borderRadius: '5px',
    transition: 'all 0.3s ease',
    textDecoration: 'none', // Link olduğu için
  };

  return (
    <header style={{
      backgroundColor: colors.white,
      boxShadow: '0 2px 4px rgba(0, 0, 0, 0.05)',
      padding: '15px 0',
      position: 'sticky',
      top: '0',
      zIndex: '1000',
    }}>
      <div style={{
        maxWidth: '1200px',
        margin: '0 auto',
        padding: '0 20px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}>
        <div style={{}}> {/* Logo için ek div */}
          <Link to="/" style={{
            fontSize: '2em',
            fontWeight: '700',
            color: colors.primary,
            textDecoration: 'none', // Link olduğu için
          }}>
            QuizLand
          </Link>
        </div>
        <nav>
          <ul style={{
            listStyle: 'none',
            margin: '0',
            padding: '0',
            display: 'flex',
            gap: '20px',
          }}>
            {isAuthenticated ? (
              <li>
                <LogoutButton />
              </li>
            ) : (
              <>
                <li>
                  <Link
                    to="/login"
                    style={{ ...commonNavLinkStyles }}
                    // Hover efekti için JavaScript event listener gerekir
                  >
                    Giriş Yap
                  </Link>
                </li>
                <li>
                  <Link
                    to="/register"
                    style={{ ...primaryNavBtnStyles }}
                    // Hover efekti için JavaScript event listener gerekir
                  >
                    Kaydol
                  </Link>
                </li>
              </>
            )}
          </ul>
        </nav>
      </div>
    </header>
  );
}

export default Header;