package com.monolith.notifications.api;

import com.monolith.notifications.api.dto.NotificationResponse;
import com.monolith.notifications.api.dto.SendNotificationRequest;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    NotificationResponse sendNotification(SendNotificationRequest request);
    List<NotificationResponse> getNotificationsByUserId(UUID userId);
    void processPendingNotifications();
}
