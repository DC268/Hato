/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * HATO - Seed Data & State for Finca La Esperanza
 */

import {
  GeoPoint,
  PolygonGeofence,
  CircleGeofence,
  Animal,
  Farm,
  Alert,
  Position,
  Tracker
} from '../types/domain';

// Coordenadas base en Finca La Esperanza
export const INITIAL_FARM: Farm = {
  id: 'farm-01',
  name: 'Finca La Esperanza',
  location: 'San Carlos, Alajuela, Costa Rica',
  totalAreaHa: 142.5,
  ownerName: 'Don Rodrigo Castro',
  center: new GeoPoint(10.4682, -84.6441),
};

export const SECONDARY_FARM: Farm = {
  id: 'farm-02',
  name: 'Hacienda El Porvenir',
  location: 'Chivilcoy, Buenos Aires',
  totalAreaHa: 210.0,
  ownerName: 'Familia Méndez',
  center: new GeoPoint(-34.895, -60.018),
};

// Polígonos de Potreros
export const INITIAL_ZONES = [
  new PolygonGeofence(
    'zone-norte',
    'farm-01',
    'Potrero Norte',
    15, // 15m tolerancia
    [
      new GeoPoint(10.4715, -84.6465),
      new GeoPoint(10.4718, -84.6425),
      new GeoPoint(10.4678, -84.6428),
      new GeoPoint(10.4674, -84.6468),
    ],
    true,
    'Lote Brachiaria · Topografía ondulada',
    18.5,
    1820
  ),
  new PolygonGeofence(
    'zone-sur',
    'farm-01',
    'Potrero Sur (Hato Principal)',
    15,
    [
      new GeoPoint(10.4675, -84.6425),
      new GeoPoint(10.4672, -84.6385),
      new GeoPoint(10.4625, -84.6390),
      new GeoPoint(10.4630, -84.6432),
    ],
    true,
    'Pastoreo intensivo rotacional con sombra nativa',
    24.0,
    2150
  ),
  new PolygonGeofence(
    'zone-represa',
    'farm-01',
    'Potrero La Represa',
    15,
    [
      new GeoPoint(10.4628, -84.6475),
      new GeoPoint(10.4632, -84.6435),
      new GeoPoint(10.4590, -84.6438),
      new GeoPoint(10.4586, -84.6478),
    ],
    true,
    'Acceso directo a bebedero comunal',
    24.0,
    1960
  ),
  new CircleGeofence(
    'zone-corral',
    'farm-01',
    'Corral de Manejo',
    10, // 10m radio tolerancia
    new GeoPoint(10.4652, -84.6430),
    35, // radio 35m
    false, // Inicialmente pausado como en la captura
    'Báscula y área veterinaria',
    0.5,
    220
  ),
];

