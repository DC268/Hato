package com.hato.infrastructure.gps;

import com.hato.domain.model.Position;
import java.util.Optional;

public interface ProtocolDecoder {
    boolean canDecode(byte[] header);
    Optional<Position> decode(byte[] data);
}
