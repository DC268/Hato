/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * HATO - Rules Engine & Services Implementation
 * Faithfully mirrors the Java 21 Spring Boot POO Backend Specification
 */

import {
  GeoPoint,
  Position,
  Animal,
  Geofence,
  Alert,
  AlertEvent,
  AlertContext,
  AlertRule,
  AlertProperties,
  PositionRequest,
  Tracker,
  Severity
} from '../types/domain';

// -------------------------------------------------------------
// Exit Confirmation Policy (Section 16.4: Menos falsas alarmas)
// -------------------------------------------------------------
export class ExitConfirmationPolicy {
  public readonly required: number; // lecturas consecutivas fuera, por defecto 2
  public readonly maxAccuracyMeters: number; // máximo error aceptable, por defecto 50m

  constructor(required: number = 2, maxAccuracyMeters: number = 50) {
    this.required = required;
    this.maxAccuracyMeters = maxAccuracyMeters;
  }

  public isUsable(p: Position): boolean {
    return p.accuracyMeters <= this.maxAccuracyMeters;
  }

  public confirmed(lastReadings: Position[], zone: Geofence): boolean {
    const usableReadings = lastReadings.filter((r) => this.isUsable(r));
    if (usableReadings.length < this.required) {
      return false;
    }
    return usableReadings
      .slice(0, this.required)
      .every((r) => zone.isOutside(r.point));
  }
}

// -------------------------------------------------------------
// Alert Rules (Section 7.3 & 9)
// -------------------------------------------------------------

export class OutOfZoneRule implements AlertRule {
  private policy: ExitConfirmationPolicy;

  constructor(policy: ExitConfirmationPolicy) {
    this.policy = policy;
  }

  public evaluate(ctx: AlertContext): AlertEvent[] {
    const now = ctx.current.point;
    const events: AlertEvent[] = [];

    for (const zone of ctx.zones) {
      if (!zone.active) continue;

      const isOutsideNow = zone.isOutside(now);
      const wasOutsideBefore = ctx.previous ? zone.isOutside(ctx.previous.point) : false;

      // Solo dispara en la transición de dentro a fuera (o primera lectura con confirmación)
      if (isOutsideNow && !wasOutsideBefore) {
        const confirmed = this.policy.confirmed(ctx.recentReadings, zone);
        if (confirmed) {
          const distanceOut = Math.round(zone.distanceToBoundaryMeters(now));
          events.push({
            type: 'EXITED_ZONE',
            animalId: ctx.animal.id,
            zoneId: zone.id,
            at: ctx.current.recordedAt,
            severity: 'HIGH',
            metersOut: distanceOut,
            metadata: {
              zoneName: zone.name,
              accuracy: ctx.current.accuracyMeters,
            },
          });
        }
      }
    }

    return events;
  }
}

export class BackInZoneRule implements AlertRule {
  public evaluate(ctx: AlertContext): AlertEvent[] {
    const now = ctx.current.point;
    const events: AlertEvent[] = [];

    for (const zone of ctx.zones) {
      if (!zone.active) continue;

      const isInsideNow = zone.contains(now);
      const wasOutsideBefore = ctx.previous ? zone.isOutside(ctx.previous.point) : false;

      if (isInsideNow && wasOutsideBefore) {
        events.push({
          type: 'RETURNED_TO_ZONE',
          animalId: ctx.animal.id,
          zoneId: zone.id,
          at: ctx.current.recordedAt,
          severity: 'LOW',
          metadata: { zoneName: zone.name },
        });
      }
    }

    return events;
  }
}

export class LowBatteryRule implements AlertRule {
  private thresholdPercent: number;

  constructor(thresholdPercent: number = 20) {
    this.thresholdPercent = thresholdPercent;
  }

  public evaluate(ctx: AlertContext): AlertEvent[] {
    if (ctx.current.batteryPercent < this.thresholdPercent) {
      return [
        {
          type: 'LOW_BATTERY',
          animalId: ctx.animal.id,
          at: ctx.current.recordedAt,
          severity: 'MEDIUM',
          batteryPercent: ctx.current.batteryPercent,
        },
      ];
    }
    return [];
  }
}

export class SpeedRule implements AlertRule {
  private maxAllowedKmh: number;

  constructor(maxAllowedKmh: number = 25) {
    this.maxAllowedKmh = maxAllowedKmh;
  }

