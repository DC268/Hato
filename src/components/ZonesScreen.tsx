/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { Geofence, Farm } from '../types/domain';

interface ZonesScreenProps {
  currentFarm: Farm;
  zones: Geofence[];
  onToggleZone: (zoneId: string) => void;
  onOpenCreateZoneModal: () => void;
  onViewZoneAnimals: (zoneId: string) => void;
  onNavigateToAlert: () => void;
}

export const ZonesScreen: React.FC<ZonesScreenProps> = ({
  currentFarm,
  zones,
  onToggleZone,
  onOpenCreateZoneModal,
  onViewZoneAnimals,
  onNavigateToAlert,
}) => {
  const potreroNorte = zones.find((z) => z.id === 'zone-norte');
  const potreroRepresa = zones.find((z) => z.id === 'zone-represa');
  const corralManejo = zones.find((z) => z.id === 'zone-corral');

  const activeZonesCount = zones.filter((z) => z.active).length;

  return (
    <div className="flex flex-col w-full px-4 pb-28">
      {/* Header Contextual */}
      <section className="mt-3 mb-4">
        <div className="flex items-center justify-between mb-1">
          <span className="font-headline text-3xl uppercase text-on-surface tracking-tight font-bold">
            Zonas de pastoreo
          </span>
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-surface-container-high text-primary text-xs uppercase font-bold border border-outline-variant/40">
            <span className="material-symbols-outlined text-[14px]">pin_drop</span>
            {currentFarm.name}
          </span>
        </div>
        <p className="text-sm text-on-surface-variant">
          {activeZonesCount} potreros delimitados por geocercas activas
        </p>
      </section>

      {/* Main Action Button "Crear nueva zona" */}
      <button
        onClick={onOpenCreateZoneModal}
        className="w-full min-h-[52px] bg-primary text-white rounded-xl text-sm uppercase tracking-wider font-bold flex items-center justify-center gap-2 shadow-md active:scale-[0.98] transition-transform font-headline text-lg"
        id="btn-crear-zona"
        type="button"
      >
        <span className="material-symbols-outlined text-[22px]">add_circle</span>
        <span>Crear nueva zona</span>
      </button>

      {/* Potrero Cards List */}
      <div className="flex flex-col gap-4 mt-5">
        {/* Card 1: Potrero Norte (Con Alerta) */}
        <article className="w-full bg-surface-container-lowest rounded-xl shadow-xs overflow-hidden flex flex-col border border-outline-variant/50 transition-all">
          {/* Mini Satellite Map */}
          <div className="relative w-full h-36 bg-surface-container-highest overflow-hidden">
            <div
              className="w-full h-full bg-cover bg-center"
              style={{
                backgroundImage: `url('https://lh3.googleusercontent.com/aida-public/AB6AXuCE-NqCl-Q1MiXSxUWBr5w9QXOkYsRv9c7eZCIud2-_GMNnVDydrGSflgs6DFnBx_aPsZi7mtd_LCAkEkaK12dcs23gS2AenhrufEW3NZF9GOebtK3Qjx2hZgBm5gkUfo91U2XHz14m3ggeGCfuy7sZGC5stAg3EM7Z1p1luJ7n_kPIY3IcvmhBoHvYUvI8moU7Yg0zcAZIwj222y-DL9t9s_pgtnJfLV2Lo7eoCnkgFrKHnB4PvOBs')`,
              }}
            ></div>
            <div className="absolute inset-0 bg-primary/10 pointer-events-none"></div>

            {/* Geofence Status Badge */}
            <div className="absolute top-3 left-3 flex items-center gap-1.5 px-3 py-1 bg-surface-container-lowest/90 backdrop-blur-sm rounded-lg shadow-xs border border-outline-variant/40">
              <span
                className={`w-2.5 h-2.5 rounded-full ${
                  potreroNorte?.active ? 'bg-primary animate-pulse' : 'bg-outline'
                }`}
              ></span>
              <span className="text-[11px] text-primary uppercase font-bold">
                {potreroNorte?.active ? 'Geocerca Activa' : 'Geocerca Pausada'}
              </span>
            </div>

            {/* Surface area badge */}
            <div className="absolute bottom-3 right-3 px-2.5 py-0.5 bg-inverse-surface/80 rounded text-inverse-on-surface text-xs font-bold font-headline">
              18,5 ha
            </div>
          </div>

          {/* Warning Bar (Escaped cow) */}
          <button
            onClick={onNavigateToAlert}
            className="w-full bg-error-container hover:bg-error-container/90 text-on-error-container px-4 py-2.5 flex items-center gap-2 text-xs transition-colors text-left"
          >
            <span className="material-symbols-outlined text-error text-[18px]">warning</span>
            <span className="flex-1 font-bold">1 animal fuera del límite (Vaca #304 / #03)</span>
            <span className="material-symbols-outlined text-error text-[16px]">chevron_right</span>
          </button>

          {/* Card Body and Switch */}
          <div className="p-4 flex flex-col gap-2.5">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="font-headline text-2xl uppercase text-on-surface tracking-tight font-bold">
                  Potrero Norte
                </h2>
                <p className="text-xs text-on-surface-variant">
                  Lote Brachiaria · Topografía ondulada
                </p>
              </div>

              {/* Toggle switch */}
              <button
                onClick={() => onToggleZone('zone-norte')}
                aria-pressed={potreroNorte?.active ?? true}
                className={`zone-toggle relative inline-flex h-8 w-14 flex-shrink-0 cursor-pointer rounded-full transition-colors focus:outline-none items-center ${
                  potreroNorte?.active ? 'bg-primary' : 'bg-surface-variant'
                }`}
                type="button"
                title={potreroNorte?.active ? 'Desactivar geocerca' : 'Activar geocerca'}
              >
                <span className="sr-only">Toggle Potrero Norte</span>
                <span
                  className={`inline-block h-6 w-6 transform rounded-full bg-white shadow-md transition-transform ${
                    potreroNorte?.active ? 'translate-x-7' : 'translate-x-1'
                  }`}
                ></span>
              </button>
            </div>

            {/* Key Metrics Grid */}
            <div className="grid grid-cols-2 gap-2 pt-1">
              <div className="p-2.5 rounded-lg bg-surface-container-low flex items-center gap-2.5 border border-outline-variant/30">
                <div className="w-8 h-8 rounded-full bg-primary-fixed flex items-center justify-center text-on-primary-fixed">
                  <span className="material-symbols-outlined text-[18px]">pets</span>
                </div>
                <div>
                  <span className="block text-[11px] uppercase text-on-surface-variant font-bold">
                    Ganado
                  </span>
                  <span className="font-headline text-lg text-on-surface font-bold">
                    14 animales
                  </span>
                </div>
              </div>

              <div className="p-2.5 rounded-lg bg-surface-container-low flex items-center gap-2.5 border border-outline-variant/30">
                <div className="w-8 h-8 rounded-full bg-secondary-fixed flex items-center justify-center text-on-secondary-fixed">
                  <span className="material-symbols-outlined text-[18px]">straighten</span>
                </div>
                <div>
                  <span className="block text-[11px] uppercase text-on-surface-variant font-bold">
                    Perímetro
                  </span>
                  <span className="font-headline text-lg text-on-surface font-bold">
                    1.820 m
                  </span>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="grid grid-cols-2 gap-2 pt-1 mt-1">
              <button
                onClick={() => alert('Modo de edición de linderos habilitado: ajuste los postes de la cerca en el mapa.')}
                className="min-h-[48px] bg-surface-container-high hover:bg-surface-variant text-on-surface rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-1 active:scale-[0.99] transition-transform"
                type="button"
              >
                <span className="material-symbols-outlined text-[18px]">polyline</span>
                <span>Editar límites</span>
              </button>

              <button
                onClick={() => onViewZoneAnimals('zone-norte')}
                className="min-h-[48px] bg-on-primary-container text-primary rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-1 active:bg-primary-fixed transition-colors"
                type="button"
              >
                <span className="material-symbols-outlined text-[18px]">list_alt</span>
                <span>Ver animales</span>
              </button>
            </div>
          </div>
        </article>

        {/* Card 2: Potrero La Represa (Normal / Todo en orden) */}
        <article className="w-full bg-surface-container-lowest rounded-xl shadow-xs overflow-hidden flex flex-col border border-outline-variant/50 transition-all">
          <div className="relative w-full h-36 bg-surface-container-highest overflow-hidden">
            <div
              className="w-full h-full bg-cover bg-center"
              style={{
                backgroundImage: `url('https://lh3.googleusercontent.com/aida-public/AB6AXuAn_odwODHiLNJdKHK2DF5hZ58rMCScLlD7PQQHc8O2i-4xhbqLY9ELo3eL4fkVETm9Et9Tw-0WdpHuIHdoX2JIrjrowN8MzBOihGT6qTkbd1Rpzgx_fLggjt3bAZOKY_XxS7330A0VPjMQevHyBjkvqSpBb76IoHLRL6nSQNaZVeXZ4vJrFb0FeNfCasIW2hXEJ7JQPMQ-NnKrgEPrJ9ZI9endGjOFhLXdXaGfT6vGKbPyMPBR3ZjD')`,
              }}
            ></div>
            <div className="absolute inset-0 bg-secondary/10 pointer-events-none"></div>

            <div className="absolute top-3 left-3 flex items-center gap-1.5 px-3 py-1 bg-surface-container-lowest/90 backdrop-blur-sm rounded-lg shadow-xs border border-outline-variant/40">
              <span
                className={`w-2.5 h-2.5 rounded-full ${
                  potreroRepresa?.active ? 'bg-primary animate-pulse' : 'bg-outline'
                }`}
              ></span>
              <span className="text-[11px] text-primary uppercase font-bold">
                {potreroRepresa?.active ? 'Geocerca Activa' : 'Geocerca Pausada'}
              </span>
            </div>

            <div className="absolute bottom-3 right-3 px-2.5 py-0.5 bg-inverse-surface/80 rounded text-inverse-on-surface text-xs font-bold font-headline">
              24,0 ha
            </div>
          </div>

          {/* Status Bar */}
          <div className="w-full bg-surface-container px-4 py-2 flex items-center gap-2 text-xs text-primary font-bold">
            <span className="material-symbols-outlined text-primary text-[18px]">check_circle</span>
            <span className="flex-1">Todo en orden · Rebaño estable</span>
          </div>

          <div className="p-4 flex flex-col gap-2.5">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="font-headline text-2xl uppercase text-on-surface tracking-tight font-bold">
                  Potrero La Represa
                </h2>
                <p className="text-xs text-on-surface-variant">
                  Acceso directo a bebedero comunal
                </p>
              </div>

              <button
                onClick={() => onToggleZone('zone-represa')}
                aria-pressed={potreroRepresa?.active ?? true}
                className={`zone-toggle relative inline-flex h-8 w-14 flex-shrink-0 cursor-pointer rounded-full transition-colors focus:outline-none items-center ${
                  potreroRepresa?.active ? 'bg-primary' : 'bg-surface-variant'
                }`}
                type="button"
                title={potreroRepresa?.active ? 'Desactivar geocerca' : 'Activar geocerca'}
              >
                <span className="sr-only">Toggle Potrero La Represa</span>
                <span
                  className={`inline-block h-6 w-6 transform rounded-full bg-white shadow-md transition-transform ${
                    potreroRepresa?.active ? 'translate-x-7' : 'translate-x-1'
                  }`}
                ></span>
              </button>
            </div>

            <div className="grid grid-cols-2 gap-2 pt-1">
              <div className="p-2.5 rounded-lg bg-surface-container-low flex items-center gap-2.5 border border-outline-variant/30">
                <div className="w-8 h-8 rounded-full bg-primary-fixed flex items-center justify-center text-on-primary-fixed">
                  <span className="material-symbols-outlined text-[18px]">pets</span>
                </div>
                <div>
                  <span className="block text-[11px] uppercase text-on-surface-variant font-bold">
                    Ganado
                  </span>
                  <span className="font-headline text-lg text-on-surface font-bold">
                    10 animales
                  </span>
                </div>
              </div>

              <div className="p-2.5 rounded-lg bg-surface-container-low flex items-center gap-2.5 border border-outline-variant/30">
                <div className="w-8 h-8 rounded-full bg-secondary-fixed flex items-center justify-center text-on-secondary-fixed">
                  <span className="material-symbols-outlined text-[18px]">water_drop</span>
                </div>
                <div>
                  <span className="block text-[11px] uppercase text-on-surface-variant font-bold">
                    Abrevadero
                  </span>
                  <span className="font-headline text-lg text-on-surface font-bold">
                    Nivel óptimo
                  </span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2 pt-1 mt-1">
              <button
                onClick={() => alert('Modo de edición de linderos habilitado.')}
                className="min-h-[48px] bg-surface-container-high hover:bg-surface-variant text-on-surface rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-1 active:scale-[0.99] transition-transform"
                type="button"
              >
                <span className="material-symbols-outlined text-[18px]">polyline</span>
                <span>Editar límites</span>
              </button>

              <button
                onClick={() => onViewZoneAnimals('zone-represa')}
                className="min-h-[48px] bg-on-primary-container text-primary rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-1 active:bg-primary-fixed transition-colors"
                type="button"
              >
                <span className="material-symbols-outlined text-[18px]">list_alt</span>
                <span>Ver animales</span>
              </button>
            </div>
          </div>
        </article>

        {/* Card 3: Corral de Manejo (Pausado) */}
        <article className="w-full bg-surface-container-lowest rounded-xl shadow-xs overflow-hidden flex flex-col border border-outline-variant/50 transition-all opacity-95">
          <div className="relative w-full h-28 bg-surface-container-highest overflow-hidden">
            <div
              className="w-full h-full bg-cover bg-center grayscale contrast-75"
              style={{
                backgroundImage: `url('https://lh3.googleusercontent.com/aida-public/AB6AXuBHjnq1Mv53rRx5vpKee5qGJfdHWDCiJJoIsuQ03-hQPY3q0I8YtEF3nwRd4Oq1kdAhowd67NOVGfSVHQXRDP-CZSRpvHXfmidNptCZFSeFLHbMzc8r2djc9WauFtIHGN5hTl4UtGEq0ETz0nwL6WQ6_yClnNxaFQcWiL10h0ie4opAXO9D8tncD5X6lvehEugTegNXQOKY8PD-Fj6aBGkHERiN1ssh3CERfGo3h_rJTdd-oibmNp0x')`,
              }}
            ></div>
            <div className="absolute inset-0 bg-surface-dim/40 pointer-events-none"></div>

            <div className="absolute top-3 left-3 flex items-center gap-1.5 px-3 py-1 bg-surface-container-lowest/90 backdrop-blur-sm rounded-lg shadow-xs border border-outline-variant/40">
              <span
                className={`w-2.5 h-2.5 rounded-full ${
                  corralManejo?.active ? 'bg-primary' : 'bg-outline'
                }`}
              ></span>
              <span className="text-[11px] text-on-surface-variant uppercase font-bold">
                {corralManejo?.active ? 'Geocerca Activa' : 'Geocerca Pausada'}
              </span>
            </div>
          </div>

          <div className="p-4 flex flex-col gap-2.5">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="font-headline text-2xl uppercase text-on-surface tracking-tight font-bold">
                  Corral de Manejo
                </h2>
                <p className="text-xs text-on-surface-variant">Báscula y área veterinaria</p>
              </div>

              <button
                onClick={() => onToggleZone('zone-corral')}
                aria-pressed={corralManejo?.active ?? false}
                className={`zone-toggle relative inline-flex h-8 w-14 flex-shrink-0 cursor-pointer rounded-full transition-colors focus:outline-none items-center ${
                  corralManejo?.active ? 'bg-primary' : 'bg-surface-variant'
                }`}
                type="button"
                title={corralManejo?.active ? 'Desactivar geocerca' : 'Activar geocerca'}
              >
                <span className="sr-only">Toggle Corral de Manejo</span>
                <span
                  className={`inline-block h-6 w-6 transform rounded-full bg-white shadow-md transition-transform ${
                    corralManejo?.active ? 'translate-x-7' : 'translate-x-1'
                  }`}
                ></span>
              </button>
            </div>

            <div className="grid grid-cols-2 gap-2 pt-1">
              <div className="p-2.5 rounded-lg bg-surface-container-low flex items-center gap-2.5 border border-outline-variant/30">
                <div className="w-8 h-8 rounded-full bg-surface-variant flex items-center justify-center text-on-surface-variant">
                  <span className="material-symbols-outlined text-[18px]">pets</span>
                </div>
                <div>
                  <span className="block text-[11px] uppercase text-on-surface-variant font-bold">
                    Ganado
                  </span>
                  <span className="font-headline text-lg text-on-surface font-bold">
                    3 animales
                  </span>
                </div>
              </div>

              <div className="p-2.5 rounded-lg bg-surface-container-low flex items-center gap-2.5 border border-outline-variant/30">
                <div className="w-8 h-8 rounded-full bg-surface-variant flex items-center justify-center text-on-surface-variant">
                  <span className="material-symbols-outlined text-[18px]">radar</span>
                </div>
                <div>
                  <span className="block text-[11px] uppercase text-on-surface-variant font-bold">
                    Tolerancia
                  </span>
                  <span className="font-headline text-lg text-on-surface font-bold">
                    10 m radio
                  </span>
                </div>
              </div>
            </div>

            <div className="pt-1 mt-1">
              <button
                onClick={() => alert('Parámetros de báscula y antena RFID del corral en línea.')}
                className="w-full min-h-[48px] bg-surface-container-high hover:bg-surface-variant text-on-surface rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-1 active:scale-[0.99] transition-transform"
                type="button"
              >
                <span className="material-symbols-outlined text-[18px]">tune</span>
                <span>Configurar corral</span>
              </button>
            </div>
          </div>
        </article>
      </div>

      {/* Field Usage Tip Banner */}
      <aside className="mt-5 p-4 rounded-xl bg-tertiary-fixed text-on-tertiary-fixed flex items-start gap-3 shadow-xs border border-tertiary/20">
        <div className="w-10 h-10 rounded-full bg-tertiary-container text-on-tertiary-container flex items-center justify-center flex-shrink-0 mt-0.5">
          <span className="material-symbols-outlined text-[22px]">lightbulb</span>
        </div>
        <div className="flex-1">
          <span className="block font-headline text-lg font-bold mb-0.5 text-on-tertiary-fixed">
            Consejo de uso en campo
          </span>
          <p className="text-xs text-on-tertiary-fixed-variant leading-snug">
            Toque cualquier potrero para ajustar el perímetro o asignar ganado. Puede desactivar
            temporalmente una geocerca al arrear animales para evitar falsas alarmas.
          </p>
        </div>
      </aside>
    </div>
  );
};
