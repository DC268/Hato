package com.hato.infrastructure.gps;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DecoderFactory {

    private final List<ProtocolDecoder> decoders;

    public DecoderFactory(List<ProtocolDecoder> decoders) {
        this.decoders = decoders;
    }

    public Optional<ProtocolDecoder> findDecoder(byte[] header) {
        return decoders.stream()
                .filter(d -> d.canDecode(header))
                .findFirst();
    }
}
