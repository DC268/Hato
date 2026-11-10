package com.hato.api.dto;

public record UserDto(
        String id,
        String name,
        String email,
        String role,
        String phone
) {}
