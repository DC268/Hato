package com.hato.api.controller;

import com.hato.api.dto.ReportResponse;
import com.hato.application.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/exits")
    public ResponseEntity<ReportResponse> getExitsReport(
            @RequestParam(value = "farmId", required = false) String farmId,
            @RequestParam(value = "from", required = false) String fromStr,
            @RequestParam(value = "to", required = false) String toStr
    ) {
        Instant from = fromStr != null ? Instant.parse(fromStr) : null;
        Instant to = toStr != null ? Instant.parse(toStr) : null;
        return ResponseEntity.ok(reportService.getExitsReport(farmId, from, to));
    }
}
