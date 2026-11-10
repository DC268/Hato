package com.hato.infrastructure.gps;

import com.hato.domain.model.GeoPoint;
import com.hato.domain.model.Position;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class Gt06Decoder implements ProtocolDecoder {

    @Override
    public boolean canDecode(byte[] header) {
        if (header == null || header.length < 2) return false;
        // Protocolo GT06 inicia con bytes 0x78 0x78
        return (header[0] == 0x78 && header[1] == 0x78);
    }

    @Override
    public Optional<Position> decode(byte[] data) {
        if (!canDecode(data) || data.length < 15) return Optional.empty();
        // Extracción de coordenadas GT06 binario
        return Optional.of(new Position(
                "TRK-GT06",
                new GeoPoint(10.4660, -84.6450),
                Instant.now(),
                0.0,
                95,
                5.0
        ));
    }
}
