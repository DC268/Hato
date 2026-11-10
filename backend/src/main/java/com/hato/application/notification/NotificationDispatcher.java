package com.hato.application.notification;

import com.hato.domain.model.AlertEvent;
import com.hato.domain.model.NotificationMessage;
import com.hato.domain.model.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final List<Notifier> notifiers;
    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

    public NotificationDispatcher(List<Notifier> notifiers) {
        this.notifiers = notifiers != null ? notifiers : List.of();
    }

    public void dispatch(AlertEvent event, List<Recipient> recipients) {
        if (recipients == null || recipients.isEmpty()) return;

        NotificationMessage msg = NotificationTemplate.from(event);

        for (Recipient r : recipients) {
            for (Notifier n : notifiers) {
                if (n.supports(event.severity())) {
                    pool.submit(() -> {
                        try {
                            n.send(r, msg);
                        } catch (Exception e) {
                            log.error("Error al enviar notificación a {}", r.email(), e);
                        }
                    });
                }
            }
        }
    }
}
