package org.opendevstack.apiservice.externalservice.jira.health;

import org.opendevstack.apiservice.externalservice.api.health.AbstractExternalServiceHealthIndicator;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.springframework.stereotype.Component;

/**
 * Health indicator for the Jira service.
 * Provides health status information for the Spring Boot Actuator endpoint.
 * The health check performs a live call to the Jira {@code /rest/api/2/serverInfo} endpoint.
 */
@Component
public class JiraHealthIndicator extends AbstractExternalServiceHealthIndicator {

    public JiraHealthIndicator(JiraService jiraService) {
        super(jiraService, "Jira");
    }
}
