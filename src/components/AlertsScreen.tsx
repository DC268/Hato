/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { Alert } from '../types/domain';

interface AlertsScreenProps {
  alerts: Alert[];
  historyAlerts: Alert[];
  onAcknowledgeAlert: (alertId: string, notes?: string) => void;
  onViewOnMap: (animalId: string) => void;
  onNavigateToZones: () => void;
  onShowToast: (message: string) => void;
}

export const AlertsScreen: React.FC<AlertsScreenProps> = ({
  alerts,
  historyAlerts,
  onAcknowledgeAlert,
  onViewOnMap,
  onNavigateToZones,
  onShowToast,
}) => {
  const [activeTab, setActiveTab] = useState<'pendientes' | 'historial'>('pendientes');
  const [filterCategory, setFilterCategory] = useState<string>('todas');
  const [isSyncing, setIsSyncing] = useState(false);

  const pendingAlerts = alerts.filter((a) => a.status === 'PENDING');

  const filteredAlerts = pendingAlerts.filter((a) => {
    if (filterCategory === 'todas') return true;
    return a.category === filterCategory;
  });

  const handleSync = () => {
    setIsSyncing(true);
    if (typeof navigator !== 'undefined' && navigator.vibrate) {
      navigator.vibrate(20);
    }
    setTimeout(() => {
      setIsSyncing(false);
      onShowToast('Conexión con collares actualizada con éxito');
    }, 700);
  };

  const handleAttend = (alert: Alert) => {
    onAcknowledgeAlert(alert.id, 'Atendida en campo por el operador');
    onShowToast(`Novedad de ${alert.animalName} registrada como atendida`);
  };

  return (
    <div className="flex flex-col w-full pb-24">
      {/* Status Bar / Resumen Operativo */}
      <div className="px-4 pt-3 pb-1">
        <div className="bg-surface-container rounded-xl p-3.5 flex items-center justify-between shadow-xs border border-outline-variant/40">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-error-container text-on-error-container flex items-center justify-center flex-shrink-0">
              <span className="material-symbols-outlined text-[24px] text-error">crisis_alert</span>
            </div>
            <div>
              <span className="font-headline text-lg uppercase tracking-tight text-on-surface block leading-tight font-bold">
                {pendingAlerts.length} incidencias activas
              </span>
              <span className="text-xs text-on-surface-variant">
                Requieren su supervisión en campo
              </span>
            </div>
          </div>

          <button
            onClick={handleSync}
            className={`w-10 h-10 rounded-full bg-surface-container-high flex items-center justify-center text-primary active:scale-95 transition-all ${
              isSyncing ? 'animate-spin' : ''
            }`}
            title="Sincronizar novedades de campo"
            type="button"
          >
            <span className="material-symbols-outlined text-[20px]">sync</span>
          </button>
        </div>
      </div>

      {/* Tabs: Pendientes vs Historial */}
      <div className="px-4 mt-2">
        <div className="flex bg-surface-container-high p-1 rounded-xl border border-outline-variant/40">
          <button
            onClick={() => setActiveTab('pendientes')}
            className={`flex-1 min-h-[48px] rounded-lg py-2 text-center text-xs uppercase tracking-wider font-bold transition-all flex items-center justify-center gap-1.5 ${
              activeTab === 'pendientes'
                ? 'bg-surface-container-lowest text-primary shadow-xs'
                : 'text-on-surface-variant'
            }`}
            type="button"
          >
            <span>Pendientes</span>
            <span className="w-5 h-5 rounded-full bg-error text-white text-[11px] flex items-center justify-center font-bold">
              {pendingAlerts.length}
            </span>
          </button>

          <button
            onClick={() => setActiveTab('historial')}
            className={`flex-1 min-h-[48px] rounded-lg py-2 text-center text-xs uppercase tracking-wider font-bold transition-all flex items-center justify-center gap-1.5 ${
              activeTab === 'historial'
                ? 'bg-surface-container-lowest text-primary shadow-xs'
                : 'text-on-surface-variant'
            }`}
            type="button"
          >
            <span>Historial</span>
            <span className="px-1.5 py-0.5 rounded-full bg-surface-container-highest text-on-surface-variant text-[11px]">
              {historyAlerts.length}
            </span>
          </button>
        </div>
      </div>

      {activeTab === 'pendientes' ? (
        <>
          {/* Quick Filters */}
          <div className="mt-2.5 px-4">
            <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-1">
              {[
                { id: 'todas', label: 'Todas', icon: 'done' },
                { id: 'salida', label: 'Salida de potrero', icon: 'fence' },
                { id: 'inactividad', label: 'Sin movimiento', icon: 'hourglass_top' },
                { id: 'bateria', label: 'Batería baja', icon: 'battery_alert' },
              ].map((chip) => {
                const isActive = filterCategory === chip.id;
                return (
                  <button
                    key={chip.id}
                    onClick={() => setFilterCategory(chip.id)}
                    className={`flex-shrink-0 min-h-[42px] px-3.5 rounded-full text-xs font-bold flex items-center gap-1.5 transition-colors shadow-xs ${
                      isActive
                        ? 'bg-primary-container text-white border border-primary'
                        : 'bg-surface-container-high text-on-surface-variant hover:bg-surface-variant'
                    }`}
                    type="button"
                  >
                    <span className="material-symbols-outlined text-[17px]">{chip.icon}</span>
                    <span>{chip.label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Alerts Cards Container */}
          <div className="px-4 mt-3 flex flex-col gap-3.5">
            {filteredAlerts.length === 0 ? (
              <div className="bg-surface-container-lowest rounded-2xl p-8 text-center flex flex-col items-center justify-center gap-3 shadow-xs border border-outline-variant/40 animate-in fade-in">
                <div className="w-16 h-16 rounded-full bg-on-primary-container/40 text-primary flex items-center justify-center">
                  <span className="material-symbols-outlined text-[36px]">task_alt</span>
                </div>
                <div>
                  <h3 className="font-headline text-2xl uppercase text-on-surface tracking-tight font-bold">
                    Hato bajo control
                  </h3>
                  <p className="text-xs text-on-surface-variant max-w-xs mx-auto mt-1 leading-relaxed">
                    No tiene alertas pendientes en este momento. Todos los animales se encuentran
                    pastando en sus zonas asignadas.
                  </p>
                </div>
                <button
                  onClick={onNavigateToZones}
                  className="min-h-[48px] px-5 rounded-xl bg-primary text-white text-xs uppercase tracking-wider font-bold active:scale-95 transition-transform"
                  type="button"
                >
                  Ver estado de potreros
                </button>
              </div>
            ) : (
              filteredAlerts.map((alert) => {
                const isExit = alert.category === 'salida' || alert.type === 'EXITED_ZONE';
                const isIdle = alert.category === 'inactividad' || alert.type === 'IDLE';
                const isBattery = alert.category === 'bateria' || alert.type === 'LOW_BATTERY';

                let accentColor = 'bg-error';
                let iconName = 'warning';
                let badgeClass = 'bg-error text-white';
                let badgeText = 'URGENTE';
                let iconContainerClass = 'bg-error-container text-error';

                if (isIdle) {
                  accentColor = 'bg-tertiary-container';
                  iconName = 'hourglass_top';
                  badgeClass = 'bg-tertiary-container text-white';
                  badgeText = 'ATENCIÓN';
                  iconContainerClass = 'bg-tertiary-fixed text-tertiary';
                } else if (isBattery) {
                  accentColor = 'bg-secondary';
                  iconName = 'battery_alert';
                  badgeClass = 'bg-secondary text-white';
                  badgeText = 'MANTENIMIENTO';
                  iconContainerClass = 'bg-secondary-fixed text-secondary';
                }

                return (
                  <article
                    key={alert.id}
                    className="bg-surface-container-lowest rounded-xl shadow-xs overflow-hidden flex flex-col border border-outline-variant/50 transition-all duration-200"
                  >
                    {/* Top Accent Band (4dp) */}
                    <div className={`h-1.5 w-full ${accentColor}`}></div>

                    <div className="p-4 flex flex-col gap-2.5">
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex items-start gap-3 min-w-0">
                          <div
                            className={`w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0 shadow-xs ${iconContainerClass}`}
                          >
                            <span className="material-symbols-outlined text-[30px]">
                              {iconName}
                            </span>
                          </div>
                          <div className="min-w-0">
                            <div className="flex items-center gap-2 mb-0.5">
                              <span
                                className={`px-2 py-0.5 rounded text-[10px] tracking-wide font-bold uppercase font-headline ${badgeClass}`}
                              >
                                {badgeText}
                              </span>
                              <span className="text-xs text-on-surface-variant">Hace unos minutos</span>
                            </div>
                            <h2 className="font-headline text-2xl text-on-surface leading-tight uppercase tracking-tight truncate font-bold">
                              {alert.title}
                            </h2>
                          </div>
                        </div>
                      </div>

                      {/* Location & Details Box */}
                      <div className="bg-surface-container-low rounded-xl p-3 flex flex-col gap-1 border border-outline-variant/30">
                        {alert.zoneName && (
                          <div className="flex items-center gap-1.5 text-on-surface">
                            <span className="material-symbols-outlined text-[17px] text-primary">
                              {isExit ? 'explore' : isIdle ? 'water' : 'fence'}
                            </span>
                            <span className="font-headline text-lg font-bold">{alert.zoneName}</span>
                          </div>
                        )}
                        <p className="text-xs text-on-surface-variant leading-relaxed">
                          {alert.description}
                        </p>

                        {/* Battery Level Bar if applicable */}
                        {isBattery && (
                          <div className="mt-2 pt-1 border-t border-outline-variant/30">
                            <div className="flex items-center justify-between text-xs font-bold mb-1">
                              <span>Nivel restante de collar GPS</span>
                              <span className="text-secondary font-headline text-lg">15 %</span>
                            </div>
                            <div className="w-full bg-surface-container-highest h-2 rounded-full overflow-hidden">
                              <div className="bg-secondary h-full rounded-full" style={{ width: '15%' }}></div>
                            </div>
                          </div>
                        )}
                      </div>

                      {/* Mini Static Aerial Map for escape */}
                      {isExit && (
                        <div
                          className="w-full h-28 rounded-xl bg-surface-container-high relative overflow-hidden flex items-end p-2 bg-cover bg-center border border-outline-variant/40"
                          style={{
                            backgroundImage: `url('https://lh3.googleusercontent.com/aida-public/AB6AXuA7ex5d9QWWQvWCfsNjcQWfjQWKaXsyB9K9rmBoaDDdf3TR6NaSzD1JgALTILerI8xoX5ux2zXpD6FwnfsPvlshzY6OHDfeYm24frw8lI9gFhOn8NYvoW_Ple6VcFIVQuBhIrygtuvpFAzzxOo1Er8VZ3As45GS19kakZLfOFJNqW1w3VWvE-RqL-Rwvfqegett8hajBRXWra99U-3iDcUtOZI6ImyA-urgfqvPDPVQxz9OuwbypWJ0')`,
                          }}
                        >
                          <div className="bg-surface-container-lowest/90 backdrop-blur-sm px-2.5 py-1 rounded-md flex items-center gap-1.5 text-on-surface shadow-xs text-xs font-bold">
                            <span className="w-2.5 h-2.5 rounded-full bg-error animate-ping"></span>
                            <span>Último reporte GPS: hace 4 min</span>
                          </div>
                        </div>
                      )}

                      {/* Tactile Big Action Buttons (Min 48-52dp for gloves) */}
                      <div className="flex flex-col sm:flex-row gap-2 pt-1">
                        {!isBattery && (
                          <button
                            onClick={() => onViewOnMap(alert.animalId)}
                            className="flex-1 min-h-[50px] px-4 rounded-xl bg-primary-container text-white font-headline text-base uppercase tracking-wider font-bold flex items-center justify-center gap-2 active:bg-primary transition-colors shadow-xs"
                            type="button"
                          >
                            <span className="material-symbols-outlined text-[20px]">pin_drop</span>
                            <span>Ver en el mapa</span>
                          </button>
                        )}

                        <button
                          onClick={() => handleAttend(alert)}
                          className="flex-1 min-h-[50px] px-4 rounded-xl bg-surface-container-high hover:bg-surface-variant text-on-surface font-headline text-base uppercase tracking-wider font-bold flex items-center justify-center gap-2 active:scale-[0.99] transition-transform border border-outline-variant/40"
                          type="button"
                        >
                          <span className="material-symbols-outlined text-[20px]">
                            {isBattery ? 'offline_bolt' : 'check_circle'}
                          </span>
                          <span>{isBattery ? 'Programar recarga' : 'Marcar atendida'}</span>
                        </button>
                      </div>
                    </div>
                  </article>
                );
              })
            )}
          </div>
        </>
      ) : (
        /* Historial Section */
        <div className="px-4 mt-3 flex flex-col gap-2.5">
          <div className="bg-surface-container-low rounded-xl p-3.5 flex items-center justify-between border border-outline-variant/40">
            <span className="font-headline text-base font-bold text-on-surface uppercase">
              Registro de las últimas 48 horas
            </span>
            <span className="text-xs text-on-surface-variant font-bold">
              {historyAlerts.length} resueltas
            </span>
          </div>

          {historyAlerts.map((item) => (
            <div
              key={item.id}
              className="bg-surface-container-lowest rounded-xl p-3.5 flex items-center justify-between shadow-xs border border-outline-variant/40"
            >
              <div className="flex items-center gap-3 min-w-0">
                <div className="w-9 h-9 rounded-full bg-surface-container-high text-on-surface-variant flex items-center justify-center flex-shrink-0">
                  <span className="material-symbols-outlined text-[20px] text-primary">check</span>
                </div>
                <div className="min-w-0">
                  <p className="font-headline text-lg font-bold text-on-surface truncate">
                    {item.title}
                  </p>
                  <p className="text-xs text-on-surface-variant">
                    Atendida por {item.attendedBy || 'Capataz'} · {item.attendedAt || 'Ayer'}
                  </p>
                </div>
              </div>
              <span className="material-symbols-outlined text-outline text-[20px]">
                chevron_right
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
