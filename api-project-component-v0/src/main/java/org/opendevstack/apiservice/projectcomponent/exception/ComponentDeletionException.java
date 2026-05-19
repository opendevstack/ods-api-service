package org.opendevstack.apiservice.projectcomponent.exception;

public class ComponentDeletionException extends RuntimeException {

    public ComponentDeletionException(String message) {
        super(message);
    }

    public ComponentDeletionException(String message, Exception e) {
        super(message, e);
    }
}
