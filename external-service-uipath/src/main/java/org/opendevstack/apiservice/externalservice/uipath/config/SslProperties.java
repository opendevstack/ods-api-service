package org.opendevstack.apiservice.externalservice.uipath.config;

import org.opendevstack.apiservice.externalservice.api.http.ExternalServiceSslProperties;

/**
 * SSL configuration properties for the UiPath Orchestrator external service.
 *
 * <p>Bound as a nested field inside {@link UiPathProperties} under the
 * {@code automation.platform.uipath.ssl.*} prefix — no standalone
 * {@code @ConfigurationProperties} annotation needed.
 */
public class SslProperties extends ExternalServiceSslProperties {
    // All fields inherited from ExternalServiceSslProperties.
    // Add UiPath-specific SSL overrides here if ever needed.
}
