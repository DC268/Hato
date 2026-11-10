package com.hato.infrastructure.gps;

import java.io.IOException;

public interface GpsReceiver {
    void start() throws IOException;
    void stop();
}
