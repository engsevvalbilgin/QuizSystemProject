// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\components\MainLayout.jsx
import React from 'react';
import Header from './Header'; // <-- Header komponentini import edeceğiz
import { Outlet } from 'react-router-dom'; // <-- Alt rotaları render etmek için Outlet'i import et

function MainLayout() {
  return (
    <div>
      {/* Uygulamanın üst kısmında sabit duracak Header */}
      <Header /> {/* <-- Header komponenti burada */}

      {/* Ana içerik alanı */}
      {/* Outlet, bu layout içindeki eşleşen alt rotanın (sayfanın) komponentini render eder */}
      <main className="main-content"> {/* CSS için class ekledik */}
        <Outlet /> {/* <-- Eşleşen sayfa içeriği buraya gelecek */}
      </main>

      {/* İstenirse buraya bir Footer eklenebilir */}
    </div>
  );
}

export default MainLayout; // Komponenti dışarıya aktarıyoruz