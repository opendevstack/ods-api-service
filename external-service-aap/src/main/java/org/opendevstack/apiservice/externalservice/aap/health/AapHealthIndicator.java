package org.opendevstack.apiservice.externalservice.aap.health;

import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.externalservice.api.health.AbstractExternalServiceHealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for Ansible Automation Platform (AAP) service.
 * Provides health status information for the actuator endpoint.
 */
@Component
public class AapHealthIndicator extends AbstractExternalServiceHealthIndicator {

    public AapHealthIndicator(AutomationPlatformService automationPlatformService) {
        super(automationPlatformService, "Ansible Automation Platform");
    }
}
