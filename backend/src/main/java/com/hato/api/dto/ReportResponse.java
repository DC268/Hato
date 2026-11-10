package com.hato.api.dto;

import java.util.List;

public record ReportResponse(
        String farmId,
        String from,
        String to,
        int totalExits,
        int activeAlerts,
        List<AlertDto> exitEvents
) {}
