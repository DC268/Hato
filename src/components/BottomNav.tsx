/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';

interface BottomNavProps {
  currentTab: string;
  onTabChange: (tab: string) => void;
  pendingAlertsCount: number;
}

export const BottomNav: React.FC<BottomNavProps> = ({
  currentTab,
  onTabChange,
  pendingAlertsCount,
}) => {
  const tabs = [
    { id: 'mapa', label: 'Mapa', icon: 'map' },
    { id: 'animales', label: 'Animales', icon: 'pets' },
    { id: 'zonas', label: 'Zonas', icon: 'fence' },
    { id: 'alertas', label: 'Alertas', icon: 'notifications', badge: pendingAlertsCount },
    { id: 'mas', label: 'Más', icon: 'menu' },
  ];

  return (
    <nav className="fixed bottom-0 w-full z-50 pb-safe bg-[#f8faf3]/95 backdrop-blur-xl border-t border-[#c0c9bc]/50 shadow-[0_-2px_12px_rgba(0,0,0,0.06)]">
      <div className="flex items-stretch justify-around h-20 px-2 max-w-lg mx-auto">
        {tabs.map((tab) => {
          const isActive = currentTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`group flex-1 min-w-[48px] flex flex-col items-center justify-center py-1 px-1 transition-all ${
                isActive ? 'text-primary font-bold' : 'text-on-surface-variant'
              }`}
              type="button"
            >
              <div
                className={`relative w-14 h-8 rounded-full flex items-center justify-center transition-colors mb-0.5 ${
                  isActive ? 'bg-on-primary-container text-primary' : 'hover:bg-surface-container'
                }`}
              >
                <span className="material-symbols-outlined text-[24px]">{tab.icon}</span>
                {tab.badge && tab.badge > 0 ? (
                  <span className="absolute top-1 right-2.5 min-w-[16px] h-4 px-1 rounded-full bg-error text-white text-[10px] leading-4 flex items-center justify-center font-bold shadow-sm">
                    {tab.badge}
                  </span>
                ) : null}
              </div>
              <span className="text-[11px] leading-tight tracking-wider truncate uppercase font-bold">
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
