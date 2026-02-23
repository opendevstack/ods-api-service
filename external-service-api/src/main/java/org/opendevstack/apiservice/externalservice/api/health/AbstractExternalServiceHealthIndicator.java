package org.opendevstack.apiservice.externalservice.api.health;

import org.opendevstack.apiservice.externalservice.api.ExternalService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for external service health indicators.
 * Provides standard health check implementation that can be reused by all external services.
 * <p>
 * Subclasses should extend this class and provide the service name and external service instance.
 * </p>
 */
@Slf4j
public abstract class AbstractExternalServiceHealthIndicator implements HealthIndicator {

    private static final String DETAIL_SERVICE = "service";
    private static final String DETAIL_STATUS = "status";
    private static final String STATUS_CONNECTED = "connected";
    private static final String STATUS_DISCONNECTED = "disconnected";
    private static final String STATUS_ERROR = "error";
    private static final String DETAIL_REASON = "reason";
    private static final String DETAIL_ERROR = "error";
    private static final String HEALTH_CHECK_FAILED = "Health check failed";

    private final ExternalService externalService;
    private final String serviceName;

    /**
     * Constructor for the abstract health indicator.
     *
     * @param externalService the external service to check health for
     * @param serviceName the display name of the service for health reporting
     */
    protected AbstractExternalServiceHealthIndicator(ExternalService externalService, String serviceName) {
        this.externalService = externalService;
        this.serviceName = serviceName;
    }

    @Override
    public Health health() {
        try {
            boolean isHealthy = externalService.isHealthy();
            if (isHealthy) {
                log.debug("{} service is healthy", serviceName);
                return Health.up()
                    .withDetail(DETAIL_SERVICE, serviceName)
                    .withDetail(DETAIL_STATUS, STATUS_CONNECTED)
                    .build();
            } else {
                log.warn("{} service is not healthy", serviceName);
                return Health.down()
                    .withDetail(DETAIL_SERVICE, serviceName)
                    .withDetail(DETAIL_STATUS, STATUS_DISCONNECTED)
                    .withDetail(DETAIL_REASON, HEALTH_CHECK_FAILED)
                    .build();
            }
        } catch (Exception e) {
            log.error("Error checking {} health: {}", serviceName, e.getMessage(), e);
            return Health.down()
                .withDetail(DETAIL_SERVICE, serviceName)
                .withDetail(DETAIL_STATUS, STATUS_ERROR)
                .withDetail(DETAIL_ERROR, e.getMessage())
                .build();
        }
    }
}
