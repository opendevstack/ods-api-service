package org.opendevstack.apiservice.project.exception;

public class ProjectKeyGenerationException extends Exception {

    public ProjectKeyGenerationException(String message) {
        super(message);
    }

    public ProjectKeyGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}