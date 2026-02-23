package org.opendevstack.apiservice.externalservice.webhookproxy.health;

import org.opendevstack.apiservice.externalservice.api.health.AbstractExternalServiceHealthIndicator;
import org.opendevstack.apiservice.externalservice.webhookproxy.service.WebhookProxyService;
import org.springframework.stereotype.Component;

/**
 * Health indicator for WebhookProxy service.
 * Provides health status information for the actuator endpoint.
 */
@Component
public class WebhookProxyHealthIndicator extends AbstractExternalServiceHealthIndicator {

    public WebhookProxyHealthIndicator(WebhookProxyService webhookProxyService) {
        super(webhookProxyService, "WebhookProxy");
    }
}
