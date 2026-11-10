package com.hato.domain.model;

import java.time.Instant;

public record User(
        String id,
        String name,
        String email,
        String passwordHash,
        String phone,
        Role role,
        String avatarUrl,
        Instant createdAt
) {}