// 27 animales registrados
export const INITIAL_ANIMALS: Animal[] = [
  {
    id: 'a-03',
    farmId: 'farm-01',
    name: 'Vaca 03',
    tagNumber: '#0342',
    displayNumber: '03',
    breed: 'Holstein',
    ageYears: 4.5,
    weightKg: 560,
    trackerId: 'TRK-0342',
    currentZoneId: 'zone-norte',
    healthStatus: 'ALERT',
    notes: 'Escape activo reportado. Se desplazó hacia la zanja noreste. Revisar lindero.',
  },
  {
    id: 'a-21',
    farmId: 'farm-01',
    name: 'Novillo 21',
    tagNumber: '#0881',
    displayNumber: '21',
    breed: 'Brahman Blanco',
    ageYears: 2.2,
    weightKg: 420,
    trackerId: 'TRK-0881',
    currentZoneId: 'zone-norte',
    healthStatus: 'ATTENTION',
    notes: 'Collar GPS V3 sin señal desde las 14:20. Posible batería agotada o interferencia.',
  },
  {
    id: 'a-12',
    farmId: 'farm-01',
    name: 'Vaca 12',
    tagNumber: '#0112',
    displayNumber: '12',
    breed: 'Girolando',
    ageYears: 5.0,
    weightKg: 530,
    trackerId: 'TRK-0112',
    currentZoneId: 'zone-sur',
    healthStatus: 'HEALTHY',
    notes: 'Excelente condición. Pastando en grupo en Potrero Sur.',
  },
  {
    id: 'a-tb',
    farmId: 'farm-01',
    name: 'Toro Barcino',
    tagNumber: '#0004',
    displayNumber: 'TB',
    breed: 'Brahman Reproductivo',
    ageYears: 6.0,
    weightKg: 890,
    trackerId: 'TRK-0004',
    currentZoneId: 'zone-sur',
    healthStatus: 'HEALTHY',
    notes: 'Semental reproductor del hato principal. Marcador activo.',
  },
  {
    id: 'a-07',
    farmId: 'farm-01',
    name: 'Ternera 07',
    tagNumber: '#0923',
    displayNumber: '07',
    breed: 'Jersey Pura',
    ageYears: 1.5,
    weightKg: 310,
    trackerId: 'TRK-0923',
    currentZoneId: 'zone-represa',
    healthStatus: 'ATTENTION',
    notes: 'Inmóvil hace 6 horas cerca del bebedero central. Posible parto o revisión veterinaria.',
  },
  // Resto del rebaño para completar los 27 animales
  { id: 'a-01', farmId: 'farm-01', name: 'Vaca 01', tagNumber: '#0101', displayNumber: '01', breed: 'Holstein', ageYears: 4, trackerId: 'TRK-0101', currentZoneId: 'zone-sur', healthStatus: 'HEALTHY' },
  { id: 'a-02', farmId: 'farm-01', name: 'Vaca 02', tagNumber: '#0102', displayNumber: '02', breed: 'Holstein', ageYears: 3, trackerId: 'TRK-0102', currentZoneId: 'zone-sur', healthStatus: 'HEALTHY' },
  { id: 'a-04', farmId: 'farm-01', name: 'Novilla 04', tagNumber: '#0104', displayNumber: '04', breed: 'Girolando', ageYears: 2, trackerId: 'TRK-0104', currentZoneId: 'zone-sur', healthStatus: 'HEALTHY' },
  { id: 'a-05', farmId: 'farm-01', name: 'Vaca 05', tagNumber: '#0105', displayNumber: '05', breed: 'Jersey', ageYears: 4, trackerId: 'TRK-0105', currentZoneId: 'zone-norte', healthStatus: 'HEALTHY' },
  { id: 'a-06', farmId: 'farm-01', name: 'Vaca 06', tagNumber: '#0106', displayNumber: '06', breed: 'Holstein', ageYears: 5, trackerId: 'TRK-0106', currentZoneId: 'zone-norte', healthStatus: 'HEALTHY' },
  { id: 'a-08', farmId: 'farm-01', name: 'Novillo 08', tagNumber: '#0108', displayNumber: '08', breed: 'Brahman', ageYears: 2, trackerId: 'TRK-0108', currentZoneId: 'zone-represa', healthStatus: 'HEALTHY' },
  { id: 'a-09', farmId: 'farm-01', name: 'Vaca 09', tagNumber: '#0109', displayNumber: '09', breed: 'Girolando', ageYears: 4, trackerId: 'TRK-0109', currentZoneId: 'zone-represa', healthStatus: 'HEALTHY' },
  { id: 'a-10', farmId: 'farm-01', name: 'Vaca 10', tagNumber: '#0110', displayNumber: '10', breed: 'Holstein', ageYears: 3, trackerId: 'TRK-0110', currentZoneId: 'zone-sur', healthStatus: 'HEALTHY' },
  { id: 'a-11', farmId: 'farm-01', name: 'Novilla 11', tagNumber: '#0111', displayNumber: '11', breed: 'Jersey', ageYears: 2, trackerId: 'TRK-0111', currentZoneId: 'zone-sur', healthStatus: 'HEALTHY' },
  { id: 'a-13', farmId: 'farm-01', name: 'Toro 13', tagNumber: '#0113', displayNumber: '13', breed: 'Simmental', ageYears: 5, trackerId: 'TRK-0113', currentZoneId: 'zone-represa', healthStatus: 'HEALTHY' },
  { id: 'a-14', farmId: 'farm-01', name: 'Novillo 14', tagNumber: '#0114', displayNumber: '14', breed: 'Brahman', ageYears: 2, trackerId: 'TRK-0114', currentZoneId: 'zone-sur', healthStatus: 'HEALTHY' },
  { id: 'a-15', farmId: 'farm-01', name: 'Novilla 15', tagNumber: '#0115', displayNumber: '15', breed: 'Girolando', ageYears: 2, trackerId: 'TRK-0115', currentZoneId: 'zone-norte', healthStatus: 'HEALTHY' },
  { id: 'a-16', farmId: 'farm-01', name: 'Vaca 16', tagNumber: '#0116', displayNumber: '16', breed: 'Holstein', ageYears: 4, trackerId: 'TRK-0116', currentZoneId: 'zone-norte', healthStatus: 'HEALTHY' },
  { id: 'a-17', farmId: 'farm-01', name: 'Vaca 17', tagNumber: '#0117', displayNumber: '17', breed: 'Jersey', ageYears: 3, trackerId: 'TRK-0117', currentZoneId: 'zone-sur', healthStatus: 'HEALTHY' },
  { id: 'a-18', farmId: 'farm-01', name: 'Novillo 18', tagNumber: '#0118', displayNumber: '18', breed: 'Brahman', ageYears: 2, trackerId: 'TRK-0118', currentZoneId: 'zone-represa', healthStatus: 'HEALTHY' },
  { id: 'a-19', farmId: 'farm-01', name: 'Vaca 19', tagNumber: '#0119', displayNumber: '19', breed: 'Holstein', ageYears: 5, trackerId: 'TRK-0119', currentZoneId: 'zone-sur', healthStatus: 'HEALTHY' },
  { id: 'a-20', farmId: 'farm-01', name: 'Vaca 20', tagNumber: '#0120', displayNumber: '20', breed: 'Girolando', ageYears: 4, trackerId: 'TRK-0120', currentZoneId: 'zone-represa', healthStatus: 'HEALTHY' },
  { id: 'a-22', farmId: 'farm-01', name: 'Vaca 22', tagNumber: '#0122', displayNumber: '22', breed: 'Holstein', ageYears: 3, trackerId: 'TRK-0122', currentZoneId: 'zone-norte', healthStatus: 'HEALTHY' },
  { id: 'a-23', farmId: 'farm-01', name: 'Novillo 23', tagNumber: '#0123', displayNumber: '23', breed: 'Brahman', ageYears: 2, trackerId: 'TRK-0123', currentZoneId: 'zone-sur', healthStatus: 'HEALTHY' },
  { id: 'a-24', farmId: 'farm-01', name: 'Vaca 24', tagNumber: '#0124', displayNumber: '24', breed: 'Jersey', ageYears: 4, trackerId: 'TRK-0124', currentZoneId: 'zone-norte', healthStatus: 'HEALTHY' },
  { id: 'a-25', farmId: 'farm-01', name: 'Novilla 25', tagNumber: '#0125', displayNumber: '25', breed: 'Girolando', ageYears: 2, trackerId: 'TRK-0125', currentZoneId: 'zone-represa', healthStatus: 'HEALTHY' },
  { id: 'a-26', farmId: 'farm-01', name: 'Vaca 26', tagNumber: '#0126', displayNumber: '26', breed: 'Holstein', ageYears: 6, trackerId: 'TRK-0126', currentZoneId: 'zone-corral', healthStatus: 'RESTING' },
];

