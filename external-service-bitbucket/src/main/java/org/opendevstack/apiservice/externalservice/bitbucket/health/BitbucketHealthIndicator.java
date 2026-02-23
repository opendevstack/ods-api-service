package org.opendevstack.apiservice.externalservice.bitbucket.health;

import org.opendevstack.apiservice.externalservice.api.health.AbstractExternalServiceHealthIndicator;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.springframework.stereotype.Component;

/**
 * Health indicator for Bitbucket service.
 * Provides health status information for the actuator endpoint.
 */
@Component
public class BitbucketHealthIndicator extends AbstractExternalServiceHealthIndicator {

    public BitbucketHealthIndicator(BitbucketService bitbucketService) {
        super(bitbucketService, "Bitbucket");
    }
}
