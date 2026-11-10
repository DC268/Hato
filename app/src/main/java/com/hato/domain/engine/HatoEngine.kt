package com.hato.domain.engine

import com.hato.domain.model.Geofence
import com.hato.domain.model.Position

class ExitConfirmationPolicy(
    val required: Int = 2,
    val maxAccuracyMeters: Float = 50f
) {
    fun isUsable(p: Position): Boolean {
        return p.accuracyMeters <= maxAccuracyMeters
    }

    fun confirmed(lastReadings: List<Position>, zone: Geofence): Boolean {
        val usableReadings = lastReadings.filter { isUsable(it) }
        if (usableReadings.size < required) {
            return false
        }
        return usableReadings.take(required).all { r ->
            zone.isOutside(r.point)
        }
    }
}
