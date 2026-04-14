package org.opendevstack.apiservice.externalservice.marketplace.exception;

public class MarketplaceClientException extends Exception {

    public MarketplaceClientException(String message) {
        super(message);
    }

    public MarketplaceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
