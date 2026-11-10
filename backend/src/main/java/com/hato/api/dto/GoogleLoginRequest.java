package com.hato.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "El idToken de Google es obligatorio")
        String idToken
) {}
