package com.hato.domain.model;

public record Recipient(
        String userId,
        String name,
        String email,
        String fcmToken,
        String telegramChatId
) {}
