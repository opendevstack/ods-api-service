package org.opendevstack.apiservice.core.security.client.credentials;

public class ClientCredentialsTokenException extends RuntimeException {

    public ClientCredentialsTokenException(String message) {
        super(message);
    }

    public ClientCredentialsTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
