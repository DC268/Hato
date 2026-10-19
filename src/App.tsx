/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * HATO - Ganadería Inteligente y Monitoreo GPS
 */

import React, { useState, useEffect, useRef } from 'react';
import {
  Animal,
  Farm,
  Geofence,
  Position,
  Alert,
  AlertProperties,
  PositionRequest,
  GeoPoint,
} from './types/domain';
import {
  INITIAL_FARM,
  SECONDARY_FARM,
  INITIAL_ZONES,
  INITIAL_ANIMALS,
  INITIAL_POSITIONS,
  INITIAL_ALERTS,
  INITIAL_HISTORY_ALERTS,
} from './data/seedData';
import {
  ExitConfirmationPolicy,
  OutOfZoneRule,
  BackInZoneRule,
  LowBatteryRule,
  SpeedRule,
  AlertDeduplicator,
  NotificationDispatcher,
} from './services/hatoEngine';
import { Header } from './components/Header';
import { BottomNav } from './components/BottomNav';
import { MapScreen } from './components/MapScreen';
import { AnimalsScreen } from './components/AnimalsScreen';
import { ZonesScreen } from './components/ZonesScreen';
import { AlertsScreen } from './components/AlertsScreen';
import { BackendInspectorScreen } from './components/BackendInspectorScreen';
import { AnimalDetailModal } from './components/AnimalDetailModal';
import { AddAnimalModal } from './components/AddAnimalModal';
import { CreateZoneModal } from './components/CreateZoneModal';
import { QrScannerModal } from './components/QrScannerModal';
import { Toast } from './components/Toast';

