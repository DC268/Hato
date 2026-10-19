/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { Animal, Geofence } from '../types/domain';

interface AddAnimalModalProps {
  zones: Geofence[];
  onClose: () => void;
  onAddAnimal: (newAnimal: Partial<Animal>) => void;
}

export const AddAnimalModal: React.FC<AddAnimalModalProps> = ({
  zones,
  onClose,
  onAddAnimal,
}) => {
  const [name, setName] = useState('');
  const [tagNumber, setTagNumber] = useState('#0');
  const [breed, setBreed] = useState('Holstein');
  const [ageYears, setAgeYears] = useState('3');
  const [trackerCode, setTrackerCode] = useState('TRK-');
  const [selectedZoneId, setSelectedZoneId] = useState(zones[0]?.id || 'zone-norte');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    // Extract display number from name or tag
    const numMatch = name.match(/\d+/) || tagNumber.match(/\d+/);
    const displayNumber = numMatch ? numMatch[0].padStart(2, '0') : name.substring(0, 2).toUpperCase();

    onAddAnimal({
      name: name.trim(),
      tagNumber: tagNumber.startsWith('#') ? tagNumber : `#${tagNumber}`,
      displayNumber,
      breed,
      ageYears: parseFloat(ageYears) || 3,
      trackerId: trackerCode.trim() || `TRK-${Math.floor(1000 + Math.random() * 9000)}`,
      currentZoneId: selectedZoneId,
      healthStatus: 'HEALTHY',
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-[#191c18]/60 backdrop-blur-xs flex flex-col justify-end p-0 animate-in fade-in">
      <div className="w-full max-w-lg mx-auto bg-surface-container-lowest rounded-t-[28px] p-5 flex flex-col gap-3 shadow-2xl animate-in slide-in-from-bottom duration-200 border-t border-outline-variant/40 max-h-[90vh] overflow-y-auto">
        <div className="w-12 h-1.5 bg-outline-variant rounded-full mx-auto mb-1"></div>

        <div className="flex items-center justify-between">
          <div>
            <h3 className="font-headline text-2xl uppercase text-on-surface font-bold">
              Registrar Nuevo Animal
            </h3>
            <p className="text-xs text-on-surface-variant">
              Asignar arete oficial y vincular collar GPS
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

        <form onSubmit={handleSubmit} className="flex flex-col gap-3 pt-1">
          <div>
            <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
              Nombre o Identificador
            </label>
            <input
              required
              type="text"
              placeholder="Ej. Vaca 28, Novillo 35"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                Arete / Tag RFID
              </label>
              <input
                required
                type="text"
                placeholder="#0345"
                value={tagNumber}
                onChange={(e) => setTagNumber(e.target.value)}
                className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none"
              />
            </div>

            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                Raza
              </label>
              <select
                value={breed}
                onChange={(e) => setBreed(e.target.value)}
                className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none"
              >
                <option value="Holstein">Holstein</option>
                <option value="Brahman">Brahman</option>
                <option value="Jersey">Jersey</option>
                <option value="Girolando">Girolando</option>
                <option value="Simmental">Simmental</option>
                <option value="Nelore">Nelore</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                Edad (años)
              </label>
              <input
                type="number"
                min="0.5"
                max="15"
                step="0.5"
                value={ageYears}
                onChange={(e) => setAgeYears(e.target.value)}
                className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none"
              />
            </div>

            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                Collar GPS (IMEI / ID)
              </label>
              <input
                type="text"
                placeholder="TRK-0345"
                value={trackerCode}
                onChange={(e) => setTrackerCode(e.target.value)}
                className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none font-mono"
              />
            </div>
          </div>

          <div>
            <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
              Potrero Inicial Asignado
            </label>
            <select
              value={selectedZoneId}
              onChange={(e) => setSelectedZoneId(e.target.value)}
              className="w-full h-11 px-3 bg-surface-container-low border border-outline-variant rounded-xl text-sm text-on-surface focus:outline-none"
            >
              {zones.map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name} ({z.areaHa} ha)
                </option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-2 pt-2">
            <button
              type="submit"
              className="w-full min-h-[50px] bg-primary text-white rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-2 active:scale-[0.99] transition-transform font-headline text-base"
            >
              <span className="material-symbols-outlined text-[20px]">check</span>
              <span>Guardar Animal en el Hato</span>
            </button>

            <button
              type="button"
              onClick={onClose}
              className="w-full min-h-[46px] bg-surface-container-high hover:bg-surface-variant text-on-surface rounded-xl text-xs uppercase tracking-wider font-bold"
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
