package com.example.library.service;

import com.example.library.dto.response.Id39586UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class Id39586AsyncNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(Id39586AsyncNotificationService.class);

    @Async("taskExecutor")
    public void sendWelcomeEmailAsync(Id39586UserResponse user) {
        try {
            logger.info("Sending welcome email to: {} ({})", user.getEmail(), Thread.currentThread().getName());

            Thread.sleep(2000);

            logger.info("Welcome email sent successfully to: {}", user.getEmail());
        } catch (InterruptedException e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
            Thread.currentThread().interrupt();
        }
    }

    @Async("taskExecutor")
    public void sendAccountUpdateNotificationAsync(Id39586UserResponse user, String changeType) {
        try {
            logger.info("Sending account update notification to: {} - Change: {}",
                    user.getEmail(), changeType);

            Thread.sleep(1000);

            logger.info("Account update notification sent to: {}", user.getEmail());
        } catch (InterruptedException e) {
            logger.error("Failed to send update notification to: {}", user.getEmail(), e);
            Thread.currentThread().interrupt();
        }
    }

    @Async("taskExecutor")
    public void logUserActionAsync(Long userId, String action, String details) {
        logger.info("ASYNC LOG: User {} performed action '{}' with details: {} - Thread: {}",
                userId, action, details, Thread.currentThread().getName());



    }
}