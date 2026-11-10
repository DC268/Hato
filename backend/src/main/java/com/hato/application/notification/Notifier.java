package com.hato.application.notification;

import com.hato.domain.model.NotificationMessage;
import com.hato.domain.model.Recipient;
import com.hato.domain.model.Severity;

public interface Notifier {
    boolean supports(Severity severity);
    void send(Recipient to, NotificationMessage message);
}
