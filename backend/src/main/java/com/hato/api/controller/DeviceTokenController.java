package com.hato.api.controller;

import com.hato.api.dto.DeviceTokenRequest;
import com.hato.infrastructure.persistence.entity.DeviceTokenEntity;
import com.hato.infrastructure.persistence.repository.SpringDataDeviceTokenRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceTokenController {

    private final SpringDataDeviceTokenRepository tokenRepository;

    public DeviceTokenController(SpringDataDeviceTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/token")
    public ResponseEntity<Void> registerToken(@Valid @RequestBody DeviceTokenRequest request, Authentication auth) {
        String userId = auth != null ? auth.getName() : "user-01";

        DeviceTokenEntity entity = tokenRepository.findByToken(request.token())
                .orElseGet(() -> {
                    DeviceTokenEntity e = new DeviceTokenEntity();
                    e.setId(UUID.randomUUID().toString());
                    e.setToken(request.token());
                    return e;
                });

        entity.setUserId(userId);
        entity.setPlatform(request.platform() != null ? request.platform() : "ANDROID");
        entity.setUpdatedAt(Instant.now());

        tokenRepository.save(entity);
        return ResponseEntity.ok().build();
    }
}
