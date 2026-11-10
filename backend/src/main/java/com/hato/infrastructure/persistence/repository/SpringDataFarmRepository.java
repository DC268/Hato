package com.hato.infrastructure.persistence.repository;

import com.hato.infrastructure.persistence.entity.FarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataFarmRepository extends JpaRepository<FarmEntity, String> {
    List<FarmEntity> findByOwnerName(String ownerName);
}
