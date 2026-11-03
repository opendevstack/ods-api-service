package org.opendevstack.apiservice.externalservice.projectsinfoservice.config;

import org.opendevstack.apiservice.externalservice.api.http.ExternalServiceSslProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SSL configuration properties for the Projects Info Service external service.
 *
 * <p>Binds to the {@code externalservices.projects-info-service.ssl} prefix.
 */
@ConfigurationProperties(prefix = "externalservices.projects-info-service.ssl")
public class ProjectsInfoServiceSslProperties extends ExternalServiceSslProperties {
    // All fields inherited from ExternalServiceSslProperties.
    // Add Projects-Info-Service-specific SSL overrides here if ever needed.
}
