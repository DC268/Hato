package com.hato.api.controller;

import com.hato.api.dto.PositionRequest;
import com.hato.api.exception.ApiException;
import com.hato.application.service.PositionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingest")
public class PositionIngestController {

    private final PositionService positionService;

    @Value("${hato.ingest.key}")
    private String expectedIngestKey;

    public PositionIngestController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping("/positions")
    public ResponseEntity<Void> ingestPosition(
            @RequestHeader(value = "X-Ingest-Key", required = false) String ingestKey,
            @Valid @RequestBody PositionRequest request
    ) {
        if (expectedIngestKey != null && !expectedIngestKey.isBlank()) {
            if (ingestKey == null || !expectedIngestKey.equals(ingestKey)) {
                throw new ApiException("INVALID_INGEST_KEY", "Clave de servicio X-Ingest-Key inválida", HttpStatus.FORBIDDEN);
            }
        }

        positionService.ingest(request);
        return ResponseEntity.accepted().build();
    }
}
