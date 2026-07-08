package org.opendevstack.apiservice.projectv1.controller.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.controller.ProjectController;
import org.opendevstack.apiservice.projectv1.exception.ErrorKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectExceptionHandlerTest {

    private ProjectExceptionHandler sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectExceptionHandler();
    }

    @Test
    void handle_constraint_violation_returns_bad_request_for_invalid_page() {
        ConstraintViolationException ex = createConstraintViolationException("page");

        ResponseEntity<GetProjectsResponse> result = sut.handleConstraintViolationException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INVALID_PAGE.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.INVALID_PAGE.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_constraint_violation_returns_bad_request_for_invalid_size() {
        ConstraintViolationException ex = createConstraintViolationException("size");

        ResponseEntity<GetProjectsResponse> result = sut.handleConstraintViolationException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INVALID_SIZE.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.INVALID_SIZE.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_constraint_violation_returns_bad_request_for_unknown_field() {
        ConstraintViolationException ex = createConstraintViolationException("unknownField");

        ResponseEntity<GetProjectsResponse> result = sut.handleConstraintViolationException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.BAD_REQUEST_BODY.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.BAD_REQUEST_BODY.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_generic_exception_returns_internal_server_error() {
        Exception ex = new RuntimeException("Unexpected database failure");

        ResponseEntity<GetProjectsResponse> result = sut.handleGenericException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(ErrorKey.INTERNAL_ERROR.getMessage());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INTERNAL_ERROR.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo("An error occurred while processing the request.");
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    // ------- helpers -------

    private ConstraintViolationException createConstraintViolationException(String fieldName) {
        Path.Node node = mock(Path.Node.class);
        when(node.getName()).thenReturn(fieldName);

        Path path = mock(Path.class);
        when(path.iterator()).thenReturn(List.of(node).iterator());

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);

        return new ConstraintViolationException(Set.of(violation));
    }
}
