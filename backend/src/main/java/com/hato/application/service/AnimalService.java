package com.hato.application.service;

import com.hato.api.dto.AnimalDto;
import com.hato.domain.model.Animal;
import com.hato.infrastructure.persistence.entity.AnimalEntity;
import com.hato.infrastructure.persistence.repository.SpringDataAnimalRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnimalService {

    private final SpringDataAnimalRepository animalRepository;

    public AnimalService(SpringDataAnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    @PostConstruct
    public void seedInitialAnimals() {
        if (animalRepository.count() == 0) {
            saveEntity("a-03", "farm-01", "Vaca Pinta", "#0342", "03", "Holstein", 4, 450f, "TRK-0001", "zone-default-1", "ALERT");
            saveEntity("a-21", "farm-01", "Toro Brahman", "#0343", "21", "Brahman", 5, 600f, "TRK-0002", "zone-default-1", "HEALTHY");
            saveEntity("a-12", "farm-01", "Vaca Manchas", "#0344", "12", "Jersey", 3, 400f, "TRK-0003", "zone-default-1", "HEALTHY");
        }
    }

    private void saveEntity(String id, String farmId, String name, String tag, String display, String breed, int age, Float weight, String trackerId, String zoneId, String health) {
        AnimalEntity a = new AnimalEntity();
        a.setId(id);
        a.setFarmId(farmId);
        a.setName(name);
        a.setTagNumber(tag);
        a.setDisplayNumber(display);
        a.setBreed(breed);
        a.setAgeYears(age);
        a.setWeightKg(weight);
        a.setTrackerId(trackerId);
        a.setCurrentZoneId(zoneId);
        a.setHealthStatus(health);
        a.setCreatedAt(Instant.now());
        animalRepository.save(a);
    }

    @Transactional(readOnly = true)
    public List<AnimalDto> findByFarm(String farmId) {
        return animalRepository.findByFarmId(farmId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Animal> findByTracker(String trackerId) {
        return animalRepository.findByTrackerId(trackerId).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public Optional<Animal> findById(String id) {
        return animalRepository.findById(id).map(this::toDomain);
    }

    @Transactional
    public AnimalDto create(AnimalDto dto) {
        AnimalEntity e = new AnimalEntity();
        e.setId(dto.id() != null && !dto.id().isBlank() ? dto.id() : UUID.randomUUID().toString());
        e.setFarmId(dto.farmId());
        e.setName(dto.name());
        e.setTagNumber(dto.tagNumber());
        e.setDisplayNumber(dto.displayNumber() != null ? dto.displayNumber() : dto.tagNumber());
        e.setBreed(dto.breed());
        e.setAgeYears(dto.ageYears());
        e.setWeightKg(dto.weightKg());
        e.setTrackerId(dto.trackerId());
        e.setCurrentZoneId(dto.currentZoneId());
        e.setHealthStatus(dto.healthStatus() != null ? dto.healthStatus() : "HEALTHY");
        e.setCreatedAt(Instant.now());

        return toDto(animalRepository.save(e));
    }

    @Transactional
    public AnimalDto update(String id, AnimalDto dto) {
        AnimalEntity e = animalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal no encontrado con id " + id));

        e.setName(dto.name());
        e.setTagNumber(dto.tagNumber());
        e.setDisplayNumber(dto.displayNumber());
        e.setBreed(dto.breed());
        e.setAgeYears(dto.ageYears());
        e.setWeightKg(dto.weightKg());
        e.setTrackerId(dto.trackerId());
        e.setCurrentZoneId(dto.currentZoneId());
        if (dto.healthStatus() != null) e.setHealthStatus(dto.healthStatus());

        return toDto(animalRepository.save(e));
    }

    @Transactional
    public void updateHealthStatus(String animalId, String healthStatus) {
        animalRepository.findById(animalId).ifPresent(e -> {
            e.setHealthStatus(healthStatus);
            animalRepository.save(e);
        });
    }

    private AnimalDto toDto(AnimalEntity e) {
        return new AnimalDto(
                e.getId(), e.getFarmId(), e.getName(), e.getTagNumber(), e.getDisplayNumber(),
                e.getBreed(), e.getAgeYears(), e.getWeightKg(), e.getPhotoUrl(), e.getNotes(),
                e.getTrackerId(), e.getCurrentZoneId(), e.getHealthStatus()
        );
    }

    private Animal toDomain(AnimalEntity e) {
        return new Animal(
                e.getId(), e.getFarmId(), e.getName(), e.getTagNumber(), e.getDisplayNumber(),
                e.getBreed(), e.getAgeYears(), e.getWeightKg(), e.getPhotoUrl(), e.getNotes(),
                e.getTrackerId(), e.getCurrentZoneId(), e.getHealthStatus()
        );
    }
}
