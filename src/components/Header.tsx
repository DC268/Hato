/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';

interface HeaderProps {
  currentTab: string;
  userRole: string;
  onRoleClick: () => void;
}

export const Header: React.FC<HeaderProps> = ({ currentTab, userRole, onRoleClick }) => {
  const getTabTitle = () => {
    switch (currentTab) {
      case 'mapa':
        return 'Mapa';
      case 'animales':
        return 'Animales';
      case 'zonas':
        return 'Zonas';
      case 'alertas':
        return 'Alertas';
      case 'mas':
        return 'Backend & Más';
      default:
        return 'Mapa';
    }
  };

  return (
    <header className="fixed top-0 w-full z-50 pt-safe bg-[#f8faf3]/90 backdrop-blur-xl border-b border-[#c0c9bc]/50 shadow-[0_1px_8px_rgba(0,0,0,0.04)]">
      <div className="h-16 px-4 flex items-center justify-between max-w-7xl mx-auto">
        {/* Brand & Context */}
        <div className="flex items-center gap-2 min-w-0">
          <img
            alt="Hato Logo"
            className="h-8 w-auto object-contain flex-shrink-0"
            src="https://lh3.googleusercontent.com/aida/AEtjO1X9odRuLB8ViyWqfZROXLxFx-G2UJpffp0ggt2liMFN0J6eRPS0XSlpYvjgbqiZE4QwE5TMXwcx1u7uPxFlU0rOnVVoihSl67gN2ILviLz0WDmIX5LnHAUJH3Ke5dsJRhLBPPvIdRXAMFrPshiSmIolXZEtb7I6GCBM4Vs1N4S5AC3xNliE7pLwjtxEa9pUiWDwbctqIKzs4OQT19a4UIk4aj7YfvSPAUj5MR_T7aJhOUl6Ky-teb8DrM8"
          />
          <span className="font-headline text-2xl tracking-tight uppercase text-primary font-bold">
            Hato
          </span>
          <span className="text-[#71796e] text-lg font-light mx-1">/</span>
          <h1 className="font-headline text-xl text-[#191c18] truncate tracking-normal uppercase font-semibold">
            {getTabTitle()}
          </h1>
        </div>

        {/* User Role / Profile Action */}
        <div className="flex items-center gap-2 flex-shrink-0">
          <button
            onClick={onRoleClick}
            className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-surface-container active:bg-surface-container-high transition-colors focus:outline-none"
            title="Cambiar rol o configuración"
            type="button"
          >
            <span className="text-xs font-bold uppercase tracking-wider text-[#12521e]">
              {userRole}
            </span>
            <div className="w-7 h-7 rounded-full bg-primary flex items-center justify-center text-white">
              <span className="material-symbols-outlined text-[17px]">person</span>
            </div>
          </button>
        </div>
      </div>
    </header>
  );
};
