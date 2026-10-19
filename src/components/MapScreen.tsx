/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { Animal, Farm, Geofence, Position } from '../types/domain';

interface MapScreenProps {
  currentFarm: Farm;
  farms: Farm[];
  onSelectFarm: (farm: Farm) => void;
  animals: Animal[];
  positions: Record<string, Position>;
  selectedAnimal: Animal | null;
  onSelectAnimal: (animal: Animal) => void;
  onOpenAnimalDetail: (animal: Animal) => void;
  onOpenScanner: () => void;
}

export const MapScreen: React.FC<MapScreenProps> = ({
  currentFarm,
  farms,
  onSelectFarm,
  animals,
  positions,
  selectedAnimal,
  onSelectAnimal,
  onOpenAnimalDetail,
  onOpenScanner,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [showFarmDropdown, setShowFarmDropdown] = useState(false);
  const [mapLayer, setMapLayer] = useState<'satelital' | 'topografico' | 'hibrido'>('satelital');
  const [showUserLocation, setShowUserLocation] = useState(false);
  const [showTrail, setShowTrail] = useState(false);
  const [showRouteGuidance, setShowRouteGuidance] = useState(false);
  const [isLiveSync, setIsLiveSync] = useState(true);

  // Fallback to Vaca 03 if no animal selected
  const activeAnimal = selectedAnimal || animals.find((a) => a.id === 'a-03') || animals[0];
  const activePosition = positions[activeAnimal.id];

  // Counters
  const outCount = animals.filter((a) => a.healthStatus === 'ALERT').length;
  const offlineCount = animals.filter((a) => a.healthStatus === 'ATTENTION').length;
  const insideCount = animals.length - outCount - offlineCount;

  // Filter animals matching search
  const filteredAnimals = animals.filter(
    (a) =>
      a.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      a.tagNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      a.trackerId.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleMarkerClick = (animal: Animal) => {
    onSelectAnimal(animal);
    if (typeof navigator !== 'undefined' && navigator.vibrate) {
      navigator.vibrate(30);
    }
  };

  return (
    <div className="flex flex-col w-full relative">
      {/* Top Utility Context Bar */}
      <div className="px-4 pt-2 pb-2.5 flex flex-col gap-2 bg-surface shadow-xs z-30">
        <div className="flex items-center justify-between gap-2 relative">
          {/* Farm Selector */}
          <div className="relative">
            <button
              onClick={() => setShowFarmDropdown(!showFarmDropdown)}
              className="flex items-center gap-1.5 min-h-[48px] px-3 rounded-xl bg-surface-container active:bg-surface-container-high transition-colors focus:outline-none"
              type="button"
            >
              <span className="material-symbols-outlined text-primary text-[22px]">agriculture</span>
              <div className="flex items-baseline gap-1">
                <span className="font-headline text-lg uppercase tracking-tight font-bold text-on-surface">
                  {currentFarm.name}
                </span>
                <span className="material-symbols-outlined text-on-surface-variant text-[18px]">
                  expand_more
                </span>
              </div>
            </button>

            {/* Farm Dropdown */}
            {showFarmDropdown && (
              <div className="absolute top-full left-0 mt-1 w-64 bg-surface-container-lowest rounded-xl shadow-xl border border-outline-variant p-2 z-50">
                <span className="text-[11px] font-bold uppercase tracking-wider text-outline px-2 py-1 block">
                  Cambiar Finca
                </span>
                {farms.map((f) => (
                  <button
                    key={f.id}
                    onClick={() => {
                      onSelectFarm(f);
                      setShowFarmDropdown(false);
                    }}
                    className={`w-full text-left px-3 py-2.5 rounded-lg flex items-center justify-between transition-colors ${
                      f.id === currentFarm.id
                        ? 'bg-primary-fixed text-on-primary-fixed font-bold'
                        : 'hover:bg-surface-container text-on-surface'
                    }`}
                  >
                    <div>
                      <p className="font-title-md text-sm">{f.name}</p>
                      <p className="text-xs text-on-surface-variant">{f.location}</p>
                    </div>
                    {f.id === currentFarm.id && (
                      <span className="material-symbols-outlined text-[18px]">check</span>
                    )}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Sync Live Badge */}
          <button
            onClick={() => setIsLiveSync(!isLiveSync)}
            className={`flex items-center gap-1.5 px-3 py-1 rounded-full font-label-sm text-xs tracking-wide transition-colors ${
              isLiveSync
                ? 'bg-primary-fixed text-on-primary-fixed'
                : 'bg-surface-container-high text-on-surface-variant'
            }`}
            title="Pausar o reanudar telemetría en vivo"
          >
            <span
              className={`w-2 h-2 rounded-full ${
                isLiveSync ? 'bg-primary animate-pulse' : 'bg-outline'
              }`}
            ></span>
            <span className="uppercase font-bold">
              {isLiveSync ? 'En Vivo • 1m' : 'Pausado'}
            </span>
          </button>
        </div>

        {/* Quick Search Bar */}
        <div className="relative w-full">
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-on-surface-variant">
            <span className="material-symbols-outlined text-[20px]">search</span>
          </div>
          <input
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full h-12 pl-11 pr-11 bg-surface-container-lowest text-on-surface rounded-xl placeholder:text-outline focus:outline-none focus:ring-2 focus:ring-primary shadow-xs border border-outline-variant/40"
            placeholder="Buscar animal por arete o collar..."
            type="text"
          />
          <button
            onClick={onOpenScanner}
            aria-label="Escanear arete RFID o QR"
            className="absolute inset-y-0 right-0 w-12 h-12 flex items-center justify-center text-on-surface-variant hover:text-primary active:scale-95 transition-transform"
            type="button"
            title="Escanear arete RFID / QR en campo"
          >
            <span className="material-symbols-outlined text-[22px]">qr_code_scanner</span>
          </button>
        </div>

        {/* Search Results Dropdown if searching */}
        {searchQuery.trim().length > 0 && (
          <div className="bg-surface-container-lowest rounded-xl p-2 shadow-lg border border-outline-variant max-h-48 overflow-y-auto">
            {filteredAnimals.length === 0 ? (
              <p className="text-xs text-outline p-2 text-center">No se encontraron animales</p>
            ) : (
              filteredAnimals.map((animal) => (
                <button
                  key={animal.id}
                  onClick={() => {
                    onSelectAnimal(animal);
                    setSearchQuery('');
                  }}
                  className="w-full flex items-center justify-between p-2 rounded-lg hover:bg-surface-container text-left"
                >
                  <div className="flex items-center gap-2">
                    <span className="w-7 h-7 rounded-md bg-primary-fixed text-primary font-bold flex items-center justify-center text-xs">
                      {animal.displayNumber}
                    </span>
                    <div>
                      <span className="font-bold text-xs">{animal.name}</span>
                      <span className="text-[11px] text-outline ml-1">{animal.tagNumber}</span>
                    </div>
                  </div>
                  <span className="text-xs uppercase font-bold text-primary">Ver</span>
                </button>
              ))
            )}
          </div>
        )}
      </div>

      {/* Interactive Map Viewport */}
      <div className="relative w-full h-[520px] overflow-hidden bg-[#2e312d] select-none" id="field-map-container">
        {/* Map Background Image */}
        <div
          className={`absolute inset-0 bg-cover bg-center w-full h-full transform scale-105 transition-all duration-300 ${
            mapLayer === 'topografico' ? 'contrast-125 saturate-50 brightness-110' : ''
          }`}
          style={{
            backgroundImage: `url('https://lh3.googleusercontent.com/aida-public/AB6AXuD5JYl9IiZSvRm_zNszFURbAvJiI_Ex54QL6HVG18cxm-Y3FWcgErBGZcVXB-1dYm-eXcFDMNVzguCJvef0PX_FuHXtBzC6CDFXM-cFshsKdz3TvXIaEghx_KSZHW0VsEZQyKEnWny6hdvcyZxOh1ZolkwnbHIJZsCLjAPfiNeLYfBIdMtECi5A68y_sjwdEEz2doYNp8F5anV3tQwtRueZufc8EmF4hguN3ZZ505vfYxRxGHL2zmEO')`,
          }}
        ></div>

        {/* Topographic Grid Overlay if selected */}
        {mapLayer === 'topografico' && (
          <div className="absolute inset-0 bg-[radial-gradient(#156589_1px,transparent_1px)] [background-size:24px_24px] opacity-40 pointer-events-none"></div>
        )}

        {/* SVG Geofence Polygons & Grazing Zones */}
        <svg
          className="absolute inset-0 w-full h-full pointer-events-none"
          preserveAspectRatio="none"
          viewBox="0 0 400 520"
        >
          <defs>
            <filter id="glow-danger" x="-20%" y="-20%" width="140%" height="140%">
              <feDropShadow dx="0" dy="2" stdDeviation="4" floodColor="#ba1a1a" floodOpacity="0.6" />
            </filter>
          </defs>

          {/* Potrero Norte (Zone of breach) */}
          <polygon fill="#2e6b34" fillOpacity="0.16" points="25,30 195,30 185,190 20,180" />
          <polyline
            fill="none"
            points="25,30 195,30 185,190 20,180 25,30"
            stroke="#12521e"
            strokeDasharray="5,4"
            strokeWidth="2.5"
          />

          {/* Potrero Sur (Active safe herd) */}
          <polygon fill="#2e6b34" fillOpacity="0.14" points="205,80 380,80 375,340 195,310" />
          <polyline
            fill="none"
            points="205,80 380,80 375,340 195,310 205,80"
            stroke="#12521e"
            strokeWidth="2.5"
          />

          {/* Forbidden / Ravine Buffer Zone (Riesgo Zanja) */}
          <polygon fill="#ba1a1a" fillOpacity="0.14" points="20,210 170,220 160,320 15,310" />
          <polyline
            fill="none"
            points="20,210 170,220 160,320 15,310 20,210"
            stroke="#ba1a1a"
            strokeDasharray="4,4"
            strokeWidth="2"
          />

          {/* Breach Vector line from pasture to Vaca 03 */}
          <line
            stroke="#ba1a1a"
            strokeDasharray="4,4"
            strokeWidth="2.5"
            x1="175"
            x2="115"
            y1="185"
            y2="120"
          />

          {/* Historical Breadcrumb Trail when toggled */}
          {showTrail && (
            <g className="animate-in fade-in duration-300">
              <polyline
                fill="none"
                points="180,180 160,170 145,150 130,135 115,120"
                stroke="#ffddb5"
                strokeWidth="3.5"
                strokeLinecap="round"
                strokeDasharray="2,3"
              />
              <circle cx="180" cy="180" r="4" fill="#5d421c" stroke="#ffffff" strokeWidth="1.5" />
              <circle cx="160" cy="170" r="3" fill="#ffddb5" />
              <circle cx="145" cy="150" r="3" fill="#ffddb5" />
              <circle cx="130" cy="135" r="3" fill="#ffddb5" />
            </g>
          )}

          {/* Route guidance from rancher position to escaped cow */}
          {showRouteGuidance && (
            <g className="animate-in fade-in duration-300">
              <path
                d="M 320,440 L 260,380 L 190,260 L 140,160 L 115,120"
                fill="none"
                stroke="#156589"
                strokeWidth="4"
                strokeLinecap="round"
                strokeDasharray="6,4"
              />
              <circle cx="320" cy="440" r="6" fill="#156589" stroke="#ffffff" strokeWidth="2" />
            </g>
          )}
        </svg>

        {/* Zone Labels on Map */}
        <div className="absolute top-8 left-7 px-2.5 py-1 rounded bg-[#f8faf3]/95 text-primary font-headline text-xs uppercase tracking-wider font-bold shadow-sm pointer-events-none border border-outline-variant/50">
          Potrero Norte
        </div>
        <div className="absolute top-20 right-8 px-2.5 py-1 rounded bg-[#f8faf3]/95 text-primary font-headline text-xs uppercase tracking-wider font-bold shadow-sm pointer-events-none border border-outline-variant/50">
          Potrero Sur (Hato Principal)
        </div>
        <div className="absolute top-[215px] left-5 px-2 py-0.5 rounded bg-[#ba1a1a]/90 text-white font-headline text-[11px] uppercase tracking-wider font-bold shadow-sm pointer-events-none">
          Riesgo Zanja
        </div>

        {/* Animal Marker: Vaca 07 (Safe Inside) */}
        <button
          onClick={() => {
            const a = animals.find((i) => i.displayNumber === '07' || i.name.includes('07')) || animals[4];
            handleMarkerClick(a);
          }}
          aria-label="Vaca 07 Dentro"
          className="absolute top-[160px] right-[110px] flex items-center justify-center w-11 h-11 rounded-full bg-primary-container text-white font-headline text-2xl font-bold shadow-md active:scale-95 transition-transform"
          style={{ boxShadow: '0 0 0 3px #ffffff, 0 4px 10px rgba(0,0,0,0.35)' }}
          type="button"
        >
          07
        </button>

        {/* Animal Marker: Vaca 12 (Safe Inside) */}
        <button
          onClick={() => {
            const a = animals.find((i) => i.displayNumber === '12') || animals[2];
            handleMarkerClick(a);
          }}
          aria-label="Vaca 12 Dentro"
          className="absolute top-[230px] right-[130px] flex items-center justify-center w-11 h-11 rounded-full bg-primary-container text-white font-headline text-2xl font-bold shadow-md active:scale-95 transition-transform"
          style={{ boxShadow: '0 0 0 3px #ffffff, 0 4px 10px rgba(0,0,0,0.35)' }}
          type="button"
        >
          12
        </button>

        {/* Animal Marker: Novillo 21 (Sin Señal - Offline Gray) */}
        <button
          onClick={() => {
            const a = animals.find((i) => i.displayNumber === '21') || animals[1];
            handleMarkerClick(a);
          }}
          aria-label="Novillo 21 Sin señal"
          className="absolute bottom-[240px] left-[55px] flex items-center justify-center w-11 h-11 rounded-full bg-outline text-white font-headline text-2xl font-bold shadow-md active:scale-95 transition-transform"
          style={{ boxShadow: '0 0 0 3px #ffffff, 0 4px 8px rgba(0,0,0,0.25)' }}
          type="button"
        >
          <span className="relative z-10">21</span>
          <span className="absolute w-8 h-[2px] bg-error rounded rotate-45 pointer-events-none"></span>
        </button>

        {/* CRITICAL ALERT MARKER: Vaca 03 (Escaped) */}
        <div className="absolute top-[85px] left-[85px] z-20 flex flex-col items-center">
          {/* Radar pulse effect */}
          <span className="absolute -top-1 w-14 h-14 rounded-full bg-error opacity-40 animate-ping pointer-events-none"></span>

          {/* Triangular Pin */}
          <button
            onClick={() => {
              const a = animals.find((i) => i.displayNumber === '03') || animals[0];
              handleMarkerClick(a);
            }}
            aria-label="Alerta Crítica: Vaca 03 Fuera de zona"
            className="relative flex flex-col items-center justify-center w-13 h-13 px-2 py-1 rounded-xl bg-error text-white shadow-xl active:scale-90 transition-transform"
            style={{ boxShadow: '0 0 0 3px #ffffff, 0 4px 12px rgba(186,26,26,0.6)' }}
            type="button"
          >
            <div className="flex items-center gap-1 font-headline text-2xl tracking-tight font-bold leading-tight">
              <span className="material-symbols-outlined text-[18px] text-white animate-bounce">
                warning
              </span>
              <span>03</span>
            </div>
            <span className="font-headline text-[10px] uppercase font-bold leading-none tracking-widest text-white">
              FUERA
            </span>
          </button>
          <div className="w-0 h-0 border-l-[6px] border-l-transparent border-r-[6px] border-r-transparent border-t-[8px] border-t-error"></div>
        </div>

        {/* Rancher GPS marker when user location is toggled */}
        {showUserLocation && (
          <div className="absolute bottom-16 right-24 z-20 flex flex-col items-center">
            <span className="w-10 h-10 rounded-full bg-secondary/30 animate-ping absolute"></span>
            <div className="w-8 h-8 rounded-full bg-secondary text-white flex items-center justify-center shadow-lg border-2 border-white">
              <span className="material-symbols-outlined text-[18px]">directions_walk</span>
            </div>
            <span className="px-2 py-0.5 rounded bg-surface/90 text-[10px] font-bold text-secondary uppercase shadow-xs mt-1">
              Usted
            </span>
          </div>
        )}

        {/* Floating Map Controls (Thumb Ergonomics - Right Column) */}
        <div className="absolute right-4 bottom-24 z-20 flex flex-col gap-2">
          {/* Zoom fit / Ver todo el hato */}
          <button
            onClick={() => {
              setShowRouteGuidance(false);
              setShowTrail(false);
            }}
            aria-label="Ver todo el hato"
            className="w-12 h-12 flex items-center justify-center rounded-xl bg-surface-container-lowest text-on-surface shadow-md active:bg-surface-container transition-colors border border-outline-variant/40"
            title="Ver todo el hato"
            type="button"
          >
            <span className="material-symbols-outlined text-[26px]">crop_free</span>
          </button>

          {/* Mi ubicación (GPS) */}
          <button
            onClick={() => setShowUserLocation(!showUserLocation)}
            aria-label="Mi ubicación"
            className={`w-12 h-12 flex items-center justify-center rounded-xl shadow-md transition-colors border border-outline-variant/40 ${
              showUserLocation
                ? 'bg-secondary text-white'
                : 'bg-surface-container-lowest text-secondary active:bg-surface-container'
            }`}
            title="Mi ubicación actual"
            type="button"
          >
            <span className="material-symbols-outlined text-[26px]">my_location</span>
          </button>

          {/* Capas de mapa */}
          <button
            onClick={() =>
              setMapLayer(
                mapLayer === 'satelital'
                  ? 'topografico'
                  : mapLayer === 'topografico'
                  ? 'hibrido'
                  : 'satelital'
              )
            }
            aria-label="Capas del mapa"
            className="w-12 h-12 flex items-center justify-center rounded-xl bg-surface-container-lowest text-on-surface-variant shadow-md active:bg-surface-container transition-colors border border-outline-variant/40"
            title={`Capa actual: ${mapLayer}`}
            type="button"
          >
            <span className="material-symbols-outlined text-[26px]">layers</span>
          </button>
        </div>

        {/* Map Scale Indicator */}
        <div className="absolute left-4 bottom-24 z-10 px-2.5 py-1 rounded-md bg-[#f8faf3]/90 text-on-surface font-headline text-xs shadow-xs flex items-center gap-1 pointer-events-none border border-outline-variant/40">
          <span className="material-symbols-outlined text-[14px]">straighten</span>
          <span className="font-bold">Escala: 200m</span>
        </div>
      </div>

      {/* Emergent Tactical Bottom Sheet */}
      <div className="relative -mt-20 z-30 flex flex-col w-full bg-surface-container-lowest rounded-t-[28px] shadow-2xl pb-6 border-t border-outline-variant/40">
        {/* Drag Handle Bar for Field Gloves */}
        <div className="w-full flex items-center justify-center pt-3 pb-2 cursor-grab">
          <div className="w-14 h-1.5 rounded-full bg-outline-variant"></div>
        </div>

        {/* Hato Health & Status Metric Strip */}
        <div className="px-4 pt-1 pb-2">
          <div className="flex items-center justify-between mb-2">
            <h2 className="font-headline text-xl text-on-surface uppercase tracking-normal font-bold">
              Resumen de Animales
            </h2>
            <span className="font-headline text-sm text-outline font-bold">
              Total: {animals.length} cabezas
            </span>
          </div>

          {/* 3 Key High-Visibility Counters */}
          <div className="grid grid-cols-3 gap-2">
            {/* Dentro */}
            <div className="flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-primary-fixed/30 text-on-surface border border-primary/20">
              <div className="flex items-center gap-1 text-primary">
                <span className="material-symbols-outlined text-[18px]">check_circle</span>
                <span className="font-headline text-3xl font-bold leading-none text-primary">
                  {insideCount}
                </span>
              </div>
              <span className="text-[11px] font-bold uppercase tracking-wide text-on-surface-variant mt-0.5">
                Dentro
              </span>
            </div>

            {/* Fuera (Alerta) */}
            <div className="flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-error-container text-on-error-container border border-error/30">
              <div className="flex items-center gap-1 text-error">
                <span className="material-symbols-outlined text-[18px]">warning</span>
                <span className="font-headline text-3xl font-bold leading-none text-error">
                  {outCount}
                </span>
              </div>
              <span className="text-[11px] font-bold uppercase tracking-wide text-error mt-0.5">
                Fuera
              </span>
            </div>

            {/* Sin Señal */}
            <div className="flex flex-col items-center justify-center py-2 px-1 rounded-xl bg-surface-container text-on-surface-variant border border-outline-variant/40">
              <div className="flex items-center gap-1 text-outline">
                <span className="material-symbols-outlined text-[18px]">
                  signal_cellular_connected_no_internet_0_bar
                </span>
                <span className="font-headline text-3xl font-bold leading-none text-on-surface">
                  {offlineCount}
                </span>
              </div>
              <span className="text-[11px] font-bold uppercase tracking-wide text-on-surface-variant mt-0.5">
                Sin Señal
              </span>
            </div>
          </div>
        </div>

        {/* Selected Animal Card (Vaca 03 default or selected) */}
        <div className="px-4 mt-1">
          <div className="p-3.5 rounded-2xl bg-surface-container-low border border-outline-variant/60 shadow-xs">
            {/* Header: Identification + Critical Pill */}
            <div className="flex items-start justify-between gap-2 mb-3">
              <div className="flex items-center gap-3">
                <div
                  className={`w-12 h-12 rounded-xl flex items-center justify-center font-headline text-2xl font-bold shadow-xs text-white ${
                    activeAnimal.healthStatus === 'ALERT'
                      ? 'bg-error'
                      : activeAnimal.healthStatus === 'ATTENTION'
                      ? 'bg-outline'
                      : 'bg-primary'
                  }`}
                >
                  {activeAnimal.displayNumber}
                </div>
                <div>
                  <div className="flex items-center gap-1.5">
                    <h3 className="font-headline text-xl text-on-surface uppercase tracking-tight font-bold">
                      {activeAnimal.name} • {activeAnimal.breed}
                    </h3>
                  </div>
                  <p
                    className={`text-xs font-bold flex items-center gap-1 mt-0.5 ${
                      activeAnimal.healthStatus === 'ALERT'
                        ? 'text-error'
                        : activeAnimal.healthStatus === 'ATTENTION'
                        ? 'text-tertiary'
                        : 'text-primary'
                    }`}
                  >
                    <span className="material-symbols-outlined text-[15px]">
                      {activeAnimal.healthStatus === 'ALERT'
                        ? 'report_problem'
                        : activeAnimal.healthStatus === 'ATTENTION'
                        ? 'schedule'
                        : 'check_circle'}
                    </span>
                    {activeAnimal.healthStatus === 'ALERT'
                      ? 'A 140 m del Potrero Norte (Riesgo Zanja)'
                      : activeAnimal.healthStatus === 'ATTENTION'
                      ? 'Sin desplazamientos recientes (Atención)'
                      : 'Pastando dentro del lindero asignado'}
                  </p>
                </div>
              </div>

              <span
                className={`px-2.5 py-1 rounded-full text-[11px] uppercase tracking-wider font-bold shrink-0 ${
                  activeAnimal.healthStatus === 'ALERT'
                    ? 'bg-error-container text-error'
                    : activeAnimal.healthStatus === 'ATTENTION'
                    ? 'bg-tertiary-fixed text-on-tertiary-fixed'
                    : 'bg-primary-fixed text-primary'
                }`}
              >
                {activeAnimal.healthStatus === 'ALERT'
                  ? 'Fuga Activa'
                  : activeAnimal.healthStatus === 'ATTENTION'
                  ? 'En Observación'
                  : 'Dentro'}
              </span>
            </div>

            {/* Realtime Telemetry Grid */}
            <div className="grid grid-cols-3 gap-2 py-2 px-3 mb-3 bg-surface-container-lowest rounded-xl border border-outline-variant/40">
              <div className="flex flex-col">
                <span className="text-[11px] uppercase tracking-wider text-outline font-bold">
                  Velocidad
                </span>
                <div className="flex items-center gap-1 text-on-surface mt-0.5">
                  <span className="material-symbols-outlined text-secondary text-[16px]">speed</span>
                  <span className="font-headline text-base font-bold">
                    {activePosition?.speedKmh ?? 0} km/h
                  </span>
                </div>
              </div>

              <div className="flex flex-col">
                <span className="text-[11px] uppercase tracking-wider text-outline font-bold">
                  Collar Batería
                </span>
                <div className="flex items-center gap-1 text-on-surface mt-0.5">
                  <span
                    className={`material-symbols-outlined text-[16px] ${
                      (activePosition?.batteryPercent ?? 80) < 20
                        ? 'text-error'
                        : 'text-primary'
                    }`}
                  >
                    battery_5_bar
                  </span>
                  <span className="font-headline text-base font-bold">
                    {activePosition?.batteryPercent ?? 78}%
                  </span>
                </div>
              </div>

              <div className="flex flex-col">
                <span className="text-[11px] uppercase tracking-wider text-outline font-bold">
                  Último reporte
                </span>
                <div className="flex items-center gap-1 text-on-surface mt-0.5">
                  <span className="material-symbols-outlined text-outline text-[16px]">
                    schedule
                  </span>
                  <span className="font-headline text-base font-bold">Hace 45s</span>
                </div>
              </div>
            </div>

            {/* Route guidance alert if active */}
            {showRouteGuidance && (
              <div className="mb-3 p-2.5 rounded-xl bg-secondary-fixed/50 border border-secondary text-on-secondary-fixed flex items-center justify-between text-xs font-bold animate-in fade-in">
                <div className="flex items-center gap-2">
                  <span className="material-symbols-outlined text-secondary text-[20px]">
                    navigation
                  </span>
                  <span>Ruta activa: 380m al Noroeste (aprox 4 min a caballo)</span>
                </div>
                <button
                  onClick={() => setShowRouteGuidance(false)}
                  className="text-secondary font-bold uppercase text-[11px]"
                >
                  Cerrar
                </button>
              </div>
            )}

            {/* Tactical Fast Action Buttons (Min height 52dp for field gloves) */}
            <div className="flex flex-col gap-2">
              {/* Primary Action: How to reach (Azul Río #156589 / Secondary) */}
              <button
                onClick={() => {
                  setShowRouteGuidance(!showRouteGuidance);
                  setShowUserLocation(true);
                  if (typeof navigator !== 'undefined' && navigator.vibrate) {
                    navigator.vibrate(40);
                  }
                }}
                className={`w-full min-h-[52px] px-4 rounded-xl flex items-center justify-center gap-2 text-sm uppercase tracking-wider font-bold shadow-sm transition-all ${
                  showRouteGuidance
                    ? 'bg-secondary-container text-on-secondary-container border border-secondary'
                    : 'bg-secondary text-white hover:bg-[#005d80] active:scale-[0.99]'
                }`}
                type="button"
              >
                <span className="material-symbols-outlined text-[22px]">navigation</span>
                <span>{showRouteGuidance ? 'Ruta en Pantalla' : 'Cómo Llegar (Ruta Rápida)'}</span>
              </button>

              {/* Secondary Operational Split */}
              <div className="grid grid-cols-2 gap-2">
                <button
                  onClick={() => {
                    setShowTrail(!showTrail);
                    if (typeof navigator !== 'undefined' && navigator.vibrate) {
                      navigator.vibrate(25);
                    }
                  }}
                  className={`w-full min-h-[52px] px-3 rounded-xl flex items-center justify-center gap-1.5 text-xs uppercase tracking-wider font-bold transition-colors border border-outline-variant/40 ${
                    showTrail
                      ? 'bg-tertiary-fixed text-on-tertiary-fixed'
                      : 'bg-surface-container-highest hover:bg-surface-variant text-on-surface'
                  }`}
                  type="button"
                >
                  <span className="material-symbols-outlined text-[20px]">timeline</span>
                  <span>{showTrail ? 'Ocultar Rastro' : 'Ver Recorrido'}</span>
                </button>

                <button
                  onClick={() => onOpenAnimalDetail(activeAnimal)}
                  className="w-full min-h-[52px] px-3 rounded-xl bg-surface-container-highest hover:bg-surface-variant active:scale-[0.99] text-on-surface flex items-center justify-center gap-1.5 text-xs uppercase tracking-wider font-bold transition-colors border border-outline-variant/40"
                  type="button"
                >
                  <span className="material-symbols-outlined text-[20px]">badge</span>
                  <span>Ver Ficha</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
