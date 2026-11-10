package com.hato.domain.rule;

import com.hato.domain.model.AlertEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
public class IdleTooLongRule implements AlertRule {

    @Override
    public List<AlertEvent> evaluate(AlertContext ctx) {
        if (ctx.previous().isEmpty()) return Collections.emptyList();

        var prev = ctx.previous().get();
        double distance = ctx.current().point().distanceMetersTo(prev.point());
        Duration duration = Duration.between(prev.recordedAt(), ctx.current().recordedAt()).abs();

        if (distance < 5.0 && duration.toHours() >= 4) {
            return List.of(new AlertEvent.Idle(
                    ctx.animal().id(),
                    ctx.current().recordedAt(),
                    duration
            ));
        }
        return Collections.emptyList();
    }
}
