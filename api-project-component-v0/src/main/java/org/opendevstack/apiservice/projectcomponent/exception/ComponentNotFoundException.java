package org.opendevstack.apiservice.projectcomponent.exception;

public class ComponentNotFoundException extends RuntimeException {

    public ComponentNotFoundException(String message) {
        super(message);
    }

    public ComponentNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
