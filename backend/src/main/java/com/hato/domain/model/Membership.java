package com.hato.domain.model;

public record Membership(
        String id,
        String userId,
        String farmId,
        Role role
) {}
