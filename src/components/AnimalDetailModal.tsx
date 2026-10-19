/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { Animal, Position } from '../types/domain';

interface AnimalDetailModalProps {
  animal: Animal | null;
  position?: Position;
  onClose: () => void;
  onViewOnMap: (animalId: string) => void;
  onPingCollar: (trackerId: string) => void;
}

export const AnimalDetailModal: React.FC<AnimalDetailModalProps> = ({
  animal,
  position,
  onClose,
  onViewOnMap,
  onPingCollar,
}) => {
  if (!animal) return null;

  return (
    <div className="fixed inset-0 z-50 bg-[#191c18]/60 backdrop-blur-xs flex flex-col justify-end p-0 animate-in fade-in">
      <div className="w-full max-w-lg mx-auto bg-surface-container-lowest rounded-t-[28px] p-5 flex flex-col gap-3.5 shadow-2xl animate-in slide-in-from-bottom duration-200 border-t border-outline-variant/40 max-h-[90vh] overflow-y-auto">
        <div className="w-12 h-1.5 bg-outline-variant rounded-full mx-auto mb-1"></div>

        {/* Modal Header */}
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div
              className={`w-14 h-14 rounded-2xl flex items-center justify-center font-headline text-3xl font-bold shadow-xs text-white ${
                animal.healthStatus === 'ALERT'
                  ? 'bg-error'
                  : animal.healthStatus === 'ATTENTION'
                  ? 'bg-outline'
                  : 'bg-primary'
              }`}
            >
              {animal.displayNumber}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="font-headline text-2xl uppercase text-on-surface font-bold">
                  {animal.name}
                </h3>
                <span className="text-xs bg-surface-container-high px-2 py-0.5 rounded text-on-surface-variant uppercase font-bold">
                  {animal.tagNumber}
                </span>
              </div>
              <p className="text-xs text-on-surface-variant font-bold mt-0.5">
                {animal.breed} · {animal.ageYears} años {animal.weightKg ? `· ${animal.weightKg} kg` : ''}
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-10 h-10 rounded-full bg-surface-container flex items-center justify-center text-on-surface hover:bg-surface-container-high"
            type="button"
          >
            <span className="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>

        {/* Status Highlight Banner */}
        <div
          className={`p-3 rounded-xl flex items-center justify-between text-xs font-bold ${
            animal.healthStatus === 'ALERT'
              ? 'bg-error-container text-error border border-error/30'
              : animal.healthStatus === 'ATTENTION'
              ? 'bg-tertiary-fixed text-on-tertiary-fixed border border-tertiary/30'
              : 'bg-primary-fixed/40 text-primary border border-primary/30'
          }`}
        >
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-[18px]">
              {animal.healthStatus === 'ALERT'
                ? 'warning'
                : animal.healthStatus === 'ATTENTION'
                ? 'hourglass_top'
                : 'check_circle'}
            </span>
            <span>
              {animal.healthStatus === 'ALERT'
                ? 'Alerta Crítica: Fuera de Geocerca (140m fuera)'
                : animal.healthStatus === 'ATTENTION'
                ? 'En Observación / Inactivo'
                : 'Dentro de Potrero Asignado'}
            </span>
          </div>
          <span className="uppercase text-[10px] tracking-wider font-headline">
            {animal.healthStatus}
          </span>
        </div>

        {/* Telemetry & Collar Hardware Card */}
        <div className="bg-surface-container-low rounded-xl p-3.5 border border-outline-variant/40 flex flex-col gap-2.5">
          <span className="text-[11px] font-bold uppercase tracking-wider text-outline block">
            Telemetría de Collar GPS (Traccar / LoRa)
          </span>

          <div className="grid grid-cols-2 gap-3 text-xs">
            <div>
              <span className="text-outline block text-[11px]">Código Dispositivo:</span>
              <span className="font-mono font-bold text-on-surface">{animal.trackerId}</span>
            </div>
            <div>
              <span className="text-outline block text-[11px]">Batería Collar:</span>
              <span className="font-headline text-base font-bold text-on-surface">
                {position?.batteryPercent ?? 78}%
              </span>
            </div>
            <div>
              <span className="text-outline block text-[11px]">Velocidad Actual:</span>
              <span className="font-headline text-base font-bold text-on-surface">
                {position?.speedKmh ?? 0} km/h
              </span>
            </div>
            <div>
              <span className="text-outline block text-[11px]">Precisión Señal:</span>
              <span className="font-bold text-on-surface">±{position?.accuracyMeters ?? 6.0} m</span>
            </div>
          </div>

          <div className="pt-2 border-t border-outline-variant/30 flex justify-between items-center text-xs">
            <span className="text-outline">Última lectura recibida:</span>
            <span className="font-bold text-on-surface">Hace 45 segundos</span>
          </div>
        </div>

        {/* Clinical / Field Notes */}
        <div className="bg-surface-container-low rounded-xl p-3.5 border border-outline-variant/40">
          <span className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
            Cuaderno de Campo & Notas
          </span>
          <p className="text-xs text-on-surface-variant leading-relaxed">
            {animal.notes || 'Animal en buenas condiciones de pastoreo. Vacunas al día (Fiebre Aftosa y Brucelosis).'}
          </p>
        </div>

        {/* Modal Actions */}
        <div className="flex flex-col gap-2 pt-1">
          <button
            onClick={() => {
              onViewOnMap(animal.id);
              onClose();
            }}
            className="w-full min-h-[50px] bg-primary text-white rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-2 active:scale-[0.99] transition-transform font-headline text-base"
            type="button"
          >
            <span className="material-symbols-outlined text-[20px]">pin_drop</span>
            <span>Ver Ubicación en el Mapa</span>
          </button>

          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => onPingCollar(animal.trackerId)}
              className="min-h-[48px] bg-surface-container-high hover:bg-surface-variant text-on-surface rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-1 active:scale-[0.99] transition-transform"
              type="button"
            >
              <span className="material-symbols-outlined text-[18px]">cell_tower</span>
              <span>Ping Collar</span>
            </button>

            <button
              onClick={onClose}
              className="min-h-[48px] bg-surface-container-high hover:bg-surface-variant text-on-surface rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-1"
              type="button"
            >
              <span>Cerrar</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
