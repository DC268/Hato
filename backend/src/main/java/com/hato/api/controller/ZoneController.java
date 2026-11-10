package com.hato.api.controller;

import com.hato.api.dto.ZoneDto;
import com.hato.application.service.ZoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping
    public ResponseEntity<List<ZoneDto>> getZones(@RequestParam(value = "farmId", required = false) String farmId) {
        return ResponseEntity.ok(zoneService.findAll(farmId));
    }

    @PostMapping
    public ResponseEntity<ZoneDto> createZone(@RequestBody ZoneDto dto) {
        return ResponseEntity.ok(zoneService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable("id") String id) {
        zoneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
