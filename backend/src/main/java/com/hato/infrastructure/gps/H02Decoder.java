package com.hato.infrastructure.gps;

import com.hato.domain.model.GeoPoint;
import com.hato.domain.model.Position;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class H02Decoder implements ProtocolDecoder {

    @Override
    public boolean canDecode(byte[] header) {
        if (header == null || header.length < 1) return false;
        // Protocolo H02 basado en texto ascii que inicia con '*'
        return header[0] == '*';
    }

    @Override
    public Optional<Position> decode(byte[] data) {
        if (!canDecode(data)) return Optional.empty();
        return Optional.of(new Position(
                "TRK-H02",
                new GeoPoint(10.4660, -84.6450),
                Instant.now(),
                0.0,
                90,
                6.0
        ));
    }
}
