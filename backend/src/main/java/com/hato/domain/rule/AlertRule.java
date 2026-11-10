package com.hato.domain.rule;

import com.hato.domain.model.AlertEvent;
import java.util.List;

public interface AlertRule {
    List<AlertEvent> evaluate(AlertContext ctx);
}
