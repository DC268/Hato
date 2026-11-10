package com.hato.infrastructure.persistence.repository;

import com.hato.infrastructure.persistence.entity.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataPositionRepository extends JpaRepository<PositionEntity, Long> {

    boolean existsByTrackerIdAndRecordedAt(String trackerId, Instant recordedAt);

    Optional<PositionEntity> findFirstByTrackerIdOrderByRecordedAtDesc(String trackerId);

    List<PositionEntity> findByTrackerIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            String trackerId, Instant from, Instant to
    );
}
