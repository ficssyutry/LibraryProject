package com.example.library.service;

import com.example.library.entity.mongo.Id39586ErrorLog;
import com.example.library.repository.Id39586ErrorLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class Id39586MongoLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(Id39586MongoLoggingService.class);
    private final Id39586ErrorLogRepository errorLogRepository;

    public Id39586MongoLoggingService(Id39586ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @Async("taskExecutor")
    public void saveErrorLog(Id39586ErrorLog errorLog) {
        try {

            if (errorLog.getErrorId() == null) {
                errorLog.setErrorId(UUID.randomUUID().toString());
            }

            errorLogRepository.save(errorLog);
            logger.debug("Error log saved to MongoDB with ID: {}", errorLog.getErrorId());

        } catch (Exception e) {
            logger.error("Failed to save error log to MongoDB: {}", e.getMessage());
            logger.error("Original error: {}", errorLog.getMessage());
        }
    }

    @Async("taskExecutor")
    public void saveErrorLogWithRetry(Id39586ErrorLog errorLog, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                errorLogRepository.save(errorLog);
                logger.debug("Error log saved to MongoDB on attempt {}", attempt);
                return;
            } catch (Exception e) {
                logger.warn("Failed to save error log (attempt {}/{}): {}",
                        attempt, maxRetries, e.getMessage());

                if (attempt == maxRetries) {
                    logger.error("Failed to save error log after {} attempts", maxRetries);
                    logger.error("ERROR LOG FALLBACK: {}", errorLog.getMessage());
                }

                try {
                    Thread.sleep(100 * attempt); // Экспоненциальная задержка
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}