package com.monolith.notifications.infra;

import com.monolith.notifications.api.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorker {

    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "${notifications.worker.delay-ms:30000}", initialDelay = 10000)
    public void run() {
        log.debug("NotificationWorker running scheduled job to process pending notifications");
        try {
            notificationService.processPendingNotifications();
        } catch (Exception e) {
            log.error("Error occurred while processing pending notifications in worker: {}", e.getMessage(), e);
        }
    }
}
