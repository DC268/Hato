package com.hato.domain.rule;

import com.hato.domain.geofence.Geofence;
import com.hato.domain.model.Position;
import java.util.List;

public class ExitConfirmationPolicy {

    private final int required;
    private final double maxAccuracyMeters;

    public ExitConfirmationPolicy(int required, double maxAccuracyMeters) {
        this.required = required;
        this.maxAccuracyMeters = maxAccuracyMeters;
    }

    public boolean isUsable(Position p) {
        return p.accuracyMeters() <= maxAccuracyMeters;
    }

    public boolean confirmed(List<Position> lastReadings, Geofence zone) {
        List<Position> usable = lastReadings.stream().filter(this::isUsable).toList();
        if (usable.size() < required) {
            return false;
        }
        return usable.stream().limit(required).allMatch(r -> zone.isOutside(r.point()));
    }
}
