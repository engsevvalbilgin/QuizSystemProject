import React from 'react';
import Header from './Header'; 
import { Outlet } from 'react-router-dom'; 

function MainLayout() {
  return (
    <div>
      <Header /> 
      <main className="main-content"> 
        <Outlet /> 
      </main>

    </div>
  );
}

export default MainLayout;