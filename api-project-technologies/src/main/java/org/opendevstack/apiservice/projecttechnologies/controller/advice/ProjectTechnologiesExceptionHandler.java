package org.opendevstack.apiservice.projecttechnologies.controller.advice;

import org.opendevstack.apiservice.projecttechnologies.controller.ProjectTechnologiesController;
import org.opendevstack.apiservice.projecttechnologies.model.RestErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = ProjectTechnologiesController.class)
public class ProjectTechnologiesExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ProjectTechnologiesExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<RestErrorMessage> handleResponseStatusException(ResponseStatusException exception) {
        String message = exception.getReason() == null ? "Request failed." : exception.getReason();
        log.warn("Project technologies request rejected with status {}: {}", exception.getStatusCode().value(), message);
        return ResponseEntity.status(exception.getStatusCode()).body(new RestErrorMessage().message(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> handleGenericException(Exception exception) {
        log.error("Unexpected error while retrieving project technologies", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestErrorMessage().message("An error occurred while processing the request."));
    }
}