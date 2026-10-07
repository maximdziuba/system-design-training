package com.monolith.notifications.infra;

import com.monolith.notifications.api.NotificationService;
import com.monolith.notifications.api.dto.NotificationResponse;
import com.monolith.notifications.api.dto.NotificationStatus;
import com.monolith.notifications.api.dto.SendNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("Sending notification via [{}] to recipient: {}", request.getChannel(), request.getRecipient());

        NotificationEntity entity = NotificationEntity.builder()
                .userId(request.getUserId())
                .channel(request.getChannel())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(NotificationStatus.PENDING)
                .build();

        NotificationEntity saved = notificationRepository.save(entity);

        // Deliver notification via channel gateway
        try {
            deliverNotification(saved);
            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(Instant.now());
            log.info("Notification successfully delivered to {}", saved.getRecipient());
        } catch (Exception e) {
            log.error("Failed to deliver notification to {}: {}", saved.getRecipient(), e.getMessage());
            saved.setStatus(NotificationStatus.FAILED);
        }

        NotificationEntity updated = notificationRepository.save(saved);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUserId(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void processPendingNotifications() {
        List<NotificationEntity> pendingList = notificationRepository.findByStatus(NotificationStatus.PENDING);
        if (pendingList.isEmpty()) {
            return;
        }

        log.info("Processing {} pending notifications", pendingList.size());
        for (NotificationEntity entity : pendingList) {
            try {
                deliverNotification(entity);
                entity.setStatus(NotificationStatus.SENT);
                entity.setSentAt(Instant.now());
                notificationRepository.save(entity);
            } catch (Exception e) {
                log.error("Failed retrying notification {}: {}", entity.getId(), e.getMessage());
                entity.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(entity);
            }
        }
    }

    private void deliverNotification(NotificationEntity entity) {
        // Notification gateway integration simulation (email/SMS)
        log.info("Delivering [{}] subject: '{}' to '{}'",
                entity.getChannel(), entity.getSubject(), entity.getRecipient());
    }

    private NotificationResponse mapToResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .channel(entity.getChannel())
                .recipient(entity.getRecipient())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .sentAt(entity.getSentAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
