package org.opendevstack.apiservice.projectcomponent.exception;

public class ComponentCreationException extends RuntimeException {

    public ComponentCreationException(String message) {
        super(message);
    }

    public ComponentCreationException(String message, Exception e) {
        super(message, e);
    }
}
