package org.opendevstack.apiservice.externalservice.api.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.api.ExternalService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AbstractExternalServiceHealthIndicator}.
 * Tests the health check functionality when external services are healthy, unhealthy, or throws exceptions.
 */
@ExtendWith(MockitoExtension.class)
class AbstractExternalServiceHealthIndicatorTest {

    @Mock
    private ExternalService externalService;

    /**
     * Concrete implementation of the abstract health indicator for testing purposes.
     */
    private static class TestHealthIndicator extends AbstractExternalServiceHealthIndicator {
        public TestHealthIndicator(ExternalService externalService, String serviceName) {
            super(externalService, serviceName);
        }
    }

    @Test
    void testHealthWhenServiceIsHealthy() {
        // Arrange
        when(externalService.isHealthy()).thenReturn(true);
        AbstractExternalServiceHealthIndicator indicator = 
            new TestHealthIndicator(externalService, "TestService");

        // Act
        Health health = indicator.health();

        // Assert
        assertNotNull(health);
        assertEquals(Status.UP, health.getStatus());
        assertEquals("TestService", health.getStatus().getDescription());
    }

    @Test
    void testHealthWhenServiceIsUnhealthy() {
        // Arrange
        when(externalService.isHealthy()).thenReturn(false);
        AbstractExternalServiceHealthIndicator indicator = 
            new TestHealthIndicator(externalService, "TestService");

        // Act
        Health health = indicator.health();

        // Assert
        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void testHealthWhenServiceThrowsException() {
        // Arrange
        String errorMessage = "Connection timeout";
        when(externalService.isHealthy()).thenThrow(new RuntimeException(errorMessage));
        AbstractExternalServiceHealthIndicator indicator = 
            new TestHealthIndicator(externalService, "TestService");

        // Act
        Health health = indicator.health();

        // Assert
        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void testHealthWhenServiceThrowsCheckedException() {
        // Arrange
        String errorMessage = "Database connection error";
        when(externalService.isHealthy()).thenThrow(new IllegalStateException(errorMessage));
        AbstractExternalServiceHealthIndicator indicator = 
            new TestHealthIndicator(externalService, "DatabaseService");

        // Act
        Health health = indicator.health();

        // Assert
        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void testHealthStatusIsUp() {
        // Arrange
        when(externalService.isHealthy()).thenReturn(true);
        AbstractExternalServiceHealthIndicator indicator = 
            new TestHealthIndicator(externalService, "TestService");

        // Act
        Health health = indicator.health();

        // Assert - Verify status is UP and not null
        assertNotNull(health);
        assertNotNull(health.getStatus());
        assertEquals(Status.UP, health.getStatus());
    }

}

