package com.hato.domain.geofence;

import java.time.LocalTime;

public record ZoneSchedule(
        String zoneId,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {
    public boolean isWithinSchedule(LocalTime time) {
        if (!active) return false;
        if (startTime.isBefore(endTime)) {
            return !time.isBefore(startTime) && !time.isAfter(endTime);
        } else {
            return !time.isBefore(startTime) || !time.isAfter(endTime);
        }
    }
}
