/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * HATO - Domain Model & Rules Engine (Java 21 / POO Architecture translated to TypeScript)
 */

export class GeoPoint {
  public readonly lat: number;
  public readonly lng: number;

  constructor(lat: number, lng: number) {
    if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
      throw new Error(`Coordenadas fuera de rango: lat=${lat}, lng=${lng}`);
    }
    this.lat = lat;
    this.lng = lng;
  }

  /**
   * Distancia en metros usando la fórmula de Haversine.
   */
  public distanceMetersTo(other: GeoPoint): number {
    const r = 6371000; // Radio de la Tierra en metros
    const dLat = ((other.lat - this.lat) * Math.PI) / 180;
    const dLng = ((other.lng - this.lng) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((this.lat * Math.PI) / 180) *
        Math.cos((other.lat * Math.PI) / 180) *
        Math.sin(dLng / 2) *
        Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return r * c;
  }
}

export interface Position {
  trackerId: string;
  point: GeoPoint;
  recordedAt: string; // ISO String (hora registrada por el GPS)
  receivedAt: string;
  speedKmh: number;
  batteryPercent: number;
  accuracyMeters: number;
}

export interface Tracker {
  id: string;
  code: string; // IMEI o código físico
  batteryPercent: number;
  lastSeenAt: string;
  reportIntervalSeconds: number;
  status: 'ONLINE' | 'OFFLINE' | 'LOW_BATTERY';
}

export interface Animal {
  id: string;
  farmId: string;
  name: string;
  tagNumber: string; // Arete e.g. #0342
  displayNumber: string; // 03, 21, 12, TB, 07
  breed: string;
  ageYears: number;
  weightKg?: number;
  photoUrl?: string;
  notes?: string;
  trackerId: string;
  currentZoneId?: string;
  healthStatus: 'HEALTHY' | 'ALERT' | 'ATTENTION' | 'RESTING';
}

export interface Farm {
  id: string;
  name: string;
  location: string;
  totalAreaHa: number;
  ownerName: string;
  center: GeoPoint;
}

// -------------------------------------------------------------
// Geofence Hierarchy (Polymorphic Pattern)
// -------------------------------------------------------------

export abstract class Geofence {
  public readonly id: string;
  public readonly farmId: string;
  public readonly name: string;
  public readonly toleranceMeters: number;
  public active: boolean;
  public description: string;
  public areaHa: number;
  public perimeterMeters: number;

  constructor(
    id: string,
    farmId: string,
    name: string,
    toleranceMeters: number = 10,
    active: boolean = true,
    description: string = '',
    areaHa: number = 0,
    perimeterMeters: number = 0
  ) {
    this.id = id;
    this.farmId = farmId;
    this.name = name;
    this.toleranceMeters = toleranceMeters;
    this.active = active;
    this.description = description;
    this.areaHa = areaHa;
    this.perimeterMeters = perimeterMeters;
  }

  public abstract contains(p: GeoPoint): boolean;
  public abstract distanceToBoundaryMeters(p: GeoPoint): number;

  /**
   * Fuera de verdad: salió y superó el margen de tolerancia del GPS.
   */
  public isOutside(p: GeoPoint): boolean {
    if (!this.active) return false;
    return !this.contains(p) && this.distanceToBoundaryMeters(p) > this.toleranceMeters;
  }
}

export class CircleGeofence extends Geofence {
  public readonly center: GeoPoint;
  public readonly radiusMeters: number;

  constructor(
    id: string,
    farmId: string,
    name: string,
    toleranceMeters: number,
    center: GeoPoint,
    radiusMeters: number,
    active: boolean = true,
    description: string = '',
    areaHa: number = 0.5,
    perimeterMeters: number = 220
  ) {
    super(id, farmId, name, toleranceMeters, active, description, areaHa, perimeterMeters);
    this.center = center;
    this.radiusMeters = radiusMeters;
  }

  public contains(p: GeoPoint): boolean {
    return this.center.distanceMetersTo(p) <= this.radiusMeters;
  }

  public distanceToBoundaryMeters(p: GeoPoint): number {
    return Math.abs(this.center.distanceMetersTo(p) - this.radiusMeters);
  }
}

export class PolygonGeofence extends Geofence {
  public readonly points: GeoPoint[];

