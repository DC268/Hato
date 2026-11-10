package com.hato.infrastructure.persistence.repository;

import com.hato.infrastructure.persistence.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SpringDataAlertRepository extends JpaRepository<AlertEntity, String> {
    List<AlertEntity> findByFarmIdOrderByCreatedAtDesc(String farmId);
    List<AlertEntity> findByFarmIdAndAcknowledgedFalseOrderByCreatedAtDesc(String farmId);
    List<AlertEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);
}
