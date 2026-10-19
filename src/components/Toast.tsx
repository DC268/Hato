/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';

interface ToastProps {
  message: string | null;
  onClose: () => void;
}

export const Toast: React.FC<ToastProps> = ({ message }) => {
  if (!message) return null;

  return (
    <div className="fixed bottom-24 left-1/2 -translate-x-1/2 bg-inverse-surface text-inverse-on-surface px-4 py-3 rounded-xl shadow-xl flex items-center gap-2 transform transition-all duration-300 z-50 max-w-[90vw] animate-in fade-in slide-in-from-bottom-2 border border-outline">
      <span className="material-symbols-outlined text-primary-fixed text-[22px]">
        check_circle
      </span>
      <span className="text-sm font-bold truncate">{message}</span>
    </div>
  );
};
