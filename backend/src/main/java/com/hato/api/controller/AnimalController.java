package com.hato.api.controller;

import com.hato.api.dto.AnimalDto;
import com.hato.application.service.AnimalService;
import com.hato.application.service.PositionService;
import com.hato.domain.model.Position;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    private final AnimalService animalService;
    private final PositionService positionService;

    public AnimalController(AnimalService animalService, PositionService positionService) {
        this.animalService = animalService;
        this.positionService = positionService;
    }

    @PostMapping
    public ResponseEntity<AnimalDto> createAnimal(@RequestBody AnimalDto dto) {
        return ResponseEntity.ok(animalService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalDto> updateAnimal(@PathVariable("id") String id, @RequestBody AnimalDto dto) {
        return ResponseEntity.ok(animalService.update(id, dto));
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<List<Position>> getTrack(
            @PathVariable("id") String id,
            @RequestParam(value = "from", required = false) String fromStr,
            @RequestParam(value = "to", required = false) String toStr
    ) {
        var animalOpt = animalService.findById(id);
        if (animalOpt.isEmpty() || animalOpt.get().trackerId() == null) {
            return ResponseEntity.ok(List.of());
        }

        Instant to = toStr != null ? Instant.parse(toStr) : Instant.now();
        Instant from = fromStr != null ? Instant.parse(fromStr) : to.minus(7, ChronoUnit.DAYS);

        return ResponseEntity.ok(positionService.getTrack(animalOpt.get().trackerId(), from, to));
    }
}
