package com.hato.infrastructure.persistence.repository;

import com.hato.infrastructure.persistence.entity.ZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataZoneRepository extends JpaRepository<ZoneEntity, String> {
    List<ZoneEntity> findByFarmId(String farmId);
    List<ZoneEntity> findByFarmIdAndActiveTrue(String farmId);
}
