import React from 'react';
import { Link } from 'react-router-dom';

function MainScreen() {
  // CSS değişkenlerini JavaScript objeleri olarak tanımlayabiliriz
  const colors = {
    primary: '#4CAF50', // Yeşil
    secondary: '#FFC107', // Sarı
    accent: '#03A9F4', // Açık Mavi
    text: '#333',
    bg: '#f4f7f6',
    white: '#ffffff',
    grayLight: '#ececec',
    grayDark: '#666',
  };

  const commonButtonStyles = {
    display: 'inline-block',
    padding: '12px 25px',
    borderRadius: '5px',
    fontSize: '1.1em',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.3s ease',
    margin: '0 10px',
    border: 'none', // Inline stillerde varsayılan border'ı kaldırmak için
  };

  return (
    <div style={{ padding: '40px 0', textAlign: 'center' }}>
      <div style={{
        backgroundColor: colors.primary,
        color: colors.white,
        padding: '80px 20px',
        borderRadius: '10px',
        marginBottom: '40px',
        boxShadow: '0 5px 15px rgba(0, 0, 0, 0.1)',
      }}>
        <h1 style={{ fontSize: '3.5em', marginBottom: '20px', fontWeight: '700' }}>
          QuizLand'e Hoş Geldiniz!
        </h1>
        <p style={{
          fontSize: '1.3em',
          lineHeight: '1.6',
          maxWidth: '700px',
          margin: '0 auto 30px auto',
        }}>
          Bilginizi test etmeye hazır mısınız? Binlerce eğlenceli ve öğretici quiz ile kendinizi geliştirin, arkadaşlarınızla yarışın!
        </p>
        <div style={{ marginTop: '30px' }}>
          <Link
            to="/login"
            style={{
              ...commonButtonStyles,
              backgroundColor: colors.secondary,
              color: colors.text,
              border: `2px solid ${colors.secondary}`,
            }}
            // Hover efekti için JavaScript ile event listener eklemek gerekir, bu karmaşıklaşır.
            // Bu yüzden inline stillerde hover efektleri zorlayıcıdır.
          >
            Giriş Yap
          </Link>
          <Link
            to="/register"
            style={{
              ...commonButtonStyles,
              backgroundColor: 'transparent',
              color: colors.white,
              border: `2px solid ${colors.white}`,
            }}
          >
            Kaydol
          </Link>
        </div>
      </div>

      <div style={{ padding: '40px 0' }}>
        <h2 style={{ fontSize: '2.5em', marginBottom: '40px', color: colors.primary }}>
          Neden QuizLand?
        </h2>
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
          gap: '30px',
          padding: '0 20px',
        }}>
          <div style={{
            backgroundColor: colors.white,
            padding: '30px',
            borderRadius: '10px',
            boxShadow: '0 4px 10px rgba(0, 0, 0, 0.08)',
            textAlign: 'left',
            // Hover için JavaScript event listener gerekir
          }}>
            <h3 style={{ fontSize: '1.5em', color: colors.accent, marginBottom: '15px' }}>
              Geniş Kategori Yelpazesi
            </h3>
            <p style={{ fontSize: '1em', lineHeight: '1.6', color: colors.grayDark }}>
              Tarihten bilime, sanattan spora kadar birçok konuda quizlerle bilginizi genişletin.
            </p>
          </div>
          <div style={{
            backgroundColor: colors.white,
            padding: '30px',
            borderRadius: '10px',
            boxShadow: '0 4px 10px rgba(0, 0, 0, 0.08)',
            textAlign: 'left',
          }}>
            <h3 style={{ fontSize: '1.5em', color: colors.accent, marginBottom: '15px' }}>
              Eğlenceli ve Öğretici
            </h3>
            <p style={{ fontSize: '1em', lineHeight: '1.6', color: colors.grayDark }}>
              Sıkılmadan yeni şeyler öğrenin ve kendinize meydan okuyun.
            </p>
          </div>
          <div style={{
            backgroundColor: colors.white,
            padding: '30px',
            borderRadius: '10px',
            boxShadow: '0 4px 10px rgba(0, 0, 0, 0.08)',
            textAlign: 'left',
          }}>
            <h3 style={{ fontSize: '1.5em', color: colors.accent, marginBottom: '15px' }}>
              Arkadaşlarınla Yarış
            </h3>
            <p style={{ fontSize: '1em', lineHeight: '1.6', color: colors.grayDark }}>
              Arkadaşlarınıza meydan okuyun ve liderlik tablosunda yerinizi alın!
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default MainScreen;