export default function App() {
  const [currentTab, setCurrentTab] = useState<'mapa' | 'animales' | 'zonas' | 'alertas' | 'mas'>('mapa');
  const [currentFarm, setCurrentFarm] = useState<Farm>(INITIAL_FARM);
  const [userRole, setUserRole] = useState<string>('Capataz');
  const [animals, setAnimals] = useState<Animal[]>(INITIAL_ANIMALS);
  const [zones, setZones] = useState<Geofence[]>(INITIAL_ZONES);
  const [positions, setPositions] = useState<Record<string, Position>>(INITIAL_POSITIONS);
  const [alerts, setAlerts] = useState<Alert[]>(INITIAL_ALERTS);
  const [historyAlerts, setHistoryAlerts] = useState<Alert[]>(INITIAL_HISTORY_ALERTS);
  const [selectedAnimal, setSelectedAnimal] = useState<Animal | null>(INITIAL_ANIMALS[0]);
  const [lastIngestedPayload, setLastIngestedPayload] = useState<PositionRequest | null>(null);

  // Alert Properties (Configurable via application.yml / Inspector)
  const [alertProperties, setAlertProperties] = useState<AlertProperties>({
    noSignalFactor: 3,
    lowBatteryPercent: 20,
    idleHours: 6,
    minAccuracyMeters: 50,
    exitConfirmations: 2,
  });

  // Modal States
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [detailAnimal, setDetailAnimal] = useState<Animal | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showCreateZoneModal, setShowCreateZoneModal] = useState(false);
  const [showScannerModal, setShowScannerModal] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Ingestion history map for idempotency (key: trackerCode + recordedAt)
  const ingestedKeys = useRef<Set<string>>(new Set());

  // Services
  const dispatcherRef = useRef(new NotificationDispatcher());
  const deduplicatorRef = useRef(new AlertDeduplicator());

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage((current) => (current === msg ? null : current));
    }, 3200);
  };

  // Toggle Geofence active/inactive
  const handleToggleZone = (zoneId: string) => {
    setZones((prev) =>
      prev.map((z) => {
        if (z.id === zoneId) {
          z.active = !z.active;
          showToast(`Geocerca "${z.name}" ${z.active ? 'activada' : 'pausada temporalmente'}`);
        }
        return z;
      })
    );
  };

  // Add new animal
  const handleAddAnimal = (newAnimalData: Partial<Animal>) => {
    const newAnimal: Animal = {
      id: `a-${Date.now()}`,
      farmId: currentFarm.id,
      name: newAnimalData.name || 'Nuevo Animal',
      tagNumber: newAnimalData.tagNumber || `#0${animals.length + 1}`,
      displayNumber: newAnimalData.displayNumber || `${animals.length + 1}`,
      breed: newAnimalData.breed || 'Holstein',
      ageYears: newAnimalData.ageYears || 3,
      trackerId: newAnimalData.trackerId || `TRK-${Date.now()}`,
      currentZoneId: newAnimalData.currentZoneId || 'zone-norte',
      healthStatus: 'HEALTHY',
    };

    setAnimals((prev) => [newAnimal, ...prev]);

    // Initial position inside its assigned zone
    setPositions((prev) => ({
      ...prev,
      [newAnimal.id]: {
        trackerId: newAnimal.trackerId,
        point: new GeoPoint(10.4690, -84.6440),
        recordedAt: new Date().toISOString(),
        receivedAt: new Date().toISOString(),
        speedKmh: 1.0,
        batteryPercent: 100,
        accuracyMeters: 5.0,
      },
    }));

    showToast(`Animal ${newAnimal.name} (${newAnimal.tagNumber}) registrado con éxito`);
  };

  // Create new zone
  const handleCreateZone = (newZone: Geofence) => {
    setZones((prev) => [...prev, newZone]);
  };

  // Acknowledge / attend alert
  const handleAcknowledgeAlert = (alertId: string, notes?: string) => {
    const alert = alerts.find((a) => a.id === alertId);
    if (!alert) return;

    setAlerts((prev) => prev.filter((a) => a.id !== alertId));

    const resolvedAlert: Alert = {
      ...alert,
      status: 'RESOLVED',
      attendedBy: userRole,
      attendedAt: 'Hace instantes',
      resolutionNotes: notes,
    };

    setHistoryAlerts((prev) => [resolvedAlert, ...prev]);

    // If it was the critical exit alert for Vaca 03, adjust animal state
    if (alert.animalId === 'a-03') {
      setAnimals((prev) =>
        prev.map((a) => (a.id === 'a-03' ? { ...a, healthStatus: 'HEALTHY' } : a))
      );
    }
  };

  // Position Ingestion Engine (mirroring PositionService.ingest)
  const handleSimulateIngest = (req: PositionRequest) => {
    const dedupeKey = `${req.trackerCode}_${req.recordedAt}`;
    if (ingestedKeys.current.has(dedupeKey)) {
      // Idempotency: duplicate discarded without error
      return;
    }
    ingestedKeys.current.add(dedupeKey);
    setLastIngestedPayload(req);

    // Find animal by tracker code
    const animal = animals.find((a) => a.trackerId === req.trackerCode);
    if (!animal) return;

    const previousPos = positions[animal.id];
    const newPos: Position = {
      trackerId: req.trackerCode,
      point: new GeoPoint(req.lat, req.lng),
      recordedAt: req.recordedAt,
      receivedAt: new Date().toISOString(),
      speedKmh: req.speedKmh ?? 0,
      batteryPercent: req.battery ?? previousPos?.batteryPercent ?? 80,
      accuracyMeters: req.accuracy ?? 6.0,
    };

    setPositions((prev) => ({
      ...prev,
      [animal.id]: newPos,
    }));

    // Setup Rule Engine & Policies
    const policy = new ExitConfirmationPolicy(
      alertProperties.exitConfirmations,
      alertProperties.minAccuracyMeters
    );

    const outRule = new OutOfZoneRule(policy);
    const backRule = new BackInZoneRule();
    const batteryRule = new LowBatteryRule(alertProperties.lowBatteryPercent);
    const speedRule = new SpeedRule(25);

    const context = {
      animal,
      current: newPos,
      previous: previousPos,
      zones: zones.filter((z) => z.active),
      recentReadings: [newPos, previousPos].filter(Boolean) as Position[],
    };

    // Evaluate Out of Zone
    const outEvents = outRule.evaluate(context);
    for (const ev of outEvents) {
      if (deduplicatorRef.current.shouldEmit(ev, 30)) {
        dispatcherRef.current.playFieldAlarm(ev.severity);
        const msg = dispatcherRef.current.createNotificationMessage(ev, animal.name);

        const newAlert: Alert = {
          id: `al-${Date.now()}`,
          animalId: animal.id,
          animalName: animal.name,
          tagNumber: animal.tagNumber,
          zoneId: ev.zoneId,
          zoneName: ev.metadata?.zoneName as string,
          type: 'EXITED_ZONE',
          severity: 'HIGH',
          title: msg.title,
          description: msg.body,
          metersOut: ev.metersOut,
          createdAt: new Date().toISOString(),
          status: 'PENDING',
          category: 'salida',
        };

        setAlerts((prev) => [newAlert, ...prev]);
        setAnimals((prev) =>
          prev.map((a) => (a.id === animal.id ? { ...a, healthStatus: 'ALERT' } : a))
        );
      }
    }

    // Evaluate Back in Zone
    const backEvents = backRule.evaluate(context);
    for (const ev of backEvents) {
      dispatcherRef.current.playFieldAlarm('LOW');
      setAnimals((prev) =>
        prev.map((a) => (a.id === animal.id ? { ...a, healthStatus: 'HEALTHY' } : a))
      );
      // Auto-resolve any pending exit alert for this animal
      setAlerts((prev) => {
        const exitAlerts = prev.filter((a) => a.animalId === animal.id && a.type === 'EXITED_ZONE');
        exitAlerts.forEach((a) => {
          setHistoryAlerts((h) => [
            {
              ...a,
              status: 'RESOLVED',
              attendedBy: 'Auto-resuelta (Regreso a zona)',
              attendedAt: 'Hace un momento',
            },
            ...h,
          ]);
        });
        return prev.filter((a) => !(a.animalId === animal.id && a.type === 'EXITED_ZONE'));
      });
      deduplicatorRef.current.clear(animal.id, 'EXITED_ZONE');
    }

    // Evaluate Speed
    const speedEvents = speedRule.evaluate(context);
    for (const ev of speedEvents) {
      if (deduplicatorRef.current.shouldEmit(ev, 30)) {
        dispatcherRef.current.playFieldAlarm('HIGH');
        const msg = dispatcherRef.current.createNotificationMessage(ev, animal.name);
        const newAlert: Alert = {
          id: `al-${Date.now()}`,
          animalId: animal.id,
          animalName: animal.name,
          tagNumber: animal.tagNumber,
          type: 'SPEED',
          severity: 'HIGH',
          title: msg.title,
          description: msg.body,
          createdAt: new Date().toISOString(),
          status: 'PENDING',
          category: 'velocidad',
        };
        setAlerts((prev) => [newAlert, ...prev]);
      }
    }

    // Evaluate Battery
    const batteryEvents = batteryRule.evaluate(context);
    for (const ev of batteryEvents) {
      if (deduplicatorRef.current.shouldEmit(ev, 360)) {
        dispatcherRef.current.playFieldAlarm('MEDIUM');
        const msg = dispatcherRef.current.createNotificationMessage(ev, animal.name);
        const newAlert: Alert = {
          id: `al-${Date.now()}`,
          animalId: animal.id,
          animalName: animal.name,
          tagNumber: animal.tagNumber,
          type: 'LOW_BATTERY',
          severity: 'MEDIUM',
          title: msg.title,
          description: msg.body,
          createdAt: new Date().toISOString(),
          status: 'PENDING',
          category: 'bateria',
        };
        setAlerts((prev) => [newAlert, ...prev]);
      }
    }
  };

  // Navigations across screens
  const handleViewOnMap = (animalId: string) => {
    const a = animals.find((i) => i.id === animalId);
    if (a) setSelectedAnimal(a);
    setCurrentTab('mapa');
  };

  const handleOpenAnimalDetail = (animal: Animal) => {
    setDetailAnimal(animal);
    setShowDetailModal(true);
  };

  const handlePingCollar = (trackerId: string) => {
    showToast(`Comando PING emitido hacia collar ${trackerId} vía enlace LoRa / GSM`);
    if (typeof navigator !== 'undefined' && navigator.vibrate) {
      navigator.vibrate(40);
    }
  };

  const pendingCount = alerts.filter((a) => a.status === 'PENDING').length;

  return (
    <div className="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">
      {/* Top Header */}
      <Header
        currentTab={currentTab}
        userRole={userRole}
        onRoleClick={() => setCurrentTab('mas')}
      />

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col relative w-full pt-16 pb-20 bg-surface max-w-7xl mx-auto">
        {currentTab === 'mapa' && (
          <MapScreen
            currentFarm={currentFarm}
            farms={[INITIAL_FARM, SECONDARY_FARM]}
            onSelectFarm={setCurrentFarm}
            animals={animals}
            positions={positions}
            selectedAnimal={selectedAnimal}
            onSelectAnimal={setSelectedAnimal}
            onOpenAnimalDetail={handleOpenAnimalDetail}
            onOpenScanner={() => setShowScannerModal(true)}
          />
        )}

        {currentTab === 'animales' && (
          <AnimalsScreen
            animals={animals}
            positions={positions}
            onSelectAnimal={setSelectedAnimal}
            onOpenAnimalDetail={handleOpenAnimalDetail}
            onOpenAddModal={() => setShowAddModal(true)}
          />
        )}

        {currentTab === 'zonas' && (
          <ZonesScreen
            currentFarm={currentFarm}
            zones={zones}
            onToggleZone={handleToggleZone}
            onOpenCreateZoneModal={() => setShowCreateZoneModal(true)}
            onViewZoneAnimals={(zoneId) => {
              setCurrentTab('animales');
            }}
            onNavigateToAlert={() => setCurrentTab('alertas')}
          />
        )}

        {currentTab === 'alertas' && (
          <AlertsScreen
            alerts={alerts}
            historyAlerts={historyAlerts}
            onAcknowledgeAlert={handleAcknowledgeAlert}
            onViewOnMap={handleViewOnMap}
            onNavigateToZones={() => setCurrentTab('zonas')}
            onShowToast={showToast}
          />
        )}

        {currentTab === 'mas' && (
          <BackendInspectorScreen
            currentFarm={currentFarm}
            userRole={userRole}
            onRoleChange={setUserRole}
            animals={animals}
            alerts={alerts}
            alertProperties={alertProperties}
            onUpdateAlertProperties={setAlertProperties}
            onSimulateIngest={handleSimulateIngest}
            onShowToast={showToast}
            lastIngestedPayload={lastIngestedPayload}
          />
        )}
      </main>

      {/* Bottom Navigation */}
      <BottomNav
        currentTab={currentTab}
        onTabChange={(tab) => setCurrentTab(tab as any)}
        pendingAlertsCount={pendingCount}
      />

      {/* Modals */}
      {showDetailModal && detailAnimal && (
        <AnimalDetailModal
          animal={detailAnimal}
          position={positions[detailAnimal.id]}
          onClose={() => setShowDetailModal(false)}
          onViewOnMap={handleViewOnMap}
          onPingCollar={handlePingCollar}
        />
      )}

      {showAddModal && (
        <AddAnimalModal
          zones={zones}
          onClose={() => setShowAddModal(false)}
          onAddAnimal={handleAddAnimal}
        />
      )}

      {showCreateZoneModal && (
        <CreateZoneModal
          onClose={() => setShowCreateZoneModal(false)}
          onCreateZone={handleCreateZone}
          onShowToast={showToast}
        />
      )}

      {showScannerModal && (
        <QrScannerModal
          animals={animals}
          onClose={() => setShowScannerModal(false)}
          onAnimalScanned={(animal) => {
            setSelectedAnimal(animal);
            handleOpenAnimalDetail(animal);
          }}
        />
      )}

      {/* Floating Toast */}
      <Toast message={toastMessage} onClose={() => setToastMessage(null)} />
    </div>
  );
}
