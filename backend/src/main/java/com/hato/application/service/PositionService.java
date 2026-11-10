package com.hato.application.service;

import com.hato.api.dto.PositionRequest;
import com.hato.domain.model.Animal;
import com.hato.domain.model.GeoPoint;
import com.hato.domain.model.Position;
import com.hato.domain.rule.AlertContext;
import com.hato.infrastructure.persistence.entity.PositionEntity;
import com.hato.infrastructure.persistence.entity.TrackerEntity;
import com.hato.infrastructure.persistence.repository.SpringDataPositionRepository;
import com.hato.infrastructure.persistence.repository.SpringDataTrackerRepository;
import com.hato.infrastructure.realtime.LivePositionPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PositionService {

    private static final Logger log = LoggerFactory.getLogger(PositionService.class);

    private final SpringDataPositionRepository positionRepository;
    private final SpringDataTrackerRepository trackerRepository;
    private final AnimalService animalService;
    private final ZoneService zoneService;
    private final RuleEngine rules;
    private final AlertService alertService;
    private final LivePositionPublisher livePublisher;

    public PositionService(
            SpringDataPositionRepository positionRepository,
            SpringDataTrackerRepository trackerRepository,
            AnimalService animalService,
            ZoneService zoneService,
            RuleEngine rules,
            AlertService alertService,
            LivePositionPublisher livePublisher
    ) {
        this.positionRepository = positionRepository;
        this.trackerRepository = trackerRepository;
        this.animalService = animalService;
        this.zoneService = zoneService;
        this.rules = rules;
        this.alertService = alertService;
        this.livePublisher = livePublisher;
    }

    @Transactional
    public void ingest(PositionRequest req) {
        // 1. Idempotencia: evitar duplicados por clave única (trackerId + recordedAt)
        if (positionRepository.existsByTrackerIdAndRecordedAt(req.trackerCode(), req.recordedAt())) {
            log.debug("Posición ignorada por duplicado: {} en {}", req.trackerCode(), req.recordedAt());
            return;
        }

        // 2. Obtener posición previa
        Optional<Position> previous = positionRepository
                .findFirstByTrackerIdOrderByRecordedAtDesc(req.trackerCode())
                .map(this::toDomain);

        // 3. Guardar en base de datos PostgreSQL / Supabase
        PositionEntity entity = new PositionEntity();
        entity.setTrackerId(req.trackerCode());
        entity.setLat(req.lat());
        entity.setLng(req.lng());
        entity.setRecordedAt(req.recordedAt());
        entity.setSpeedKmh(req.speedKmh() != null ? req.speedKmh() : 0.0);
        entity.setBattery(req.battery() != null ? req.battery() : 100);
        entity.setAccuracyMeters(req.accuracy() != null ? req.accuracy() : 5.0);
        entity.setCreatedAt(Instant.now());
        positionRepository.save(entity);

        // 4. Actualizar estado del tracker
        updateTrackerState(req);

        // 5. Vincular con el animal correspondiente
        Position current = toDomain(entity);
        Optional<Animal> animalOpt = animalService.findByTracker(req.trackerCode());

        String farmId = animalOpt.map(Animal::farmId).orElse("farm-01");
        String animalId = animalOpt.map(Animal::id).orElse(req.trackerCode());

        // 6. Publicar por WebSocket a la aplicación móvil en tiempo real
        String status = "INSIDE";
        if (animalOpt.isPresent()) {
            Animal animal = animalOpt.get();
            var zones = zoneService.activeFor(animal);

            // Evaluar reglas de alerta (Fuga, Batería baja, Inactividad, Velocidad)
            var ctx = new AlertContext(animal, current, previous, zones);
            var events = rules.evaluate(ctx);

            boolean isOutside = zones.stream().anyMatch(z -> z.isOutside(current.point()));
            status = isOutside ? "ALERT" : "INSIDE";

            animalService.updateHealthStatus(animal.id(), status.equals("ALERT") ? "ALERT" : "HEALTHY");

            if (!events.isEmpty()) {
                alertService.register(events, farmId, animal.name(), zones.isEmpty() ? "Potrero" : zones.get(0).name());
            }
        }

        livePublisher.publishPosition(farmId, animalId, current, status);
    }

    private void updateTrackerState(PositionRequest req) {
        TrackerEntity tracker = trackerRepository.findByCode(req.trackerCode())
                .orElseGet(() -> {
                    TrackerEntity t = new TrackerEntity();
                    t.setId("trk-" + req.trackerCode().toLowerCase());
                    t.setCode(req.trackerCode());
                    t.setReportIntervalSeconds(30);
                    return t;
                });

        tracker.setLastSeenAt(req.recordedAt());
        if (req.battery() != null) tracker.setBatteryPercent(req.battery());
        trackerRepository.save(tracker);
    }

    @Transactional(readOnly = true)
    public List<Position> getTrack(String trackerId, Instant from, Instant to) {
        return positionRepository
                .findByTrackerIdAndRecordedAtBetweenOrderByRecordedAtAsc(trackerId, from, to)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Position toDomain(PositionEntity e) {
        return new Position(
                e.getTrackerId(),
                new GeoPoint(e.getLat(), e.getLng()),
                e.getRecordedAt(),
                e.getSpeedKmh(),
                e.getBattery(),
                e.getAccuracyMeters()
        );
    }
}
