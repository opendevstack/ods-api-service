package org.opendevstack.apiservice.project.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.controller.ProjectController;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = ProjectController.class)
@Slf4j
public class ProjectExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CreateProjectResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        log.warn("Request body validation error: {}", ex.getMessage());

        String validationMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        if (validationMessage.isBlank()) {
            validationMessage = ErrorKey.BAD_REQUEST_BODY.getMessage();
        }

        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.setErrorKey(ErrorKey.BAD_REQUEST_BODY.getKey());
        response.setMessage(validationMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ProjectValidationException.class)
    public ResponseEntity<CreateProjectResponse> handleValidationException(ProjectValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        ErrorKey errorKey = ex.getErrorKey();
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.setErrorKey(errorKey.getKey());
        response.setMessage(errorKey.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }
}


