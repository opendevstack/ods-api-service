package org.opendevstack.apiservice.project.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.project.controller.ProjectController;
import org.opendevstack.apiservice.project.exception.ClientAppNotRegisteredException;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
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

    @ExceptionHandler(ClientAppNotRegisteredException.class)
    public ResponseEntity<CreateProjectResponse> handleClientAppNotRegisteredException(
            ClientAppNotRegisteredException ex) {
        log.warn("ClientApp registration error: {}", ex.getMessage());
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        response.setErrorKey(ErrorKey.CLIENT_APP_NOT_REGISTERED.getKey());
        response.setMessage(ErrorKey.CLIENT_APP_NOT_REGISTERED.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
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

    @ExceptionHandler(ProjectCreationException.class)
    public ResponseEntity<CreateProjectResponse> handleProjectCreationException(
            ProjectCreationException ex) {
        log.error("Project creation error: {}", ex.getMessage(), ex);
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(ErrorKey.PROJECT_ALREADY_EXISTS.getMessage());
        response.setErrorKey(ErrorKey.PROJECT_ALREADY_EXISTS.getKey());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ProjectKeyGenerationException.class)
    public ResponseEntity<CreateProjectResponse> handleProjectKeyGenerationException(
            ProjectKeyGenerationException ex) {
        log.error("Failed to generate project key: {}", ex.getMessage(), ex);
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(ErrorKey.INTERNAL_ERROR.getMessage());
        response.setErrorKey("PROJECT_KEY_GENERATION_FAILED");
        response.setMessage("Failed to generate a unique project key.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(AutomationPlatformException.class)
    public ResponseEntity<CreateProjectResponse> handleAutomationPlatformException(
            AutomationPlatformException ex) {
        log.error("Failed to execute automated job: {}", ex.getMessage(), ex);
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(ErrorKey.INTERNAL_ERROR.getMessage());
        response.setErrorKey(ErrorKey.INTERNAL_ERROR.getKey());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CreateProjectResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        CreateProjectResponse response = new CreateProjectResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(ErrorKey.INTERNAL_ERROR.getMessage());
        response.setErrorKey(ErrorKey.INTERNAL_ERROR.getKey());
        response.setMessage("An error occurred while processing the request.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }
}


