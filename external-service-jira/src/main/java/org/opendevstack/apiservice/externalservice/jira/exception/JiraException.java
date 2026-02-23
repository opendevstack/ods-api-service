package org.opendevstack.apiservice.externalservice.jira.exception;

/**
 * Exception thrown when Jira API operations fail.
 */
public class JiraException extends Exception {

    public JiraException(String message) {
        super(message);
    }

    public JiraException(String message, Throwable cause) {
        super(message, cause);
    }
}
