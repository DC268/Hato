package com.hato.domain.model;

import java.time.Duration;
import java.time.Instant;

/**
 * Jerarquía sellada de eventos de alerta del dominio.
 */
public sealed interface AlertEvent {

    String animalId();
    Instant at();
    Severity severity();

    record ExitedZone(String animalId, String zoneId, Instant at, double metersOut) implements AlertEvent {
        @Override
        public Severity severity() {
            return Severity.HIGH;
        }
    }

    record ReturnedToZone(String animalId, String zoneId, Instant at) implements AlertEvent {
        @Override
        public Severity severity() {
            return Severity.LOW;
        }
    }

    record LowBattery(String animalId, Instant at, int percent) implements AlertEvent {
        @Override
        public Severity severity() {
            return Severity.MEDIUM;
        }
    }

    record NoSignal(String animalId, Instant at, Duration silentFor) implements AlertEvent {
        @Override
        public Severity severity() {
            return Severity.MEDIUM;
        }
    }

    record Idle(String animalId, Instant at, Duration idleFor) implements AlertEvent {
        @Override
        public Severity severity() {
            return Severity.HIGH;
        }
    }
}