export const INITIAL_TRACKERS: Record<string, Tracker> = {
  'TRK-0342': {
    id: 'trk-0342',
    code: 'TRK-0342',
    batteryPercent: 15,
    lastSeenAt: new Date(Date.now() - 45 * 1000).toISOString(),
    reportIntervalSeconds: 60,
    status: 'LOW_BATTERY',
  },
  'TRK-0881': {
    id: 'trk-0881',
    code: 'TRK-0881',
    batteryPercent: 64,
    lastSeenAt: new Date(Date.now() - 25 * 60 * 1000).toISOString(),
    reportIntervalSeconds: 60,
    status: 'OFFLINE',
  },
  'TRK-0112': {
    id: 'trk-0112',
    code: 'TRK-0112',
    batteryPercent: 92,
    lastSeenAt: new Date(Date.now() - 120 * 1000).toISOString(),
    reportIntervalSeconds: 60,
    status: 'ONLINE',
  },
  'TRK-0004': {
    id: 'trk-0004',
    code: 'TRK-0004',
    batteryPercent: 85,
    lastSeenAt: new Date(Date.now() - 30 * 1000).toISOString(),
    reportIntervalSeconds: 60,
    status: 'ONLINE',
  },
  'TRK-0923': {
    id: 'trk-0923',
    code: 'TRK-0923',
    batteryPercent: 78,
    lastSeenAt: new Date(Date.now() - 90 * 1000).toISOString(),
    reportIntervalSeconds: 60,
    status: 'ONLINE',
  },
};

