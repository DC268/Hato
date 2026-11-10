package com.hato.domain.rule;

import com.hato.domain.geofence.Geofence;
import com.hato.domain.model.AlertEvent;
import com.hato.domain.model.GeoPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OutOfZoneRule implements AlertRule {

    @Override
    public List<AlertEvent> evaluate(AlertContext ctx) {
        List<AlertEvent> events = new ArrayList<>();
        GeoPoint now = ctx.current().point();

        for (Geofence zone : ctx.zones()) {
            boolean isOutsideNow = zone.isOutside(now);
            boolean wasInsideBefore = ctx.previous()
                    .map(p -> !zone.isOutside(p.point()))
                    .orElse(true);

            // Disparar solo en la transición de adentro hacia afuera para evitar spam
            if (isOutsideNow && wasInsideBefore) {
                events.add(new AlertEvent.ExitedZone(
                        ctx.animal().id(),
                        zone.id(),
                        ctx.current().recordedAt(),
                        zone.distanceToBoundaryMeters(now)
                ));
            }
        }
        return events;
    }
}