  constructor(
    id: string,
    farmId: string,
    name: string,
    toleranceMeters: number,
    points: GeoPoint[],
    active: boolean = true,
    description: string = '',
    areaHa: number = 0,
    perimeterMeters: number = 0
  ) {
    if (points.length < 3) {
      throw new Error(`PolygonGeofence requiere al menos 3 vértices, recibido: ${points.length}`);
    }
    super(id, farmId, name, toleranceMeters, active, description, areaHa, perimeterMeters);
    this.points = points;
  }

  /**
   * Ray casting algorithm (Punto en polígono)
   */
  public contains(p: GeoPoint): boolean {
    let inside = false;
    const n = this.points.length;
    for (let i = 0, j = n - 1; i < n; j = i++) {
      const xi = this.points[i].lng;
      const yi = this.points[i].lat;
      const xj = this.points[j].lng;
      const yj = this.points[j].lat;

      const intersect =
        yi > p.lat !== yj > p.lat &&
        p.lng < ((xj - xi) * (p.lat - yi)) / (yj - yi) + xi;
      if (intersect) inside = !inside;
    }
    return inside;
  }

  /**
   * Distancia mínima a cualquiera de los bordes del polígono
   */
  public distanceToBoundaryMeters(p: GeoPoint): number {
    let minDistance = Infinity;
    const n = this.points.length;
    for (let i = 0; i < n; i++) {
      const j = (i + 1) % n;
      const p1 = this.points[i];
      const p2 = this.points[j];
      const dist = this.pointToSegmentDistance(p, p1, p2);
      if (dist < minDistance) {
        minDistance = dist;
      }
    }
    return minDistance;
  }

  private pointToSegmentDistance(p: GeoPoint, p1: GeoPoint, p2: GeoPoint): number {
    const d1 = p1.distanceMetersTo(p2);
    if (d1 === 0) return p.distanceMetersTo(p1);

    // Proyección en coordenadas locales simplificadas
    const dx = p2.lng - p1.lng;
    const dy = p2.lat - p1.lat;
    const t = Math.max(0, Math.min(1, ((p.lng - p1.lng) * dx + (p.lat - p1.lat) * dy) / (dx * dx + dy * dy)));
    const proj = new GeoPoint(p1.lat + t * dy, p1.lng + t * dx);
    return p.distanceMetersTo(proj);
  }
}

// -------------------------------------------------------------
// Alert Event & Rule Hierarchy
// -------------------------------------------------------------

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type AlertType =
  | 'EXITED_ZONE'
  | 'RETURNED_TO_ZONE'
  | 'LOW_BATTERY'
  | 'NO_SIGNAL'
  | 'IDLE'
  | 'SPEED'
  | 'MASS_EXIT';

export interface AlertEvent {
  type: AlertType;
  animalId: string;
  zoneId?: string;
  at: string;
  severity: Severity;
  metersOut?: number;
  batteryPercent?: number;
  silentForMs?: number;
  idleForMs?: number;
  speedKmh?: number;
  metadata?: Record<string, unknown>;
}

export interface AlertContext {
  animal: Animal;
  current: Position;
  previous?: Position;
  zones: Geofence[];
  recentReadings: Position[];
}

export interface AlertRule {
  evaluate(ctx: AlertContext): AlertEvent[];
}

export interface Alert {
  id: string;
  animalId: string;
  animalName: string;
  tagNumber: string;
  zoneId?: string;
  zoneName?: string;
  type: AlertType;
  severity: Severity;
  title: string;
  description: string;
  metersOut?: number;
  createdAt: string;
  status: 'PENDING' | 'RESOLVED';
  attendedBy?: string;
  attendedAt?: string;
  resolutionNotes?: string;
  category: 'salida' | 'inactividad' | 'bateria' | 'velocidad';
}

export interface AlertProperties {
  noSignalFactor: number; // Por defecto 3 veces el intervalo
  lowBatteryPercent: number; // Por defecto 20%
  idleHours: number; // Por defecto 6 horas
  minAccuracyMeters: number; // Por defecto 50 metros
  exitConfirmations: number; // Por defecto 2 lecturas consecutivas
}

export interface PositionRequest {
  trackerCode: string;
  lat: number;
  lng: number;
  recordedAt: string;
  speedKmh?: number;
  battery?: number;
  accuracy?: number;
}
