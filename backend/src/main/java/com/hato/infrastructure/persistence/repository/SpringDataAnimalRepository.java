package com.hato.infrastructure.persistence.repository;

import com.hato.infrastructure.persistence.entity.AnimalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataAnimalRepository extends JpaRepository<AnimalEntity, String> {
    List<AnimalEntity> findByFarmId(String farmId);
    Optional<AnimalEntity> findByTrackerId(String trackerId);
    Optional<AnimalEntity> findByTagNumber(String tagNumber);
}
