/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * HATO - Backend POO Architecture, Ingestion Simulator & Diagnostic Console
 * Follows the Java 21 / Spring Boot / PostGIS backend specification document
 */

import React, { useState } from 'react';
import {
  Animal,
  Alert,
  AlertProperties,
  PositionRequest,
  Farm
} from '../types/domain';

interface BackendInspectorScreenProps {
  currentFarm: Farm;
  userRole: string;
  onRoleChange: (role: string) => void;
  animals: Animal[];
  alerts: Alert[];
  alertProperties: AlertProperties;
  onUpdateAlertProperties: (props: AlertProperties) => void;
  onSimulateIngest: (req: PositionRequest) => void;
  onShowToast: (message: string) => void;
  lastIngestedPayload: PositionRequest | null;
}

export const BackendInspectorScreen: React.FC<BackendInspectorScreenProps> = ({
  currentFarm,
  userRole,
  onRoleChange,
  animals,
  alerts,
  alertProperties,
  onUpdateAlertProperties,
  onSimulateIngest,
  onShowToast,
  lastIngestedPayload,
}) => {
  const [activeSection, setActiveSection] = useState<'simulator' | 'rules' | 'architecture' | 'reports'>('simulator');
  const [customLat, setCustomLat] = useState('10.4725');
  const [customLng, setCustomLng] = useState('-84.6472');
  const [customSpeed, setCustomSpeed] = useState('3.5');
  const [customBattery, setCustomBattery] = useState('75');
  const [customAccuracy, setCustomAccuracy] = useState('6.0');
  const [selectedAnimalId, setSelectedAnimalId] = useState('a-12');
  const [isExporting, setIsExporting] = useState(false);

  // Pre-built simulation scenarios
  const runEscapeScenario = (animalId: string) => {
    const animal = animals.find((a) => a.id === animalId) || animals[2]; // Vaca 12
    // Send 2 consecutive readings outside Potrero Norte to satisfy ExitConfirmationPolicy
    const baseTime = Date.now();
    const req1: PositionRequest = {
      trackerCode: animal.trackerId,
      lat: 10.4735,
      lng: -84.6480, // Clearly outside
      recordedAt: new Date(baseTime - 30000).toISOString(),
      speedKmh: 4.8,
      battery: 88,
      accuracy: 6.5,
    };
    const req2: PositionRequest = {
      trackerCode: animal.trackerId,
      lat: 10.4740,
      lng: -84.6485,
      recordedAt: new Date(baseTime).toISOString(),
      speedKmh: 5.2,
      battery: 87,
      accuracy: 5.8,
    };

    onSimulateIngest(req1);
    setTimeout(() => {
      onSimulateIngest(req2);
      onShowToast(`Simulación completada: Salida confirmada de ${animal.name} (2 lecturas fuera).`);
    }, 400);
  };

  const runReturnScenario = () => {
    const req: PositionRequest = {
      trackerCode: 'TRK-0342', // Vaca 03
      lat: 10.4700,
      lng: -84.6445, // Back inside Potrero Norte
      recordedAt: new Date().toISOString(),
      speedKmh: 1.5,
      battery: 15,
      accuracy: 4.2,
    };
    onSimulateIngest(req);
    onShowToast('Simulación: Vaca 03 regresó dentro del lindero. Alerta auto-resuelta.');
  };

  const runSpeedScenario = () => {
    const req: PositionRequest = {
      trackerCode: 'TRK-0004', // Toro Barcino
      lat: 10.4850,
      lng: -84.6600,
      recordedAt: new Date().toISOString(),
      speedKmh: 52.0, // High speed indicating truck transit
      battery: 84,
      accuracy: 7.0,
    };
    onSimulateIngest(req);
    onShowToast('Simulación: Velocidad sospechosa de 52 km/h detectada (Alerta de transporte).');
  };

  const runDuplicateScenario = () => {
    const time = new Date().toISOString();
    const req: PositionRequest = {
      trackerCode: 'TRK-0112',
      lat: 10.4655,
      lng: -84.6408,
      recordedAt: time,
      speedKmh: 1.2,
      battery: 92,
      accuracy: 4.8,
    };
    // Send twice
    onSimulateIngest(req);
    onSimulateIngest(req);
    onShowToast('Prueba de idempotencia: clave (trackerId, recordedAt) única. Duplicado ignorado sin error.');
  };

  const handleCustomIngest = () => {
    const animal = animals.find((a) => a.id === selectedAnimalId) || animals[0];
    const req: PositionRequest = {
      trackerCode: animal.trackerId,
      lat: parseFloat(customLat) || 10.468,
      lng: parseFloat(customLng) || -84.644,
      recordedAt: new Date().toISOString(),
      speedKmh: parseFloat(customSpeed) || 0,
      battery: parseInt(customBattery, 10) || 80,
      accuracy: parseFloat(customAccuracy) || 8.0,
    };
    onSimulateIngest(req);
    onShowToast(`Posición GPS ingerida para ${animal.name} (${animal.trackerId})`);
  };

  const handleExportReport = () => {
    setIsExporting(true);
    setTimeout(() => {
      const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(
        JSON.stringify(
          {
            farm: currentFarm,
            generatedAt: new Date().toISOString(),
            totalAnimals: animals.length,
            activeAlerts: alerts.filter((a) => a.status === 'PENDING'),
            resolvedAlerts: alerts.filter((a) => a.status === 'RESOLVED'),
          },
          null,
          2
        )
      );
      const downloadAnchor = document.createElement('a');
      downloadAnchor.setAttribute('href', dataStr);
      downloadAnchor.setAttribute('download', `reporte_hato_${currentFarm.id}_${Date.now()}.json`);
      document.body.appendChild(downloadAnchor);
      downloadAnchor.click();
      downloadAnchor.remove();
      setIsExporting(false);
      onShowToast('Reporte generado y descargado exitosamente');
    }, 600);
  };

  return (
    <div className="flex flex-col w-full px-4 pb-28">
      {/* Header Contextual */}
      <section className="mt-3 mb-3">
        <div className="flex items-center justify-between mb-1">
          <span className="font-headline text-3xl uppercase text-on-surface tracking-tight font-bold">
            Backend & Diagnóstico
          </span>
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-primary-fixed text-primary text-xs uppercase font-bold">
            <span className="material-symbols-outlined text-[14px]">terminal</span>
            Java 21 · POO
          </span>
        </div>
        <p className="text-xs text-on-surface-variant">
          Simulador de protocolos GPS (Traccar / LoRa / TCP), motor de reglas y telemetría en tiempo real.
        </p>
      </section>

      {/* Navigation Sub-Tabs */}
      <div className="flex bg-surface-container-high p-1 rounded-xl border border-outline-variant/40 mb-4 overflow-x-auto no-scrollbar">
        {[
          { id: 'simulator', label: 'Simulador GPS', icon: 'sensors' },
          { id: 'rules', label: 'Reglas & Umbrales', icon: 'tune' },
          { id: 'architecture', label: 'Arquitectura', icon: 'account_tree' },
          { id: 'reports', label: 'Reportes', icon: 'description' },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveSection(tab.id as any)}
            className={`flex-1 min-h-[42px] px-3 rounded-lg text-xs font-bold uppercase tracking-wider flex items-center justify-center gap-1.5 transition-all whitespace-nowrap ${
              activeSection === tab.id
                ? 'bg-surface-container-lowest text-primary shadow-xs'
                : 'text-on-surface-variant'
            }`}
          >
            <span className="material-symbols-outlined text-[17px]">{tab.icon}</span>
            <span>{tab.label}</span>
          </button>
        ))}
      </div>

      {/* Role & Farm Management Strip */}
      <div className="bg-surface-container-lowest rounded-xl p-3.5 shadow-xs border border-outline-variant/40 mb-4">
        <div className="flex items-center justify-between">
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-outline block">
              Rol de Usuario Activo
            </span>
            <span className="font-headline text-lg font-bold text-on-surface">
              {userRole}
            </span>
          </div>

          <div className="flex items-center gap-1">
            {['Propietario', 'Capataz', 'Solo Lectura'].map((role) => (
              <button
                key={role}
                onClick={() => {
                  onRoleChange(role);
                  onShowToast(`Rol cambiado a: ${role}`);
                }}
                className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-colors ${
                  userRole === role
                    ? 'bg-primary text-white'
                    : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                }`}
              >
                {role}
              </button>
            ))}
          </div>
        </div>
      </div>

      {activeSection === 'simulator' && (
        <div className="flex flex-col gap-4 animate-in fade-in">
          {/* Preset Simulation Tests */}
          <div className="bg-surface-container-lowest rounded-xl p-4 shadow-xs border border-outline-variant/40">
            <h3 className="font-headline text-xl uppercase font-bold text-on-surface mb-2 flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]">play_circle</span>
              <span>Escenarios de Prueba Automatizados</span>
            </h3>
            <p className="text-xs text-on-surface-variant mb-3 leading-relaxed">
              Pruebe los criterios de aceptación del backend (Sección 21 del documento): deduplicación, filtro de precisión de 50m y confirmación de escape con 2 lecturas consecutivas.
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              <button
                onClick={() => runEscapeScenario('a-12')}
                className="p-3 rounded-xl bg-error-container hover:bg-error-container/90 text-on-error-container flex items-center gap-2.5 text-left border border-error/20 active:scale-[0.99] transition-transform"
                type="button"
              >
                <span className="material-symbols-outlined text-error text-[24px]">crisis_alert</span>
                <div>
                  <strong className="block text-xs font-bold">Simular escape de Vaca 12</strong>
                  <span className="text-[11px] text-error">Envía 2 posiciones consecutivas fuera de zona</span>
                </div>
              </button>

              <button
                onClick={runReturnScenario}
                className="p-3 rounded-xl bg-primary-fixed/40 hover:bg-primary-fixed/60 text-primary flex items-center gap-2.5 text-left border border-primary/20 active:scale-[0.99] transition-transform"
                type="button"
              >
                <span className="material-symbols-outlined text-primary text-[24px]">task_alt</span>
                <div>
                  <strong className="block text-xs font-bold">Simular regreso de Vaca 03</strong>
                  <span className="text-[11px] text-primary/80">Reingresa a zona y auto-resuelve alerta</span>
                </div>
              </button>

              <button
                onClick={runSpeedScenario}
                className="p-3 rounded-xl bg-secondary-fixed/50 hover:bg-secondary-fixed/70 text-on-secondary-fixed flex items-center gap-2.5 text-left border border-secondary/20 active:scale-[0.99] transition-transform"
                type="button"
              >
                <span className="material-symbols-outlined text-secondary text-[24px]">speed</span>
                <div>
                  <strong className="block text-xs font-bold">Simular velocidad de 52 km/h</strong>
                  <span className="text-[11px] text-secondary">Dispara SpeedRule (posible transporte no autorizado)</span>
                </div>
              </button>

              <button
                onClick={runDuplicateScenario}
                className="p-3 rounded-xl bg-surface-container hover:bg-surface-container-high text-on-surface flex items-center gap-2.5 text-left border border-outline-variant/40 active:scale-[0.99] transition-transform"
                type="button"
              >
                <span className="material-symbols-outlined text-outline text-[24px]">content_copy</span>
                <div>
                  <strong className="block text-xs font-bold">Probar idempotencia (Duplicados)</strong>
                  <span className="text-[11px] text-outline">Envía la misma lectura dos veces</span>
                </div>
              </button>
            </div>
          </div>

          {/* Manual Ingest Console */}
          <div className="bg-surface-container-lowest rounded-xl p-4 shadow-xs border border-outline-variant/40">
            <h3 className="font-headline text-xl uppercase font-bold text-on-surface mb-2 flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]">satellite_alt</span>
              <span>Ingesta Manual (POST /api/ingest/positions)</span>
            </h3>
            <p className="text-xs text-on-surface-variant mb-3">
              Formato de entrada Traccar / LoRaWAN / TCP con cabecera <code className="bg-surface-container px-1 py-0.5 rounded text-primary font-mono text-[11px]">X-Ingest-Key</code>:
            </p>

            <div className="grid grid-cols-2 gap-3 mb-3">
              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                  Animal
                </label>
                <select
                  value={selectedAnimalId}
                  onChange={(e) => setSelectedAnimalId(e.target.value)}
                  className="w-full h-10 px-3 rounded-lg bg-surface-container-low border border-outline-variant text-xs text-on-surface focus:outline-none"
                >
                  {animals.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.name} ({a.trackerId})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                  Velocidad (km/h)
                </label>
                <input
                  type="number"
                  value={customSpeed}
                  onChange={(e) => setCustomSpeed(e.target.value)}
                  className="w-full h-10 px-3 rounded-lg bg-surface-container-low border border-outline-variant text-xs text-on-surface"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                  Latitud
                </label>
                <input
                  type="text"
                  value={customLat}
                  onChange={(e) => setCustomLat(e.target.value)}
                  className="w-full h-10 px-3 rounded-lg bg-surface-container-low border border-outline-variant text-xs font-mono text-on-surface"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                  Longitud
                </label>
                <input
                  type="text"
                  value={customLng}
                  onChange={(e) => setCustomLng(e.target.value)}
                  className="w-full h-10 px-3 rounded-lg bg-surface-container-low border border-outline-variant text-xs font-mono text-on-surface"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                  Batería (%)
                </label>
                <input
                  type="number"
                  value={customBattery}
                  onChange={(e) => setCustomBattery(e.target.value)}
                  className="w-full h-10 px-3 rounded-lg bg-surface-container-low border border-outline-variant text-xs text-on-surface"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-outline block mb-1">
                  Precisión GPS (m)
                </label>
                <input
                  type="number"
                  value={customAccuracy}
                  onChange={(e) => setCustomAccuracy(e.target.value)}
                  className="w-full h-10 px-3 rounded-lg bg-surface-container-low border border-outline-variant text-xs text-on-surface"
                />
              </div>
            </div>

            <button
              onClick={handleCustomIngest}
              className="w-full min-h-[48px] bg-primary text-white rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-2 active:scale-[0.99] transition-transform font-headline text-base"
              type="button"
            >
              <span className="material-symbols-outlined text-[20px]">send</span>
              <span>Enviar Posición al Backend</span>
            </button>
          </div>

          {/* Last JSON Payload Inspector */}
          {lastIngestedPayload && (
            <div className="bg-[#191c18] text-[#a7e9a5] rounded-xl p-4 font-mono text-xs overflow-x-auto shadow-md border border-outline">
              <span className="text-white text-[11px] font-bold block mb-1 font-sans">
                Último PositionRequest (Sección 18):
              </span>
              <pre>{JSON.stringify(lastIngestedPayload, null, 2)}</pre>
            </div>
          )}
        </div>
      )}

      {activeSection === 'rules' && (
        <div className="flex flex-col gap-4 animate-in fade-in">
          {/* Rules Configuration (AlertProperties) */}
          <div className="bg-surface-container-lowest rounded-xl p-4 shadow-xs border border-outline-variant/40">
            <h3 className="font-headline text-xl uppercase font-bold text-on-surface mb-2 flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]">tune</span>
              <span>Parámetros de Reglas (AlertProperties)</span>
            </h3>
            <p className="text-xs text-on-surface-variant mb-4">
              Mapeados desde <code className="bg-surface-container px-1 py-0.5 rounded text-primary font-mono text-[11px]">application.yml</code> (Sección 19):
            </p>

            <div className="flex flex-col gap-3.5">
              <div>
                <div className="flex justify-between text-xs font-bold mb-1">
                  <span>Factor Sin Señal (no-signal-factor):</span>
                  <span className="text-primary font-headline text-base">{alertProperties.noSignalFactor}x intervalo</span>
                </div>
                <input
                  type="range"
                  min="2"
                  max="6"
                  value={alertProperties.noSignalFactor}
                  onChange={(e) =>
                    onUpdateAlertProperties({
                      ...alertProperties,
                      noSignalFactor: parseInt(e.target.value, 10),
                    })
                  }
                  className="w-full accent-primary"
                />
                <span className="text-[11px] text-outline">
                  Alerta cuando pasa más de {alertProperties.noSignalFactor * 60}s sin recibir señal.
                </span>
              </div>

              <div>
                <div className="flex justify-between text-xs font-bold mb-1">
                  <span>Umbral Batería Baja (low-battery-percent):</span>
                  <span className="text-primary font-headline text-base">{alertProperties.lowBatteryPercent}%</span>
                </div>
                <input
                  type="range"
                  min="10"
                  max="35"
                  value={alertProperties.lowBatteryPercent}
                  onChange={(e) =>
                    onUpdateAlertProperties({
                      ...alertProperties,
                      lowBatteryPercent: parseInt(e.target.value, 10),
                    })
                  }
                  className="w-full accent-primary"
                />
              </div>

              <div>
                <div className="flex justify-between text-xs font-bold mb-1">
                  <span>Horas de Inmovilidad (idle-hours):</span>
                  <span className="text-primary font-headline text-base">{alertProperties.idleHours} horas</span>
                </div>
                <input
                  type="range"
                  min="2"
                  max="12"
                  value={alertProperties.idleHours}
                  onChange={(e) =>
                    onUpdateAlertProperties({
                      ...alertProperties,
                      idleHours: parseInt(e.target.value, 10),
                    })
                  }
                  className="w-full accent-primary"
                />
              </div>

              <div>
                <div className="flex justify-between text-xs font-bold mb-1">
                  <span>Filtro Precisión Máxima (min-accuracy-meters):</span>
                  <span className="text-primary font-headline text-base">{alertProperties.minAccuracyMeters} m</span>
                </div>
                <input
                  type="range"
                  min="20"
                  max="100"
                  step="5"
                  value={alertProperties.minAccuracyMeters}
                  onChange={(e) =>
                    onUpdateAlertProperties({
                      ...alertProperties,
                      minAccuracyMeters: parseInt(e.target.value, 10),
                    })
                  }
                  className="w-full accent-primary"
                />
                <span className="text-[11px] text-outline">
                  Lecturas con error mayor a {alertProperties.minAccuracyMeters}m se descartan para evitar falsas alarmas.
                </span>
              </div>

              <div>
                <div className="flex justify-between text-xs font-bold mb-1">
                  <span>Confirmaciones de Salida (exit-confirmations):</span>
                  <span className="text-primary font-headline text-base">{alertProperties.exitConfirmations} lecturas</span>
                </div>
                <input
                  type="range"
                  min="1"
                  max="4"
                  value={alertProperties.exitConfirmations}
                  onChange={(e) =>
                    onUpdateAlertProperties({
                      ...alertProperties,
                      exitConfirmations: parseInt(e.target.value, 10),
                    })
                  }
                  className="w-full accent-primary"
                />
              </div>
            </div>
          </div>
        </div>
      )}

      {activeSection === 'architecture' && (
        <div className="flex flex-col gap-3 animate-in fade-in">
          <div className="bg-surface-container-lowest rounded-xl p-4 shadow-xs border border-outline-variant/40">
            <h3 className="font-headline text-xl uppercase font-bold text-on-surface mb-2">
              Estructura por Capas (Clean Architecture)
            </h3>
            <p className="text-xs text-on-surface-variant mb-3 leading-relaxed">
              Diseño desacoplado según el estándar Java 21 POO:
            </p>

            <div className="flex flex-col gap-2 font-mono text-xs">
              <div className="p-3 bg-surface-container-low rounded-lg border border-outline-variant/40">
                <span className="text-primary font-bold block mb-1 font-sans">1. Capa domain:</span>
                <span className="text-on-surface text-[11px]">
                  GeoPoint, Position, Animal, Tracker, Geofence (Polygon & Circle), AlertEvent (sealed interface), AlertRule.
                </span>
              </div>

              <div className="p-3 bg-surface-container-low rounded-lg border border-outline-variant/40">
                <span className="text-primary font-bold block mb-1 font-sans">2. Capa application:</span>
                <span className="text-on-surface text-[11px]">
                  PositionService (ingesta e idempotencia), RuleEngine, AlertService, NotificationDispatcher, ZoneService.
                </span>
              </div>

              <div className="p-3 bg-surface-container-low rounded-lg border border-outline-variant/40">
                <span className="text-primary font-bold block mb-1 font-sans">3. Capa infrastructure:</span>
                <span className="text-on-surface text-[11px]">
                  PostgreSQL + PostGIS (consultas espaciales), WebSocket LivePositionPublisher, FCM push, Watchdog.
                </span>
              </div>

              <div className="p-3 bg-surface-container-low rounded-lg border border-outline-variant/40">
                <span className="text-primary font-bold block mb-1 font-sans">4. Capa api:</span>
                <span className="text-on-surface text-[11px]">
                  REST Controllers (/api/ingest/positions, /api/animals, /api/zones, /api/alerts), JWT Auth.
                </span>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeSection === 'reports' && (
        <div className="flex flex-col gap-4 animate-in fade-in">
          <div className="bg-surface-container-lowest rounded-xl p-4 shadow-xs border border-outline-variant/40">
            <h3 className="font-headline text-xl uppercase font-bold text-on-surface mb-2 flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]">assessment</span>
              <span>Reporte de Salidas y Estado del Hato</span>
            </h3>
            <p className="text-xs text-on-surface-variant mb-4 leading-relaxed">
              Descargue un informe consolidado de movimientos, salidas de potrero y atenciones en campo (PDF/JSON).
            </p>

            <div className="p-3 bg-surface-container-low rounded-xl border border-outline-variant/40 mb-4 text-xs">
              <div className="flex justify-between py-1 border-b border-outline-variant/30">
                <span className="text-outline">Finca:</span>
                <span className="font-bold">{currentFarm.name}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-outline-variant/30">
                <span className="text-outline">Total cabezas:</span>
                <span className="font-bold">{animals.length}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-outline-variant/30">
                <span className="text-outline">Alertas activas:</span>
                <span className="font-bold text-error">{alerts.filter((a) => a.status === 'PENDING').length}</span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-outline">Novedades resueltas (48h):</span>
                <span className="font-bold text-primary">18</span>
              </div>
            </div>

            <button
              onClick={handleExportReport}
              disabled={isExporting}
              className="w-full min-h-[48px] bg-primary text-white rounded-xl text-xs uppercase tracking-wider font-bold flex items-center justify-center gap-2 active:scale-[0.99] transition-transform font-headline text-base"
              type="button"
            >
              <span className="material-symbols-outlined text-[20px]">download</span>
              <span>{isExporting ? 'Generando...' : 'Descargar Reporte JSON / PDF'}</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
