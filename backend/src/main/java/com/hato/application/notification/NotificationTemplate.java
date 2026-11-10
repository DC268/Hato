package com.hato.application.notification;

import com.hato.domain.model.AlertEvent;
import com.hato.domain.model.NotificationMessage;
import com.hato.domain.model.Severity;

public final class NotificationTemplate {

    private NotificationTemplate() {}

    public static NotificationMessage from(AlertEvent event) {
        return switch (event) {
            case AlertEvent.ExitedZone e -> new NotificationMessage(
                    "Alerta de Fuga: Animal fuera de zona",
                    String.format("El animal se encuentra a %.0f metros fuera del límite.", e.metersOut()),
                    Severity.HIGH
            );
            case AlertEvent.ReturnedToZone e -> new NotificationMessage(
                    "Animal de vuelta",
                    "El animal ha retornado dentro de los límites del potrero.",
                    Severity.LOW
            );
            case AlertEvent.LowBattery e -> new NotificationMessage(
                    "Batería baja en collar GPS",
                    "Queda el " + e.percent() + "% de batería.",
                    Severity.MEDIUM
            );
            case AlertEvent.NoSignal e -> new NotificationMessage(
                    "Sin señal del collar GPS",
                    "Silencio de " + e.silentFor().toMinutes() + " minutos sin reportar.",
                    Severity.MEDIUM
            );
            case AlertEvent.Idle e -> new NotificationMessage(
                    "Alerta de Inactividad",
                    "El animal lleva " + e.idleFor().toHours() + " horas sin moverse.",
                    Severity.HIGH
            );
        };
    }
}
