package org.opendevstack.apiservice.marketplacemetrics.exception;

/**
 * Exception thrown when there are issues with projects info service operations.
 */
public class MarketplaceMetricsException extends Exception {

    public MarketplaceMetricsException(String message) {
        super(message);
    }

    public MarketplaceMetricsException(String message, Throwable cause) {
        super(message, cause);
    }
}
