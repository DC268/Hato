package com.hato.application.service;

import com.hato.api.dto.AlertDto;
import com.hato.api.dto.ReportResponse;
import com.hato.infrastructure.persistence.repository.SpringDataAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReportService {

    private final SpringDataAlertRepository alertRepository;
    private final AlertService alertService;

    public ReportService(SpringDataAlertRepository alertRepository, AlertService alertService) {
        this.alertRepository = alertRepository;
        this.alertService = alertService;
    }

    @Transactional(readOnly = true)
    public ReportResponse getExitsReport(String farmId, Instant from, Instant to) {
        Instant start = from != null ? from : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = to != null ? to : Instant.now();

        var alerts = alertRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        var filtered = alerts.stream()
                .filter(a -> farmId == null || farmId.equalsIgnoreCase(a.getFarmId()))
                .filter(a -> "ExitedZone".equalsIgnoreCase(a.getType()))
                .map(alertService::toDto)
                .toList();

        int totalExits = filtered.size();
        int pending = (int) filtered.stream().filter(a -> !a.acknowledged()).count();

        return new ReportResponse(
                farmId != null ? farmId : "ALL",
                start.toString(),
                end.toString(),
                totalExits,
                pending,
                filtered
        );
    }
}
