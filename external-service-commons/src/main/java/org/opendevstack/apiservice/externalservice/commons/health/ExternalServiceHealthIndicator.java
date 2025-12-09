package org.opendevstack.apiservice.externalservice.commons.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Health indicator for an external service.
 * Integrates with Spring Boot Actuator to expose service health status.
 */
@Slf4j
@RequiredArgsConstructor
public class ExternalServiceHealthIndicator implements HealthIndicator {
    
    private final ExternalService service;
    
    @Override
    public Health health() {
        try {
            String serviceName = service.getServiceName();
            
            // Check if service can validate connection
            boolean canConnect = service.validateConnection();
            
            // Check if service is healthy
            boolean isHealthy = service.isHealthy();
            
            if (canConnect && isHealthy) {
                return Health.up()
                    .withDetail("service", serviceName)
                    .withDetail("status", "operational")
                    .build();
            } else if (canConnect) {
                return Health.down()
                    .withDetail("service", serviceName)
                    .withDetail("status", "unhealthy")
                    .withDetail("canConnect", true)
                    .build();
            } else {
                return Health.down()
                    .withDetail("service", serviceName)
                    .withDetail("status", "connection_failed")
                    .withDetail("canConnect", false)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Error checking health for service", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
