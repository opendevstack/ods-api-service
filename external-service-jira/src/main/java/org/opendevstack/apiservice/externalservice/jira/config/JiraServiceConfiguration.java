package org.opendevstack.apiservice.externalservice.jira.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Jira service.
 * Loads multiple Jira instance configurations from Spring configuration files.
 *
 * <p>Example YAML configuration:
 * <pre>
 * externalservices:
 *   jira:
 *     default-instance: dev
 *     instances:
 *       dev:
 *         base-url: https://jira.dev.example.com
 *         bearer-token: ${JIRA_DEV_BEARER_TOKEN:}
 *       prod:
 *         base-url: https://jira.example.com
 *         username: admin
 *         password: secret
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "externalservices.jira")
@Data
public class JiraServiceConfiguration {

    /**
     * Name of the default Jira instance to use when no instance name is provided.
     * If not set, the first configured instance is used as default.
     */
    private String defaultInstance;

    /**
     * Map of Jira instances with the instance name as the key and the configuration as the value.
     */
    private Map<String, JiraInstanceConfig> instances = new HashMap<>();

    /**
     * Configuration for a single Jira instance.
     */
    @Data
    public static class JiraInstanceConfig {

        /**
         * The base URL of the Jira server (e.g., https://jira.example.com).
         */
        private String baseUrl;

        /**
         * Bearer token for authentication (preferred method).
         * If provided, this will be used instead of username/password.
         */
        private String bearerToken;

        /**
         * Username for authentication (used with password for basic auth).
         * Only used if bearerToken is not provided.
         */
        private String username;

        /**
         * Password or personal access token for authentication.
         * Only used if bearerToken is not provided.
         */
        private String password;

        /**
         * Connection timeout in milliseconds (default: 30000).
         */
        private int connectionTimeout = 30000;

        /**
         * Read timeout in milliseconds (default: 30000).
         */
        private int readTimeout = 30000;

        /**
         * Whether to trust all SSL certificates (default: false).
         * WARNING: Should only be used in development environments.
         */
        private boolean trustAllCertificates = false;
    }
}
