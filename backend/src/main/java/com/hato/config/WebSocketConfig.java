package com.hato.config;

import com.hato.infrastructure.realtime.LiveSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LiveSocketHandler liveSocketHandler;

    public WebSocketConfig(LiveSocketHandler liveSocketHandler) {
        this.liveSocketHandler = liveSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(liveSocketHandler, "/ws/live")
                .setAllowedOrigins("*");
    }
}
