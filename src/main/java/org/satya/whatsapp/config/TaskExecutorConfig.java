package org.satya.whatsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class TaskExecutorConfig {

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(1); // Adjust the pool size as needed
    }

    @Bean
    public DisposableBean disposableBean(ScheduledExecutorService scheduledExecutorService) {
        return () -> {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduledExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutorService.shutdownNow();
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        };
    }
}


