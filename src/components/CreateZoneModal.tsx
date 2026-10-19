/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { Geofence, PolygonGeofence, GeoPoint } from '../types/domain';

interface CreateZoneModalProps {
  onClose: () => void;
  onCreateZone: (zone: Geofence) => void;
  onShowToast: (msg: string) => void;
}

export const CreateZoneModal: React.FC<CreateZoneModalProps> = ({
  onClose,
  onCreateZone,
  onShowToast,
}) => {
  const [mode, setMode] = useState<'options' | 'form'>('options');
  const [zoneName, setZoneName] = useState('');
  const [zoneDescription, setZoneDescription] = useState('Lote de pastoreo rotacional');
  const [areaHa, setAreaHa] = useState('15.0');
  const [toleranceMeters, setToleranceMeters] = useState('15');

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!zoneName.trim()) return;

    // Generated boundary points around center
    const newZone = new PolygonGeofence(
      `zone-${Date.now()}`,
      'farm-01',
      zoneName.trim(),
      parseFloat(toleranceMeters) || 15,
      [
        new GeoPoint(10.4660, -84.6450),
        new GeoPoint(10.4665, -84.6410),
        new GeoPoint(10.4630, -84.6415),
        new GeoPoint(10.4625, -84.6455),
      ],
      true,
      zoneDescription,
      parseFloat(areaHa) || 15.0,
      1450
    );

    onCreateZone(newZone);
    onShowToast(`Geocerca "${zoneName}" creada y activada`);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-[#191c18]/60 backdrop-blur-xs flex flex-col justify-end p-0 animate-in fade-in">
      <div className="w-full max-w-lg mx-auto bg-surface-container-lowest rounded-t-[28px] p-5 flex flex-col gap-3.5 shadow-2xl animate-in slide-in-from-bottom duration-200 border-t border-outline-variant/40">
        <div className="w-12 h-1.5 bg-outline-variant rounded-full mx-auto mb-1"></div>

        <div className="flex items-center justify-between">
          <div>
            <h3 className="font-headline text-2xl uppercase text-on-surface font-bold">
              Nueva Geocerca
            </h3>
            <p className="text-xs text-on-surface-variant">
              {mode === 'options'
                ? 'Seleccione el modo de trazado del potrero'
                : 'Defina los parámetros del nuevo potrero'}
            </p>
          </div>
          <button
            onClick={onClose}
            className="w-10 h-10 rounded-full bg-surface-container flex items-center justify-center text-on-surface hover:bg-surface-container-high"
            type="button"
          >
            <span className="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>

        {mode === 'options' ? (
          <div className="grid grid-cols-1 gap-2.5 pt-1">
            <button
              onClick={() => {
                setZoneName('Potrero Nuevo');
                setMode('form');
              }}
              className="p-3.5 rounded-xl bg-surface-container-low hover:bg-primary-fixed-dim/30 text-left flex items-center gap-3.5 active:scale-[0.99] transition-transform border border-outline-variant/40"
              type="button"
            >
              <div className="w-12 h-12 rounded-xl bg-primary text-white flex items-center justify-center shrink-0">
                <span className="material-symbols-outlined text-[26px]">draw</span>
              </div>
              <div>
                <strong className="block text-sm text-on-surface font-headline uppercase font-bold text-base">
                  Dibujar con los dedos en mapa
                </strong>
                <span className="text-xs text-on-surface-variant leading-snug block mt-0.5">
                  Marque los postes perimetrales sobre la imagen satelital
                </span>
              </div>
            </button>

            <button
              onClick={() => {
                setZoneName('Potrero Lindero Este');
                setMode('form');
              }}
              className="p-3.5 rounded-xl bg-surface-container-low hover:bg-primary-fixed-dim/30 text-left flex items-center gap-3.5 active:scale-[0.99] transition-transform border border-outline-variant/40"
              type="button"
            >
              <div className="w-12 h-12 rounded-xl bg-secondary text-white flex items-center justify-center shrink-0">
                <span className="material-symbols-outlined text-[26px]">directions_walk</span>
              </div>
              <div>
                <strong className="block text-sm text-on-surface font-headline uppercase font-bold text-base">
                  Recorrer a caballo / vehículo
                </strong>
                <span className="text-xs text-on-surface-variant leading-snug block mt-0.5">
                  Graba los linderos usando el GPS de su teléfono en tiempo real
                </span>
              </div>
            </button>
          </div>
        ) : (
          <form onSubmit={handleCreate} className="flex flex-col gap-3 pt-1">
            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                Nombre del Potrero
              </label>
              <input
                required
                type="text"
                value={zoneName}
                onChange={(e) => setZoneName(e.target.value)}
                className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                Descripción / Tipo de Pasto
              </label>
              <input
                type="text"
                value={zoneDescription}
                onChange={(e) => setZoneDescription(e.target.value)}
                className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                  Superficie (ha)
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={areaHa}
                  onChange={(e) => setAreaHa(e.target.value)}
                  className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                  Tolerancia GPS (m)
                </label>
                <input
                  type="number"
                  min="5"
                  max="50"
                  value={toleranceMeters}
                  onChange={(e) => setToleranceMeters(e.target.value)}
                  className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none"
                />
              </div>
            </div>

            <div className="flex flex-col gap-2 pt-2">
              <button
                type="submit"
                className="w-full min-h-[50px] bg-primary text-white rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-2 active:scale-[0.99] transition-transform font-headline text-base"
              >
                <span className="material-symbols-outlined text-[20px]">save</span>
                <span>Guardar y Activar Geocerca</span>
              </button>

              <button
                type="button"
                onClick={() => setMode('options')}
                className="w-full min-h-[46px] bg-surface-container-high hover:bg-surface-variant text-on-surface rounded-xl text-xs uppercase tracking-wider font-bold"
              >
                Volver
              </button>
            </div>
          </form>
        )}

        {mode === 'options' && (
          <button
            onClick={onClose}
            className="w-full min-h-[50px] bg-surface-container-high hover:bg-surface-variant text-on-surface rounded-xl text-xs uppercase tracking-wider font-bold font-headline text-base"
            type="button"
          >
            Cancelar
          </button>
        )}
      </div>
    </div>
  );
};
