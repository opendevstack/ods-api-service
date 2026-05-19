package org.opendevstack.apiservice.projectcomponent.exception;

public class ComponentRetrievalException extends RuntimeException {

    public ComponentRetrievalException(String message) {
        super(message);
    }

    public ComponentRetrievalException(String message, Exception e) {
        super(message, e);
    }
}