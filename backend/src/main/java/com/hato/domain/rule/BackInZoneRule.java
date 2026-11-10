package com.hato.domain.rule;

import com.hato.domain.geofence.Geofence;
import com.hato.domain.model.AlertEvent;
import com.hato.domain.model.GeoPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BackInZoneRule implements AlertRule {

    @Override
    public List<AlertEvent> evaluate(AlertContext ctx) {
        List<AlertEvent> events = new ArrayList<>();
        GeoPoint now = ctx.current().point();

        for (Geofence zone : ctx.zones()) {
            boolean isInsideNow = zone.contains(now);
            boolean wasOutsideBefore = ctx.previous()
                    .map(p -> zone.isOutside(p.point()))
                    .orElse(false);

            if (isInsideNow && wasOutsideBefore) {
                events.add(new AlertEvent.ReturnedToZone(
                        ctx.animal().id(),
                        zone.id(),
                        ctx.current().recordedAt()
                ));
            }
        }
        return events;
    }
}
