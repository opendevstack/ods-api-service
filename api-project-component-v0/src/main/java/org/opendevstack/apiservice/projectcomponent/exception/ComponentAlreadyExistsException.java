package org.opendevstack.apiservice.projectcomponent.exception;

public class ComponentAlreadyExistsException extends RuntimeException {

    public ComponentAlreadyExistsException(String message) {
        super(message);
    }

    public ComponentAlreadyExistsException(String message, Exception e) {
        super(message, e);
    }
}