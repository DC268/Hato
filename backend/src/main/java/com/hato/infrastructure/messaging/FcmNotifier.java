package com.hato.infrastructure.messaging;

import com.hato.application.notification.Notifier;
import com.hato.domain.model.NotificationMessage;
import com.hato.domain.model.Recipient;
import com.hato.domain.model.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FcmNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(FcmNotifier.class);

    @Override
    public boolean supports(Severity severity) {
        return true; // FCM recibe todos los niveles de severidad
    }

    @Override
    public void send(Recipient to, NotificationMessage message) {
        if (to.fcmToken() == null || to.fcmToken().isBlank()) return;
        // Simulación o integración con Firebase Admin SDK
        log.info("[FCM PUSH] Enviando a '{}' (Token: {}): {} - {}",
                to.name(), to.fcmToken().substring(0, Math.min(10, to.fcmToken().length())) + "...",
                message.title(), message.body());
    }
}
