package com.hato.infrastructure.persistence.repository;

import com.hato.infrastructure.persistence.entity.DeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataDeviceTokenRepository extends JpaRepository<DeviceTokenEntity, String> {
    List<DeviceTokenEntity> findByUserId(String userId);
    Optional<DeviceTokenEntity> findByToken(String token);
}
