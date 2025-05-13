// C:\Users\Hakan\Desktop\devam\front\QuizLandFrontend\src\main.jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx'; // Ana App componentimiz
import './index.css'; // Global CSS dosyamız
import { BrowserRouter } from 'react-router-dom'; // <-- BrowserRouter importu
import { AuthProvider } from './context/AuthContext.jsx'; // <-- AuthProvider importu

ReactDOM.createRoot(document.getElementById('root')).render(
  // React.StrictMode, geliştirme sırasında olası sorunları belirlemeye yardımcı olur
  <React.StrictMode>
    {/* Uygulamamızı BrowserRouter ile sarmalıyoruz */}
    <BrowserRouter> {/* <-- Burası BrowserRouter başlangıcı */}
      <AuthProvider> {/* <-- AuthProvider BAŞLANGIÇ */}
        <App /> {/* Ana App komponentimiz */}
      </AuthProvider> {/* <-- AuthProvider SONU */}
    </BrowserRouter>{/* <-- Burası BrowserRouter sonu */}
  </React.StrictMode>,
);
