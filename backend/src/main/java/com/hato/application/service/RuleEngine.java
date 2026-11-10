package com.hato.application.service;

import com.hato.domain.model.AlertEvent;
import com.hato.domain.rule.AlertContext;
import com.hato.domain.rule.AlertRule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RuleEngine {

    private final List<AlertRule> rules;

    public RuleEngine(List<AlertRule> rules) {
        this.rules = rules != null ? rules : List.of();
    }

    public List<AlertEvent> evaluate(AlertContext ctx) {
        List<AlertEvent> events = new ArrayList<>();
        for (AlertRule rule : rules) {
            events.addAll(rule.evaluate(ctx));
        }
        return events;
    }
}