// Posiciones actuales de los animales
export const INITIAL_POSITIONS: Record<string, Position> = {
  'a-03': {
    trackerId: 'TRK-0342',
    point: new GeoPoint(10.4728, -84.6475), // Fuera de Potrero Norte a 140m
    recordedAt: new Date(Date.now() - 45 * 1000).toISOString(),
    receivedAt: new Date(Date.now() - 44 * 1000).toISOString(),
    speedKmh: 4.0,
    batteryPercent: 15,
    accuracyMeters: 6.2,
  },
  'a-21': {
    trackerId: 'TRK-0881',
    point: new GeoPoint(10.4688, -84.6455), // Cerca de la zanja, sin señal
    recordedAt: new Date(Date.now() - 25 * 60 * 1000).toISOString(),
    receivedAt: new Date(Date.now() - 25 * 60 * 1000).toISOString(),
    speedKmh: 0.0,
    batteryPercent: 64,
    accuracyMeters: 14.5,
  },
  'a-12': {
    trackerId: 'TRK-0112',
    point: new GeoPoint(10.4655, -84.6408), // Dentro de Potrero Sur
    recordedAt: new Date(Date.now() - 120 * 1000).toISOString(),
    receivedAt: new Date(Date.now() - 119 * 1000).toISOString(),
    speedKmh: 1.2,
    batteryPercent: 92,
    accuracyMeters: 4.8,
  },
  'a-tb': {
    trackerId: 'TRK-0004',
    point: new GeoPoint(10.4642, -84.6412), // Dentro de Potrero Sur
    recordedAt: new Date(Date.now() - 30 * 1000).toISOString(),
    receivedAt: new Date(Date.now() - 29 * 1000).toISOString(),
    speedKmh: 1.8,
    batteryPercent: 85,
    accuracyMeters: 5.1,
  },
  'a-07': {
    trackerId: 'TRK-0923',
    point: new GeoPoint(10.4610, -84.6450), // En bebedero de Potrero La Represa
    recordedAt: new Date(Date.now() - 90 * 1000).toISOString(),
    receivedAt: new Date(Date.now() - 89 * 1000).toISOString(),
    speedKmh: 0.0,
    batteryPercent: 78,
    accuracyMeters: 4.0,
  },
};

// Historial de recorrido para Vaca 03 (fuga del potrero hacia la zanja)
export const VACA_03_TRAIL: GeoPoint[] = [
  new GeoPoint(10.4695, -84.6440), // Origen dentro del potrero
  new GeoPoint(10.4705, -84.6448),
  new GeoPoint(10.4712, -84.6455),
  new GeoPoint(10.4717, -84.6462), // Traspaso del lindero
  new GeoPoint(10.4722, -84.6468),
  new GeoPoint(10.4728, -84.6475), // Posición crítica actual (a 140m)
];

