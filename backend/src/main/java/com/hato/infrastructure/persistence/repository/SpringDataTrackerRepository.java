package com.hato.infrastructure.persistence.repository;

import com.hato.infrastructure.persistence.entity.TrackerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataTrackerRepository extends JpaRepository<TrackerEntity, String> {
    Optional<TrackerEntity> findByCode(String code);
}
