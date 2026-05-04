package org.opendevstack.apiservice.externalservice.aap.config;

import org.opendevstack.apiservice.externalservice.api.http.ExternalServiceSslProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SSL configuration properties for the Ansible Automation Platform external service.
 * Binds to the {@code automation.platform.ansible.ssl} prefix.
 */
@Component("aapSslProperties")
@ConfigurationProperties(prefix = "automation.platform.ansible.ssl")
public class SslProperties extends ExternalServiceSslProperties {
    // All fields inherited from ExternalServiceSslProperties.
    // Add AAP-specific SSL overrides here if ever needed.
}
