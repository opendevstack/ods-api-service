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
     * Authentication access token for accessing the Marketplace API
     */
    private String accessToken;

    /**
     * Authentication bearer token for accessing the Marketplace API
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
    
    private String workflow;
    
    private String odsNamespace;
    
    private String quickstarterRepository;
    
    private String catalogItemId;
}