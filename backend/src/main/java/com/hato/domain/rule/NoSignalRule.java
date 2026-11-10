package com.hato.domain.rule;

import com.hato.domain.model.AlertEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
public class NoSignalRule implements AlertRule {

    private final int factor = 3;

    @Override
    public List<AlertEvent> evaluate(AlertContext ctx) {
        if (ctx.previous().isEmpty()) return Collections.emptyList();
        var prev = ctx.previous().get();
        Duration silent = Duration.between(prev.recordedAt(), ctx.current().recordedAt()).abs();
        if (silent.toMinutes() >= 90) { // Silencio prolongado
            return List.of(new AlertEvent.NoSignal(
                    ctx.animal().id(),
                    ctx.current().recordedAt(),
                    silent
            ));
        }
        return Collections.emptyList();
    }
}
