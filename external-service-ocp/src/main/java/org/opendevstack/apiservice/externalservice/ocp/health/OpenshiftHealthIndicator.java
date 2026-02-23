package org.opendevstack.apiservice.externalservice.ocp.health;

import org.opendevstack.apiservice.externalservice.api.health.AbstractExternalServiceHealthIndicator;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.springframework.stereotype.Component;

/**
 * Health indicator for OpenShift service.
 * Provides health status information for the actuator endpoint.
 */
@Component
public class OpenshiftHealthIndicator extends AbstractExternalServiceHealthIndicator {

    public OpenshiftHealthIndicator(OpenshiftService openshiftService) {
        super(openshiftService, "OpenShift");
    }
}
