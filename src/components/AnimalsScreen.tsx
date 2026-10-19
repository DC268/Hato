/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { Animal, Position } from '../types/domain';

interface AnimalsScreenProps {
  animals: Animal[];
  positions: Record<string, Position>;
  onSelectAnimal: (animal: Animal) => void;
  onOpenAnimalDetail: (animal: Animal) => void;
  onOpenAddModal: () => void;
}

export const AnimalsScreen: React.FC<AnimalsScreenProps> = ({
  animals,
  positions,
  onSelectAnimal,
  onOpenAnimalDetail,
  onOpenAddModal,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState<'all' | 'danger' | 'offline' | 'battery'>('all');
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);
  const [selectedBreed, setSelectedBreed] = useState<string>('all');

  const criticalCount = animals.filter((a) => a.healthStatus === 'ALERT').length;
  const offlineCount = animals.filter((a) => a.healthStatus === 'ATTENTION').length;
  const lowBatteryCount = animals.filter((a) => {
    const pos = positions[a.id];
    return (pos?.batteryPercent ?? 100) < 20 || a.id === 'a-03';
  }).length;

  // Filter cattle based on search & chips
  const filteredList = animals.filter((animal) => {
    const matchesSearch =
      animal.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      animal.tagNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      animal.breed.toLowerCase().includes(searchQuery.toLowerCase()) ||
      animal.displayNumber.toLowerCase().includes(searchQuery.toLowerCase());

    if (!matchesSearch) return false;

    if (selectedBreed !== 'all' && !animal.breed.toLowerCase().includes(selectedBreed.toLowerCase())) {
      return false;
    }

    if (filterType === 'danger') {
      return animal.healthStatus === 'ALERT' || animal.id === 'a-03';
    }
    if (filterType === 'offline') {
      return animal.healthStatus === 'ATTENTION' || animal.id === 'a-21';
    }
    if (filterType === 'battery') {
      const pos = positions[animal.id];
      return (pos?.batteryPercent ?? 100) < 20 || animal.id === 'a-03';
    }

    return true;
  });

  const getStatusBadge = (animal: Animal) => {
    if (animal.id === 'a-03' || animal.healthStatus === 'ALERT') {
      return (
        <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-error-container text-on-error-container text-xs font-bold">
          <span className="material-symbols-outlined text-[17px] text-error">warning</span>
          <span>Fuera de la zona</span>
        </div>
      );
    }
    if (animal.id === 'a-21') {
      return (
        <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-surface-container-highest text-on-surface-variant text-xs font-bold">
          <span className="material-symbols-outlined text-[17px]">signal_disconnected</span>
          <span>Sin señal desde las 14:20</span>
        </div>
      );
    }
    if (animal.id === 'a-07') {
      return (
        <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-secondary-fixed-dim/40 text-secondary text-xs font-bold">
          <span className="material-symbols-outlined text-[17px]">hourglass_top</span>
          <span>Quieto hace 6 h</span>
        </div>
      );
    }
    // Normal dentro
    const zoneName = animal.currentZoneId === 'zone-norte' ? 'Potrero Norte' : 'Potrero Sur';
    return (
      <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-primary-fixed/40 text-primary text-xs font-bold">
        <span className="material-symbols-outlined text-[17px]">check_circle</span>
        <span>Dentro ({zoneName})</span>
      </div>
    );
  };

  const getBatteryBadge = (animal: Animal) => {
    const pos = positions[animal.id];
    let percent = pos?.batteryPercent ?? 85;
    if (animal.id === 'a-03') percent = 15;
    if (animal.id === 'a-21') percent = 64;
    if (animal.id === 'a-12') percent = 92;
    if (animal.id === 'a-tb') percent = 85;
    if (animal.id === 'a-07') percent = 78;

    if (percent < 20) {
      return (
        <div className="flex items-center gap-2 text-tertiary text-xs bg-tertiary-fixed/30 px-2.5 py-1 rounded-lg border border-tertiary/20 font-bold">
          <span className="material-symbols-outlined text-[17px] text-tertiary">battery_alert</span>
          <span>{percent}%</span>
        </div>
      );
    }

    return (
      <div className="flex items-center gap-1.5 text-on-surface-variant text-xs bg-surface-container px-2.5 py-1 rounded-lg font-bold">
        <span className="material-symbols-outlined text-[17px] text-primary">battery_full</span>
        <span>{percent}%</span>
      </div>
    );
  };

  const getSubtext = (animal: Animal) => {
    if (animal.id === 'a-03') return 'Última posición: Hace 4 min';
    if (animal.id === 'a-21') return 'Collar GPS V3';
    if (animal.id === 'a-12') return 'Última posición: Hace 2 min';
    if (animal.id === 'a-tb') return 'Raza Brahman Reproductivo';
    if (animal.id === 'a-07') return 'Bebedero Central';
    return `${animal.breed} · Lote Activo`;
  };

  const getSubtextIcon = (animal: Animal) => {
    if (animal.id === 'a-21') return 'cell_tower';
    if (animal.id === 'a-tb') return 'pets';
    if (animal.id === 'a-07') return 'location_on';
    return 'schedule';
  };

  return (
    <div className="flex flex-col w-full pb-32">
      {/* Status & Context Overview Card */}
      <div className="px-4 pt-3 pb-1">
        <div className="bg-surface-container-high rounded-xl p-4 shadow-xs border border-outline-variant/40">
          <div className="flex items-center justify-between">
            <div>
              <span className="font-headline text-3xl uppercase tracking-tight text-primary block leading-none font-bold">
                Animales
              </span>
              <span className="text-xs text-on-surface-variant font-bold mt-1.5 inline-flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-primary inline-block"></span>
                {animals.length} animales registrados
              </span>
            </div>

            <div className="flex items-center gap-1.5 bg-surface-container-lowest px-3 py-1.5 rounded-full shadow-xs border border-outline-variant/40">
              <span className="material-symbols-outlined text-[18px] text-error">warning</span>
              <span className="text-xs text-on-surface font-bold">{criticalCount} crítico</span>
            </div>
          </div>
        </div>
      </div>

      {/* Search & Action Row */}
      <div className="px-4 py-2">
        <div className="relative flex items-center gap-2">
          <div className="relative flex-1">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
              <span className="material-symbols-outlined text-outline text-[22px]">search</span>
            </div>
            <input
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full min-h-[52px] pl-11 pr-10 bg-surface-container-lowest rounded-xl text-sm text-on-surface placeholder:text-outline focus:outline-none focus:ring-2 focus:ring-primary shadow-xs border border-outline-variant/50"
              placeholder="Buscar por número, arete o nombre..."
              type="text"
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                aria-label="Limpiar búsqueda"
                className="absolute inset-y-0 right-0 pr-3 flex items-center justify-center min-w-[44px] text-outline active:text-on-surface"
              >
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            )}
          </div>

          <button
            onClick={() => setShowAdvancedFilters(!showAdvancedFilters)}
            aria-label="Filtros avanzados"
            className={`min-h-[52px] min-w-[52px] rounded-xl flex items-center justify-center shadow-xs transition-colors border border-outline-variant/50 ${
              showAdvancedFilters || selectedBreed !== 'all'
                ? 'bg-primary text-white'
                : 'bg-surface-container-lowest active:bg-surface-container text-primary'
            }`}
            title="Filtros por raza"
          >
            <span className="material-symbols-outlined text-[24px]">tune</span>
          </button>
        </div>

        {/* Breed Filter Drawer if toggled */}
        {showAdvancedFilters && (
          <div className="mt-2 p-3 bg-surface-container rounded-xl border border-outline-variant/40 animate-in fade-in">
            <span className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1.5">
              Filtrar por Raza
            </span>
            <div className="flex flex-wrap gap-1.5">
              {['all', 'Holstein', 'Brahman', 'Girolando', 'Jersey'].map((b) => (
                <button
                  key={b}
                  onClick={() => setSelectedBreed(b)}
                  className={`px-3 py-1 rounded-full text-xs font-bold transition-colors ${
                    selectedBreed === b
                      ? 'bg-primary text-white'
                      : 'bg-surface-container-lowest text-on-surface-variant hover:bg-surface-container-high'
                  }`}
                >
                  {b === 'all' ? 'Todas las razas' : b}
                </button>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Horizontal Filter Chips */}
      <div className="py-1">
        <div className="flex items-center gap-2 overflow-x-auto px-4 no-scrollbar">
          {/* Todos */}
          <button
            onClick={() => setFilterType('all')}
            className={`flex-shrink-0 h-10 px-4 rounded-full flex items-center gap-1.5 text-xs font-bold shadow-xs transition-all ${
              filterType === 'all'
                ? 'bg-primary-fixed text-on-primary-fixed border border-primary/30'
                : 'bg-surface-container-lowest text-on-surface-variant border border-outline-variant/40'
            }`}
            type="button"
          >
            {filterType === 'all' && (
              <span className="material-symbols-outlined text-[18px]">check</span>
            )}
            <span>Todos ({animals.length})</span>
          </button>

          {/* Warning / Fuera */}
          <button
            onClick={() => setFilterType('danger')}
            className={`flex-shrink-0 h-10 px-3.5 rounded-full flex items-center gap-1.5 text-xs font-bold shadow-xs transition-all ${
              filterType === 'danger'
                ? 'bg-error-container text-error border border-error/40'
                : 'bg-surface-container-lowest text-error border border-outline-variant/40'
            }`}
            type="button"
          >
            <span className="material-symbols-outlined text-[18px]">warning</span>
            <span>Fuera ({criticalCount})</span>
          </button>

          {/* Sin señal */}
          <button
            onClick={() => setFilterType('offline')}
            className={`flex-shrink-0 h-10 px-3.5 rounded-full flex items-center gap-1.5 text-xs font-bold shadow-xs transition-all ${
              filterType === 'offline'
                ? 'bg-surface-container-highest text-on-surface font-bold border border-outline'
                : 'bg-surface-container-lowest text-on-surface-variant border border-outline-variant/40'
            }`}
            type="button"
          >
            <span className="material-symbols-outlined text-[18px]">signal_disconnected</span>
            <span>Sin señal ({offlineCount})</span>
          </button>

          {/* Batería baja */}
          <button
            onClick={() => setFilterType('battery')}
            className={`flex-shrink-0 h-10 px-3.5 rounded-full flex items-center gap-1.5 text-xs font-bold shadow-xs transition-all ${
              filterType === 'battery'
                ? 'bg-tertiary-fixed text-on-tertiary-fixed border border-tertiary/30'
                : 'bg-surface-container-lowest text-tertiary border border-outline-variant/40'
            }`}
            type="button"
          >
            <span className="material-symbols-outlined text-[18px]">battery_alert</span>
            <span>Batería baja ({lowBatteryCount})</span>
          </button>
        </div>
      </div>

      {/* Livestock List */}
      <div className="px-4 flex flex-col gap-3 mt-2">
        {filteredList.length === 0 ? (
          <div className="px-4 py-12 flex flex-col items-center justify-center text-center bg-surface-container-lowest rounded-2xl border border-outline-variant/40">
            <div className="w-16 h-16 rounded-full bg-surface-container-high flex items-center justify-center text-outline mb-3">
              <span className="material-symbols-outlined text-[32px]">search_off</span>
            </div>
            <p className="font-headline text-xl text-on-surface font-bold">
              No se encontraron animales
            </p>
            <p className="text-xs text-on-surface-variant mt-1">
              Revisa el arete o limpia los filtros rápidos.
            </p>
          </div>
        ) : (
          filteredList.map((animal) => {
            const isCritical = animal.id === 'a-03' || animal.healthStatus === 'ALERT';
            const isOffline = animal.id === 'a-21';
            const isResting = animal.id === 'a-07';

            let avatarBg = 'bg-primary-fixed text-on-primary-fixed';
            if (isCritical) avatarBg = 'bg-error-container text-on-error-container';
            if (isOffline) avatarBg = 'bg-surface-container-high text-on-surface-variant';
            if (isResting) avatarBg = 'bg-secondary-fixed text-on-secondary-fixed';

            return (
              <article
                key={animal.id}
                onClick={() => {
                  onSelectAnimal(animal);
                  onOpenAnimalDetail(animal);
                }}
                className="bg-surface-container-lowest rounded-xl p-4 shadow-xs flex flex-col gap-3 active:scale-[0.99] transition-transform cursor-pointer border border-outline-variant/50 hover:border-primary/50"
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-12 h-12 rounded-xl flex items-center justify-center font-headline text-2xl font-bold shrink-0 ${avatarBg}`}
                    >
                      {animal.displayNumber}
                    </div>

                    <div>
                      <div className="flex items-baseline gap-2">
                        <h2 className="font-headline text-2xl text-on-surface leading-tight font-bold">
                          {animal.name}
                        </h2>
                        <span className="text-[11px] bg-surface-container-high px-2 py-0.5 rounded text-on-surface-variant uppercase font-bold">
                          {animal.tagNumber}
                        </span>
                      </div>
                      <p className="text-xs text-on-surface-variant flex items-center gap-1 mt-0.5">
                        <span className="material-symbols-outlined text-[14px]">
                          {getSubtextIcon(animal)}
                        </span>
                        <span>{getSubtext(animal)}</span>
                      </p>
                    </div>
                  </div>

                  <button
                    aria-label={`Ver detalle de ${animal.name}`}
                    className="w-10 h-10 rounded-full bg-surface-container flex items-center justify-center text-on-surface-variant shrink-0 hover:bg-surface-container-high"
                    type="button"
                  >
                    <span className="material-symbols-outlined text-[22px]">chevron_right</span>
                  </button>
                </div>

                {/* State Pill & Telemetry Grid */}
                <div className="flex flex-wrap items-center justify-between gap-2 pt-1">
                  {getStatusBadge(animal)}
                  {getBatteryBadge(animal)}
                </div>
              </article>
            );
          })
        )}
      </div>

      {/* Floating Action Button (FAB) */}
      <div className="fixed bottom-24 right-4 z-40">
        <button
          onClick={onOpenAddModal}
          aria-label="Registrar nuevo animal en el hato"
          className="h-[52px] px-5 rounded-full bg-primary text-white flex items-center gap-2 shadow-lg active:bg-primary-container active:scale-95 transition-transform font-headline text-lg uppercase tracking-wider font-bold"
          type="button"
        >
          <span className="material-symbols-outlined text-[24px]">add</span>
          <span>Agregar animal</span>
        </button>
      </div>
    </div>
  );
};
