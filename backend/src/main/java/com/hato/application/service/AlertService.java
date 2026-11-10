package com.hato.application.service;

import com.hato.api.dto.AlertDto;
import com.hato.application.notification.NotificationDispatcher;
import com.hato.domain.model.Alert;
import com.hato.domain.model.AlertEvent;
import com.hato.domain.model.Recipient;
import com.hato.domain.model.Severity;
import com.hato.infrastructure.persistence.entity.AlertEntity;
import com.hato.infrastructure.persistence.repository.SpringDataAlertRepository;
import com.hato.infrastructure.persistence.repository.SpringDataDeviceTokenRepository;
import com.hato.infrastructure.persistence.repository.SpringDataUserRepository;
import com.hato.infrastructure.realtime.LivePositionPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AlertService {

    private final SpringDataAlertRepository alertRepository;
    private final NotificationDispatcher dispatcher;
    private final AlertDeduplicator deduplicator;
    private final LivePositionPublisher livePublisher;
    private final SpringDataUserRepository userRepository;
    private final SpringDataDeviceTokenRepository deviceTokenRepository;

    public AlertService(
            SpringDataAlertRepository alertRepository,
            NotificationDispatcher dispatcher,
            AlertDeduplicator deduplicator,
            LivePositionPublisher livePublisher,
            SpringDataUserRepository userRepository,
            SpringDataDeviceTokenRepository deviceTokenRepository
    ) {
        this.alertRepository = alertRepository;
        this.dispatcher = dispatcher;
        this.deduplicator = deduplicator;
        this.livePublisher = livePublisher;
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Transactional
    public void register(List<AlertEvent> events, String farmId, String animalName, String zoneName) {
        if (events == null || events.isEmpty()) return;

        for (AlertEvent event : events) {
            if (!deduplicator.shouldDispatch(event)) continue;

            AlertEntity entity = new AlertEntity();
            entity.setId("al-" + UUID.randomUUID().toString().substring(0, 8));
            entity.setFarmId(farmId);
            entity.setAnimalId(event.animalId());
            entity.setAnimalName(animalName);
            entity.setZoneName(zoneName);
            entity.setType(event.getClass().getSimpleName());
            entity.setSeverity(event.severity());
            entity.setMessage("Evento de alerta detectado: " + entity.getType());
            entity.setCreatedAt(event.at() != null ? event.at() : Instant.now());

            if (event instanceof AlertEvent.ExitedZone e) {
                entity.setZoneId(e.zoneId());
                entity.setMetersOut(e.metersOut());
                entity.setMessage(String.format("Animal fuera de zona a %.0f m del límite", e.metersOut()));
            }

            alertRepository.save(entity);

            // Publicar en vivo a través de WebSocket
            Alert alertDomain = toDomain(entity);
            livePublisher.publishAlert(farmId, alertDomain);

            // Despachar a FCM y canales de respaldo
            List<Recipient> recipients = buildRecipients();
            dispatcher.dispatch(event, recipients);
        }
    }

    @Transactional(readOnly = true)
    public List<AlertDto> findByFarm(String farmId, String status) {
        List<AlertEntity> entities = "pending".equalsIgnoreCase(status)
                ? alertRepository.findByFarmIdAndAcknowledgedFalseOrderByCreatedAtDesc(farmId)
                : alertRepository.findByFarmIdOrderByCreatedAtDesc(farmId);

        return entities.stream().map(this::toDto).toList();
    }

    @Transactional
    public void acknowledge(String alertId, String userId) {
        alertRepository.findById(alertId).ifPresent(a -> {
            a.setAcknowledged(true);
            a.setAcknowledgedBy(userId != null ? userId : "user");
            a.setAcknowledgedAt(Instant.now());
            alertRepository.save(a);
        });
    }

    private List<Recipient> buildRecipients() {
        List<Recipient> list = new ArrayList<>();
        var users = userRepository.findAll();
        for (var u : users) {
            var tokens = deviceTokenRepository.findByUserId(u.getId());
            String fcm = tokens.isEmpty() ? null : tokens.get(0).getToken();
            list.add(new Recipient(u.getId(), u.getName(), u.getEmail(), fcm, null));
        }
        return list;
    }

    private Alert toDomain(AlertEntity e) {
        return new Alert(
                e.getId(), e.getFarmId(), e.getAnimalId(), e.getAnimalName(),
                e.getZoneId(), e.getZoneName(), e.getType(), e.getSeverity(),
                e.getMessage(), e.getMetersOut(), e.isAcknowledged(),
                e.getAcknowledgedBy(), e.getAcknowledgedAt(), e.getCreatedAt()
        );
    }

    public AlertDto toDto(AlertEntity e) {
        return new AlertDto(
                e.getId(), e.getFarmId(), e.getAnimalId(), e.getAnimalName(),
                e.getZoneId(), e.getZoneName(), e.getType(), e.getSeverity().name(),
                e.getMessage(), e.getMetersOut(), e.isAcknowledged(),
                e.getAcknowledgedBy(), e.getAcknowledgedAt(), e.getCreatedAt()
        );
    }
}
