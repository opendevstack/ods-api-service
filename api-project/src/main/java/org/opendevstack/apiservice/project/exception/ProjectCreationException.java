package org.opendevstack.apiservice.project.exception;

public class ProjectCreationException extends Exception {

    public ProjectCreationException(String message) {
        super(message);
    }

    public ProjectCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}