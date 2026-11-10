package com.hato.application.notification;

import com.hato.domain.model.AlertEvent;
import com.hato.domain.model.NotificationMessage;
import com.hato.domain.model.Severity;

public final class NotificationTemplate {

    private NotificationTemplate() {}

    public static NotificationMessage from(AlertEvent event) {
        if (event instanceof AlertEvent.ExitedZone e) {
            return new NotificationMessage(
                    "Alerta de Fuga: Animal fuera de zona",
                    String.format("El animal se encuentra a %.0f metros fuera del límite.", e.metersOut()),
                    Severity.HIGH
            );
        }
        if (event instanceof AlertEvent.ReturnedToZone) {
            return new NotificationMessage(
                    "Animal de vuelta",
                    "El animal ha retornado dentro de los límites del potrero.",
                    Severity.LOW
            );
        }
        if (event instanceof AlertEvent.LowBattery e) {
            return new NotificationMessage(
                    "Batería baja en collar GPS",
                    "Queda el " + e.percent() + "% de batería.",
                    Severity.MEDIUM
            );
        }
        if (event instanceof AlertEvent.NoSignal e) {
            return new NotificationMessage(
                    "Sin señal del collar GPS",
                    "Silencio de " + e.silentFor().toMinutes() + " minutos sin reportar.",
                    Severity.MEDIUM
            );
        }
        if (event instanceof AlertEvent.Idle e) {
            return new NotificationMessage(
                    "Alerta de Inactividad",
                    "El animal lleva " + e.idleFor().toHours() + " horas sin moverse.",
                    Severity.HIGH
            );
        }
        return new NotificationMessage(
                "Notificación Hato",
                "Actualización de estado en ganado.",
                Severity.LOW
        );
    }
}
