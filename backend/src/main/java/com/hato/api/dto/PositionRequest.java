package com.hato.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PositionRequest(
        @NotBlank(message = "El código de tracker es obligatorio")
        String trackerCode,

        @DecimalMin(value = "-90.0", message = "Latitud mínima -90")
        @DecimalMax(value = "90.0", message = "Latitud máxima 90")
        double lat,

        @DecimalMin(value = "-180.0", message = "Longitud mínima -180")
        @DecimalMax(value = "180.0", message = "Longitud máxima 180")
        double lng,

        @NotNull(message = "La marca temporal recordedAt es obligatoria")
        Instant recordedAt,

        Double speedKmh,
        Integer battery,
        Double accuracy
) {}
