package org.opendevstack.apiservice.externalservice.api.http;

import lombok.Data;

/**
 * Common SSL configuration properties for external service HTTP clients.
 *
 * <p>Each external service module declares its own
 * {@link org.springframework.boot.context.properties.ConfigurationProperties} binding with
 * its own prefix. This class is intentionally <em>not</em> annotated with
 * {@code @ConfigurationProperties} or {@code @Component} so that every module can bind it under
 * whichever YAML prefix it needs, e.g.:
 *
 * <pre>{@code
 * @ConfigurationProperties(prefix = "automation.platform.ansible.ssl")
 * public class AapSslProperties extends ExternalServiceSslProperties { ... }
 * }</pre>
 *
 * or embedded directly inside a parent properties class:
 *
 * <pre>{@code
 * public class UiPathProperties {
 *     private ExternalServiceSslProperties ssl = new ExternalServiceSslProperties();
 * }
 * }</pre>
 */
@Data
public class ExternalServiceSslProperties {

    /**
     * Whether to verify SSL certificates when making external service calls.
     * Default is {@code true} for security. Set to {@code false} only in
     * development/test environments.
     */
    private boolean verifyCertificates = true;

    /**
     * Path to the trust store file for SSL certificate validation.
     * Optional — when not set the JVM default trust store is used.
     */
    private String trustStorePath;

    /**
     * Password for the trust store file. May be empty for passwordless stores.
     */
    private String trustStorePassword;

    /**
     * Type of the trust store ({@code JKS}, {@code PKCS12}, etc.).
     * Default is {@code JKS}.
     */
    private String trustStoreType = "JKS";
}
