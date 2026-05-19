package org.opendevstack.apiservice.projectcomponent.exception;

public class ComponentBadRequestException extends RuntimeException {

    public ComponentBadRequestException(String message) {
        super(message);
    }

    public ComponentBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
