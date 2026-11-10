package com.hato.api.controller;

import com.hato.application.service.AnimalService;
import com.hato.application.service.FarmService;
import com.hato.domain.model.Farm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
public class FarmController {

    private final FarmService farmService;
    private final AnimalService animalService;

    public FarmController(FarmService farmService, AnimalService animalService) {
        this.farmService = farmService;
        this.animalService = animalService;
    }

    @GetMapping
    public ResponseEntity<List<Farm>> getFarms() {
        return ResponseEntity.ok(farmService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Farm> getFarmById(@Path("id") String id) {
        return farmService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/animals")
    public ResponseEntity<?> getFarmAnimals(@Path("id") String id) {
        return ResponseEntity.ok(animalService.findByFarm(id));
    }
}
