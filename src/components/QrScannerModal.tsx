/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { Animal } from '../types/domain';

interface QrScannerModalProps {
  animals: Animal[];
  onClose: () => void;
  onAnimalScanned: (animal: Animal) => void;
}

export const QrScannerModal: React.FC<QrScannerModalProps> = ({
  animals,
  onClose,
  onAnimalScanned,
}) => {
  const [isScanning, setIsScanning] = useState(true);

  const simulateScanTag = (animal: Animal) => {
    setIsScanning(false);
    if (typeof navigator !== 'undefined' && navigator.vibrate) {
      navigator.vibrate([80, 40, 80]);
    }
    setTimeout(() => {
      onAnimalScanned(animal);
      onClose();
    }, 400);
  };

  return (
    <div className="fixed inset-0 z-50 bg-[#191c18]/85 backdrop-blur-md flex flex-col justify-between p-4 animate-in fade-in">
      {/* Top Header */}
      <div className="flex items-center justify-between pt-safe text-white">
        <div className="flex items-center gap-2">
          <span className="material-symbols-outlined text-[24px] text-primary-fixed">
            qr_code_scanner
          </span>
          <span className="font-headline text-xl uppercase font-bold tracking-tight">
            Escáner RFID & QR de Campo
          </span>
        </div>
        <button
          onClick={onClose}
          className="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center text-white hover:bg-white/30"
          type="button"
        >
          <span className="material-symbols-outlined text-[20px]">close</span>
        </button>
      </div>

      {/* Camera Viewfinder Simulation */}
      <div className="flex-1 flex flex-col items-center justify-center py-6">
        <div className="relative w-64 h-64 border-2 border-primary-fixed rounded-3xl overflow-hidden flex items-center justify-center shadow-2xl">
          {/* Corner brackets */}
          <div className="absolute top-2 left-2 w-6 h-6 border-t-4 border-l-4 border-primary-fixed rounded-tl-lg"></div>
          <div className="absolute top-2 right-2 w-6 h-6 border-t-4 border-r-4 border-primary-fixed rounded-tr-lg"></div>
          <div className="absolute bottom-2 left-2 w-6 h-6 border-b-4 border-l-4 border-primary-fixed rounded-bl-lg"></div>
          <div className="absolute bottom-2 right-2 w-6 h-6 border-b-4 border-r-4 border-primary-fixed rounded-br-lg"></div>

          {/* Animated Laser Bar */}
          {isScanning && (
            <div className="absolute inset-x-0 h-1 bg-gradient-to-r from-transparent via-error to-transparent animate-pulse animate-bounce"></div>
          )}

          <div className="text-center p-4 text-white/80">
            <span className="material-symbols-outlined text-[48px] text-primary-fixed/80 mb-2">
              sensors
            </span>
            <p className="text-xs uppercase font-bold tracking-wider">
              Acerque el lector al arete RFID o código de barra
            </p>
          </div>
        </div>
        <p className="text-xs text-white/70 mt-4 text-center">
          Compatible con aretes ISO 11784/11785 FDX-B y códigos QR de collar
        </p>
      </div>

      {/* Quick Field Selection for testing */}
      <div className="bg-surface-container-lowest/90 backdrop-blur-md rounded-2xl p-4 flex flex-col gap-2 pb-safe border border-outline-variant/40">
        <span className="text-[11px] font-bold uppercase tracking-wider text-outline block">
          Simular Detección de Arete en el Brete:
        </span>
        <div className="grid grid-cols-3 gap-2">
          {animals.slice(0, 3).map((a) => (
            <button
              key={a.id}
              onClick={() => simulateScanTag(a)}
              className="p-2.5 rounded-xl bg-surface-container hover:bg-primary-fixed flex flex-col items-center justify-center text-center transition-colors active:scale-95"
            >
              <span className="font-headline text-lg font-bold text-on-surface leading-none">
                {a.displayNumber}
              </span>
              <span className="text-[10px] text-outline mt-0.5">{a.tagNumber}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
