package org.opendevstack.apiservice.externalservice.ocp.exception;

import lombok.Getter;

/**
 * Exception for OpenShift service operations with structured error context.
 * Provides error codes and operation context for better error handling and diagnostics.
 */
@Getter
public class OpenshiftException extends Exception {
    
    /**
     * Error code categorizing the type of failure
     */
    private final String errorCode;
    
    /**
     * The operation that was being performed when the error occurred
     */
    private final String operation;
    
    /**
     * The OpenShift instance name where the error occurred
     */
    private final String instanceName;
    
    /**
     * Create exception with message only (for backward compatibility)
     * @deprecated Use constructor with error code and context
     */
    @Deprecated
    public OpenshiftException(String message) {
        super(message);
        this.errorCode = "OPENSHIFT_ERROR";
        this.operation = null;
        this.instanceName = null;
    }
    
    /**
     * Create exception with message and cause (for backward compatibility)
     * @deprecated Use constructor with error code and context
     */
    @Deprecated
    public OpenshiftException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "OPENSHIFT_ERROR";
        this.operation = null;
        this.instanceName = null;
    }
    
    /**
     * Create exception with full context
     * 
     * @param message Error message
     * @param errorCode Error code (e.g., CONNECTION_TIMEOUT, UNAUTHORIZED, NOT_FOUND)
     * @param operation The operation being performed (e.g., "getSecret", "validateConnection")
     * @param instanceName The OpenShift instance name
     */
    public OpenshiftException(String message, String errorCode, String operation, String instanceName) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.instanceName = instanceName;
    }
    
    /**
     * Create exception with full context and cause
     * 
     * @param message Error message
     * @param cause The underlying cause
     * @param errorCode Error code (e.g., CONNECTION_TIMEOUT, UNAUTHORIZED, NOT_FOUND)
     * @param operation The operation being performed (e.g., "getSecret", "validateConnection")
     * @param instanceName The OpenShift instance name
     */
    public OpenshiftException(String message, Throwable cause, String errorCode, String operation, String instanceName) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
        this.instanceName = instanceName;
    }
    
    /**
     * Error codes for different failure scenarios
     */
    public static final class ErrorCodes {
        private ErrorCodes() {} // Utility class
        
        // Network & Connection Errors
        public static final String CONNECTION_FAILED = "CONNECTION_FAILED";
        public static final String CONNECTION_TIMEOUT = "CONNECTION_TIMEOUT";
        public static final String READ_TIMEOUT = "READ_TIMEOUT";
        public static final String SSL_ERROR = "SSL_ERROR";
        public static final String DNS_RESOLUTION_FAILED = "DNS_RESOLUTION_FAILED";
        
        // Authentication & Authorization
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String FORBIDDEN = "FORBIDDEN";
        public static final String INVALID_TOKEN = "INVALID_TOKEN";
        
        // Resource Errors
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String SECRET_NOT_FOUND = "SECRET_NOT_FOUND";
        public static final String NAMESPACE_NOT_FOUND = "NAMESPACE_NOT_FOUND";
        public static final String KEY_NOT_FOUND = "KEY_NOT_FOUND";
        
        // Service Errors
        public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
        public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
        public static final String BAD_REQUEST = "BAD_REQUEST";
        
        // Parsing & Data Errors
        public static final String PARSE_ERROR = "PARSE_ERROR";
        public static final String INVALID_RESPONSE = "INVALID_RESPONSE";
        
        // Configuration Errors
        public static final String INSTANCE_NOT_CONFIGURED = "INSTANCE_NOT_CONFIGURED";
        public static final String INVALID_CONFIGURATION = "INVALID_CONFIGURATION";
        
        // Generic
        public static final String OPENSHIFT_ERROR = "OPENSHIFT_ERROR";
    }
}
