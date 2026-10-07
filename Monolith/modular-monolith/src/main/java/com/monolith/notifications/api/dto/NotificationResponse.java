package com.monolith.notifications.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private NotificationChannel channel;
    private String recipient;
    private String subject;
    private String message;
    private NotificationStatus status;
    private Instant sentAt;
    private Instant createdAt;
}
