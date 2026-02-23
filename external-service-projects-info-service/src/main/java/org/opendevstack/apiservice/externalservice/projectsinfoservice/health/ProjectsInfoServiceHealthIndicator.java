package org.opendevstack.apiservice.externalservice.projectsinfoservice.health;

import org.opendevstack.apiservice.externalservice.api.health.AbstractExternalServiceHealthIndicator;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import org.springframework.stereotype.Component;

/**
 * Health indicator for Projects Info Service.
 * Provides health status information for the actuator endpoint.
 */
@Component
public class ProjectsInfoServiceHealthIndicator extends AbstractExternalServiceHealthIndicator {

    public ProjectsInfoServiceHealthIndicator(ProjectsInfoService projectsInfoService) {
        super(projectsInfoService, "Projects Info Service");
    }
}
