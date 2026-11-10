package com.hato.domain.rule;

import com.hato.domain.geofence.Geofence;
import com.hato.domain.model.Animal;
import com.hato.domain.model.Position;

import java.util.List;
import java.util.Optional;

public record AlertContext(
        Animal animal,
        Position current,
        Optional<Position> previous,
        List<Geofence> zones
) {}
