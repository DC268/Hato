package com.hato.domain.rule;

import com.hato.domain.model.AlertEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
public class SpeedRule implements AlertRule {

    private static final double MAX_BOVINE_SPEED_KMH = 35.0; // Velocidad sospechosa de vehículo

    @Override
    public List<AlertEvent> evaluate(AlertContext ctx) {
        if (ctx.current().speedKmh() > MAX_BOVINE_SPEED_KMH) {
            // Posible abigeato o traslado en camión
            return List.of(new AlertEvent.ExitedZone(
                    ctx.animal().id(),
                    "SPEED-ALERT",
                    ctx.current().recordedAt(),
                    ctx.current().speedKmh()
            ));
        }
        return Collections.emptyList();
    }
}
