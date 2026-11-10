package com.hato.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(
        @NotBlank(message = "El token FCM es obligatorio")
        String token,
        String platform
) {}
