package org.opendevstack.apiservice.externalservice.uipath.health;

import org.opendevstack.apiservice.externalservice.api.health.AbstractExternalServiceHealthIndicator;
import org.opendevstack.apiservice.externalservice.uipath.service.UiPathOrchestratorService;
import org.springframework.stereotype.Component;

/**
 * Health indicator for UIPath Orchestrator service.
 * Provides health status information for the actuator endpoint.
 */
@Component
public class UiPathHealthIndicator extends AbstractExternalServiceHealthIndicator {

    public UiPathHealthIndicator(UiPathOrchestratorService uiPathOrchestratorService) {
        super(uiPathOrchestratorService, "UIPath Orchestrator");
    }
}