// Alertas Iniciales Activas (2 pendientes + 1 advertencia batería)
export const INITIAL_ALERTS: Alert[] = [
  {
    id: 'al-01',
    animalId: 'a-03',
    animalName: 'Vaca 03',
    tagNumber: '#0342',
    zoneId: 'zone-norte',
    zoneName: 'Potrero Norte',
    type: 'EXITED_ZONE',
    severity: 'HIGH',
    title: 'Vaca 03 salió de la zona',
    description: 'El animal se encuentra a 140 metros fuera del lindero del lote. Riesgo: zanja noreste.',
    metersOut: 140,
    createdAt: new Date(Date.now() - 4 * 60 * 1000).toISOString(),
    status: 'PENDING',
    category: 'salida',
  },
  {
    id: 'al-02',
    animalId: 'a-07',
    animalName: 'Ternera 07',
    tagNumber: '#0923',
    zoneId: 'zone-represa',
    zoneName: 'Potrero La Represa',
    type: 'IDLE',
    severity: 'HIGH',
    title: 'Ternera 07 lleva 6 horas quieta',
    description: 'Sin desplazamientos registrados. Podría tratarse de un parto asistido o requerir examen veterinario.',
    createdAt: new Date(Date.now() - 25 * 60 * 1000).toISOString(),
    status: 'PENDING',
    category: 'inactividad',
  },
  {
    id: 'al-03',
    animalId: 'a-03',
    animalName: 'Vaca 03',
    tagNumber: '#0342',
    zoneId: 'zone-norte',
    zoneName: 'Potrero Norte',
    type: 'LOW_BATTERY',
    severity: 'MEDIUM',
    title: 'Batería baja en Vaca 03',
    description: 'Nivel restante del collar GPS al 15%. Reemplace o recargue el collar antes del próximo cambio de potrero.',
    createdAt: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
    status: 'PENDING',
    category: 'bateria',
  },
];

// Historial de 18 alertas resueltas
export const INITIAL_HISTORY_ALERTS: Alert[] = [
  {
    id: 'al-h01',
    animalId: 'a-tb',
    animalName: 'Toro 01',
    tagNumber: '#0001',
    zoneName: 'Potrero Sur',
    type: 'RETURNED_TO_ZONE',
    severity: 'LOW',
    title: 'Toro 01 regresó al Potrero Sur',
    description: 'El animal reingresó voluntariamente al potrero asignado tras arreo.',
    createdAt: new Date(Date.now() - 24 * 3600 * 1000).toISOString(),
    status: 'RESOLVED',
    attendedBy: 'Capataz Luis',
    attendedAt: 'Ayer 17:40',
    category: 'salida',
  },
  {
    id: 'al-h02',
    animalId: 'a-14',
    animalName: 'Novillo 14',
    tagNumber: '#0114',
    zoneName: 'Potrero Sur',
    type: 'LOW_BATTERY',
    severity: 'MEDIUM',
    title: 'Batería reemplazada en Novillo 14',
    description: 'Se instaló collar con carga al 100% en corral de manejo.',
    createdAt: new Date(Date.now() - 28 * 3600 * 1000).toISOString(),
    status: 'RESOLVED',
    attendedBy: 'Usted',
    attendedAt: 'Ayer 11:15',
    category: 'bateria',
  },
  {
    id: 'al-h03',
    animalId: 'a-19',
    animalName: 'Vaca 19',
    tagNumber: '#0119',
    zoneName: 'Potrero Sur',
    type: 'IDLE',
    severity: 'MEDIUM',
    title: 'Vaca 19 en movimiento normal',
    description: 'Se verificó actividad ruminal normal tras descanso bajo arboleda.',
    createdAt: new Date(Date.now() - 48 * 3600 * 1000).toISOString(),
    status: 'RESOLVED',
    attendedBy: 'Auto-resuelta',
    attendedAt: 'Hace 2 días',
    category: 'inactividad',
  },
  {
    id: 'al-h04',
    animalId: 'a-08',
    animalName: 'Novillo 08',
    tagNumber: '#0108',
    zoneName: 'Potrero La Represa',
    type: 'RETURNED_TO_ZONE',
    severity: 'LOW',
    title: 'Novillo 08 dentro de lindero',
    description: 'Retornado al lote tras breve traspaso en puerta de abrevadero.',
    createdAt: new Date(Date.now() - 52 * 3600 * 1000).toISOString(),
    status: 'RESOLVED',
    attendedBy: 'Capataz Luis',
    attendedAt: 'Hace 2 días',
    category: 'salida',
  },
  {
    id: 'al-h05',
    animalId: 'a-12',
    animalName: 'Vaca 12',
    tagNumber: '#0112',
    zoneName: 'Potrero Norte',
    type: 'RETURNED_TO_ZONE',
    severity: 'LOW',
    title: 'Vaca 12 pastando estable',
    description: 'Lectura verificada tras calibración de tolerancia de 15m.',
    createdAt: new Date(Date.now() - 72 * 3600 * 1000).toISOString(),
    status: 'RESOLVED',
    attendedBy: 'Sistema',
    attendedAt: 'Hace 3 días',
    category: 'salida',
  },
];
