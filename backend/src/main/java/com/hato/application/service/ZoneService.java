package com.hato.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hato.api.dto.ZoneDto;
import com.hato.domain.geofence.CircleGeofence;
import com.hato.domain.geofence.Geofence;
import com.hato.domain.geofence.PolygonGeofence;
import com.hato.domain.model.Animal;
import com.hato.domain.model.GeoPoint;
import com.hato.infrastructure.persistence.entity.ZoneEntity;
import com.hato.infrastructure.persistence.repository.SpringDataZoneRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ZoneService {

    private static final Logger log = LoggerFactory.getLogger(ZoneService.class);
    private final SpringDataZoneRepository zoneRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ZoneService(SpringDataZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    @PostConstruct
    public void seedInitialZone() {
        if (zoneRepository.count() == 0) {
            ZoneEntity z = new ZoneEntity();
            z.setId("zone-default-1");
            z.setFarmId("farm-01");
            z.setName("Potrero Principal");
            z.setType("POLYGON");
            z.setToleranceMeters(15.0f);
            z.setActive(true);
            z.setDescription("Zona de pastoreo rotativo norte");
            z.setAreaHa(12.5f);

            List<ZoneDto.PointDto> pts = List.of(
                    new ZoneDto.PointDto(10.4680, -84.6470),
                    new ZoneDto.PointDto(10.4680, -84.6430),
                    new ZoneDto.PointDto(10.4640, -84.6430),
                    new ZoneDto.PointDto(10.4640, -84.6470)
            );
            try {
                z.setPointsJson(objectMapper.writeValueAsString(pts));
            } catch (Exception ignored) {}
            zoneRepository.save(z);
        }
    }

    @Transactional(readOnly = true)
    public List<Geofence> activeFor(Animal animal) {
        var entities = zoneRepository.findByFarmIdAndActiveTrue(animal.farmId());
        return entities.stream().map(this::toGeofence).toList();
    }

    @Transactional(readOnly = true)
    public List<ZoneDto> findAll(String farmId) {
        var list = farmId != null ? zoneRepository.findByFarmId(farmId) : zoneRepository.findAll();
        return list.stream().map(this::toDto).toList();
    }

    @Transactional
    public ZoneDto save(ZoneDto dto) {
        ZoneEntity e = new ZoneEntity();
        e.setId(dto.id() != null && !dto.id().isBlank() ? dto.id() : "zone-" + UUID.randomUUID());
        e.setFarmId(dto.farmId() != null ? dto.farmId() : "farm-01");
        e.setName(dto.name());
        e.setType(dto.type() != null ? dto.type() : "POLYGON");
        e.setCenterLat(dto.centerLat());
        e.setCenterLng(dto.centerLng());
        e.setRadiusMeters(dto.radiusMeters());
        e.setToleranceMeters(dto.toleranceMeters());
        e.setActive(dto.active());
        e.setDescription(dto.description());
        e.setAreaHa(dto.areaHa());
        e.setCreatedAt(Instant.now());

        if (dto.points() != null && !dto.points().isEmpty()) {
            try {
                e.setPointsJson(objectMapper.writeValueAsString(dto.points()));
            } catch (Exception ex) {
                log.error("Error serializando puntos de geocerca", ex);
            }
        }

        return toDto(zoneRepository.save(e));
    }

    @Transactional
    public void delete(String id) {
        zoneRepository.deleteById(id);
    }

    public Geofence toGeofence(ZoneEntity e) {
        if ("CIRCLE".equalsIgnoreCase(e.getType()) && e.getCenterLat() != null && e.getRadiusMeters() != null) {
            return new CircleGeofence(
                    e.getId(), e.getFarmId(), e.getName(), e.getToleranceMeters(),
                    new GeoPoint(e.getCenterLat(), e.getCenterLng()),
                    e.getRadiusMeters(), e.isActive(), e.getDescription()
            );
        }

        List<GeoPoint> points = new ArrayList<>();
        if (e.getPointsJson() != null) {
            try {
                List<ZoneDto.PointDto> parsed = objectMapper.readValue(
                        e.getPointsJson(), new TypeReference<List<ZoneDto.PointDto>>() {}
                );
                for (var p : parsed) {
                    points.add(new GeoPoint(p.lat(), p.lng()));
                }
            } catch (Exception ex) {
                log.warn("Error deserializando puntos de zona {}", e.getId(), ex);
            }
        }

        if (points.size() < 3) {
            // Geocerca mínima por defecto
            points = List.of(
                    new GeoPoint(10.4680, -84.6470),
                    new GeoPoint(10.4680, -84.6430),
                    new GeoPoint(10.4640, -84.6430)
            );
        }

        return new PolygonGeofence(
                e.getId(), e.getFarmId(), e.getName(), e.getToleranceMeters(),
                points, e.isActive(), e.getDescription(), e.getAreaHa()
        );
    }

    public ZoneDto toDto(ZoneEntity e) {
        List<ZoneDto.PointDto> pts = Collections.emptyList();
        if (e.getPointsJson() != null) {
            try {
                pts = objectMapper.readValue(e.getPointsJson(), new TypeReference<List<ZoneDto.PointDto>>() {});
            } catch (Exception ignored) {}
        }
        return new ZoneDto(
                e.getId(), e.getFarmId(), e.getName(), e.getType(), pts,
                e.getCenterLat(), e.getCenterLng(), e.getRadiusMeters(),
                e.getToleranceMeters(), e.isActive(), e.getDescription(), e.getAreaHa()
        );
    }
}