  public evaluate(ctx: AlertContext): AlertEvent[] {
    if (ctx.current.speedKmh > this.maxAllowedKmh) {
      return [
        {
          type: 'SPEED',
          animalId: ctx.animal.id,
          at: ctx.current.recordedAt,
          severity: 'HIGH',
          speedKmh: ctx.current.speedKmh,
          metadata: { note: 'Posible traslado vehicular de ganado' },
        },
      ];
    }
    return [];
  }
}

// -------------------------------------------------------------
// Alert Deduplicator (Section 9)
// -------------------------------------------------------------
export class AlertDeduplicator {
  private lastAlertTimestamp: Map<string, number> = new Map();

  public shouldEmit(event: AlertEvent, cooldownMinutes: number = 360): boolean {
    const key = `${event.animalId}_${event.type}_${event.zoneId || 'all'}`;
    const now = Date.now();
    const last = this.lastAlertTimestamp.get(key) || 0;

    if (now - last > cooldownMinutes * 60 * 1000) {
      this.lastAlertTimestamp.set(key, now);
      return true;
    }
    return false;
  }

  public clear(animalId: string, type: string, zoneId?: string): void {
    const key = `${animalId}_${type}_${zoneId || 'all'}`;
    this.lastAlertTimestamp.delete(key);
  }
}

// -------------------------------------------------------------
// Notification Dispatcher & Audio Synthesizer (Section 7.4)
// -------------------------------------------------------------
export class NotificationDispatcher {
  private audioCtx: AudioContext | null = null;

  public playFieldAlarm(severity: Severity) {
    try {
      if (!this.audioCtx) {
        const AudioClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
        if (AudioClass) {
          this.audioCtx = new AudioClass();
        }
      }
      if (this.audioCtx && this.audioCtx.state === 'suspended') {
        this.audioCtx.resume();
      }
      if (this.audioCtx) {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        if (severity === 'CRITICAL' || severity === 'HIGH') {
          // Double Urgent Chirp
          osc.type = 'sawtooth';
          osc.frequency.setValueAtTime(880, this.audioCtx.currentTime);
          osc.frequency.exponentialRampToValueAtTime(440, this.audioCtx.currentTime + 0.15);
          gain.gain.setValueAtTime(0.3, this.audioCtx.currentTime);
          gain.gain.linearRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.35);
          osc.start();
          osc.stop(this.audioCtx.currentTime + 0.35);
        } else {
          osc.type = 'sine';
          osc.frequency.setValueAtTime(587.33, this.audioCtx.currentTime); // D5
          gain.gain.setValueAtTime(0.15, this.audioCtx.currentTime);
          gain.gain.linearRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.25);
          osc.start();
          osc.stop(this.audioCtx.currentTime + 0.25);
        }
      }
    } catch {
      // Audio fallback silent
    }

    if (typeof navigator !== 'undefined' && navigator.vibrate) {
      navigator.vibrate(severity === 'HIGH' ? [100, 50, 100] : [50]);
    }
  }

  public createNotificationMessage(event: AlertEvent, animalName: string): { title: string; body: string; channel: string } {
    switch (event.type) {
      case 'EXITED_ZONE':
        return {
          title: `¡${animalName} salió de la zona!`,
          body: `A ${event.metersOut ?? 140} m del límite de ${(event.metadata?.zoneName as string) || 'Potrero'}.`,
          channel: 'urgente',
        };
      case 'RETURNED_TO_ZONE':
        return {
          title: `${animalName} volvió a la zona`,
          body: 'El animal ya se encuentra dentro de los linderos seguros.',
          channel: 'info',
        };
      case 'LOW_BATTERY':
        return {
          title: `Batería baja en ${animalName}`,
          body: `Collar al ${event.batteryPercent ?? 15}%. Programar reemplazo o recarga.`,
          channel: 'mantenimiento',
        };
      case 'NO_SIGNAL':
        return {
          title: `Sin señal en ${animalName}`,
          body: 'El collar no ha emitido señal recientemente.',
          channel: 'atencion',
        };
      case 'IDLE':
        return {
          title: `${animalName} lleva horas sin movimiento`,
          body: 'Posible parto asistido o requiere inspección veterinaria.',
          channel: 'atencion',
        };
      case 'SPEED':
        return {
          title: `Velocidad inusual en ${animalName}`,
          body: `${event.speedKmh} km/h detectados. Posible traslado en vehículo.`,
          channel: 'urgente',
        };
      default:
        return {
          title: `Novedad en ${animalName}`,
          body: 'Se detectó un evento en el hato.',
          channel: 'general',
        };
    }
  }
}
