package com.hato.api.controller;

import com.hato.api.dto.AlertDto;
import com.hato.application.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<AlertDto>> getAlerts(
            @RequestParam(value = "farmId", defaultValue = "farm-01") String farmId,
            @RequestParam(value = "status", required = false) String status
    ) {
        return ResponseEntity.ok(alertService.findByFarm(farmId, status));
    }

    @PostMapping("/{id}/ack")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable("id") String id, Authentication auth) {
        String userId = auth != null ? auth.getName() : "user";
        alertService.acknowledge(id, userId);
        return ResponseEntity.noContent().build();
    }
}
