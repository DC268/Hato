package com.hato.domain.rule;

import com.hato.domain.model.AlertEvent;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LowBatteryRule implements AlertRule {

    private static final int DEFAULT_THRESHOLD = 20;

    @Override
    public List<AlertEvent> evaluate(AlertContext ctx) {
        int battery = ctx.current().batteryPercent();
        boolean wasAbove = ctx.previous()
                .map(p -> p.batteryPercent() > DEFAULT_THRESHOLD)
                .orElse(true);

        if (battery <= DEFAULT_THRESHOLD && wasAbove) {
            return List.of(new AlertEvent.LowBattery(
                    ctx.animal().id(),
                    ctx.current().recordedAt(),
                    battery
            ));
        }
        return Collections.emptyList();
    }
}
