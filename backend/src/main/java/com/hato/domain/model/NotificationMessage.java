package com.hato.domain.model;

public record NotificationMessage(
        String title,
        String body,
        Severity severity
) {}
