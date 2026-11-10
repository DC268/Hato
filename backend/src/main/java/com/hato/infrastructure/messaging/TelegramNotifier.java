package com.hato.infrastructure.messaging;

import com.hato.application.notification.Notifier;
import com.hato.domain.model.NotificationMessage;
import com.hato.domain.model.Recipient;
import com.hato.domain.model.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TelegramNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotifier.class);

    @Override
    public boolean supports(Severity severity) {
        // Telegram actúa como respaldo exclusivo para alertas de prioridad ALTA o CRÍTICA
        return severity == Severity.HIGH || severity == Severity.CRITICAL;
    }

    @Override
    public void send(Recipient to, NotificationMessage message) {
        if (to.telegramChatId() == null || to.telegramChatId().isBlank()) return;
        log.info("[TELEGRAM BOT] Enviando al chat '{}': 🚨 {} - {}",
                to.telegramChatId(), message.title(), message.body());
    }
}
