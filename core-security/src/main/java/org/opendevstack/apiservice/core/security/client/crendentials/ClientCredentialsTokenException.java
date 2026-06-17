package org.opendevstack.apiservice.core.security.client.crendentials;

public class ClientCredentialsTokenException extends RuntimeException {

    public ClientCredentialsTokenException(String message) {
        super(message);
    }

    public ClientCredentialsTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
