package org.opendevstack.apiservice.externalservice.commons;

import lombok.Getter;

/**
 * Base exception for all external service operations.
 * Provides common error handling with error codes and service context.
 */
@Getter
public class ExternalServiceException extends Exception {
    
    private final String errorCode;
    private final String serviceName;
    private final String operation;
    
    public ExternalServiceException(String message, String errorCode, String serviceName) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.operation = null;
    }
    
    public ExternalServiceException(String message, String errorCode, String serviceName, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.operation = operation;
    }
    
    public ExternalServiceException(String message, Throwable cause, String errorCode, String serviceName) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.operation = null;
    }
    
    public ExternalServiceException(String message, Throwable cause, String errorCode, String serviceName, String operation) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
        this.operation = operation;
    }
}
