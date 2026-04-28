package org.opendevstack.apiservice.externalservice.marketplace.config;

import lombok.Data;

@Data
public class MarketplaceInstanceConfig {
    /**
     * The project components base URL of the Marketplace
     */
    private String projectComponentsBaseUrl;

    /**
     * The provisioner actions base URL of the Marketplace
     */
    private String provisionerActionsBaseUrl;

    /**
     * Username for authentication (used with password for basic auth).
     */
    private String username;

    /**
     * Password or personal access token for authentication.
     */
    private String password;

    /**
     * Connection timeout in milliseconds (default: 30000)
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

    /**
     * OAuth2 scope used for OBO token exchange when calling this Marketplace instance.
     * Example: {@code api://<marketplace-app-id>/Api.Access}
     */
    private String oboScope;
    
    private String workflow;
    
    private String odsNamespace;
    
    private String quickstarterRepository;
    
    private String catalogItemId;
}