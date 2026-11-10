package com.hato.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hato.alerts")
public record AlertProperties(
        int noSignalFactor,
        int lowBatteryPercent,
        int idleHours,
        double minAccuracyMeters,
        int exitConfirmations
) {}
