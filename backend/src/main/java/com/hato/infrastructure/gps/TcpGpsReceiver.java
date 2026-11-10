package com.hato.infrastructure.gps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TcpGpsReceiver implements GpsReceiver {

    private static final Logger log = LoggerFactory.getLogger(TcpGpsReceiver.class);

    @Value("${hato.gps.tcp-port:5023}")
    private int port;

    @Value("${hato.gps.enabled:true}")
    private boolean enabled;

    private final DecoderFactory decoders;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private ExecutorService pool;

    public TcpGpsReceiver(DecoderFactory decoders) {
        this.decoders = decoders;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        if (enabled) {
            Thread.ofVirtual().start(() -> {
                try {
                    start();
                } catch (IOException e) {
                    log.error("Error iniciando servidor TCP de GPS en puerto {}", port, e);
                }
            });
        }
    }

    @Override
    public void start() throws IOException {
        running = true;
        serverSocket = new ServerSocket(port);
        pool = Executors.newVirtualThreadPerTaskExecutor();
        log.info("Servidor TCP para collares GPS iniciado en el puerto {}", port);

        while (running) {
            try {
                Socket socket = serverSocket.accept();
                pool.submit(() -> handleClient(socket));
            } catch (IOException e) {
                if (!running) break;
                log.warn("Excepción aceptando conexión GPS", e);
            }
        }
    }

    private void handleClient(Socket socket) {
        try (socket; InputStream in = socket.getInputStream()) {
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            if (read > 0) {
                byte[] data = new byte[read];
                System.arraycopy(buffer, 0, data, 0, read);
                decoders.findDecoder(data).ifPresent(decoder -> {
                    decoder.decode(data).ifPresent(pos -> {
                        log.info("Posición GPS decodificada desde TCP: {}", pos);
                    });
                });
            }
        } catch (Exception e) {
            log.debug("Error procesando conexión GPS: {}", e.getMessage());
        }
    }

    @Override
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            if (pool != null) pool.shutdown();
        } catch (IOException ignored) {}
    }
}
