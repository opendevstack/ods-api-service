package org.opendevstack.apiservice.projectusers.controller.advice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.projectusers.controller.ProjectUserController;
import org.opendevstack.apiservice.projectusers.model.AddUserToProjectRequest;
import org.opendevstack.apiservice.projectusers.model.ValidationErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
/**
 * Unit test class for the ProjectUserExceptionHandler to verify improved validation
 * error messages.
 */
class ProjectUserExceptionHandlerTest {
    private ProjectUserExceptionHandler sut;
    private AutoCloseable mocks;
    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        sut = new ProjectUserExceptionHandler();
    }
    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
    @Test
    void handle_method_argument_not_valid_exception_returns_bad_request_with_field_errors() {
        // GIVEN
        AddUserToProjectRequest target = new AddUserToProjectRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "addUserToProjectRequest");
        bindingResult.addError(new FieldError("addUserToProjectRequest", "environment", null, false, null, null,
                "Environment cannot be blank"));
        bindingResult.addError(
                new FieldError("addUserToProjectRequest", "user", null, false, null, null, "User cannot be blank"));
        bindingResult.addError(new FieldError("addUserToProjectRequest", "account", null, false, null, null,
                "Account cannot be blank"));
        bindingResult.addError(
                new FieldError("addUserToProjectRequest", "role", null, false, null, null, "Role cannot be null"));
        MethodParameter methodParameter;
        try {
            methodParameter = new MethodParameter(
                    ProjectUserController.class.getMethod(
                            "triggerMembershipRequest", String.class, AddUserToProjectRequest.class),
                    1);
        } catch (NoSuchMethodException e) {
            fail("Failed to reflect controller method for test: " + e.getMessage());
            return;
        }
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);
        // WHEN
        ResponseEntity<ValidationErrorResponse> response = sut.handleMethodArgumentNotValidException(exception);
        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        ValidationErrorResponse errorResponse = response.getBody();
        assertFalse(errorResponse.getSuccess());
        assertEquals("PROJECT_USER_ERROR", errorResponse.getErrorCode());
        assertNotNull(errorResponse.getFieldErrors());
        assertEquals(4, errorResponse.getFieldErrors().size());
        List<org.opendevstack.apiservice.projectusers.model.FieldError> fieldErrors = errorResponse.getFieldErrors();
        assertTrue(fieldErrors.stream().anyMatch(error -> "environment".equals(error.getField())));
        assertTrue(fieldErrors.stream().anyMatch(error -> "user".equals(error.getField())));
        assertTrue(fieldErrors.stream().anyMatch(error -> "account".equals(error.getField())));
        assertTrue(fieldErrors.stream().anyMatch(error -> "role".equals(error.getField())));
        fieldErrors.forEach(fieldError -> {
            assertNotNull(fieldError.getField());
            assertNotNull(fieldError.getMessage());
            if (!"object".equals(fieldError.getField())) {
                assertNotNull(fieldError.getExpectedFormat(),
                        "Expected format should be provided for field: " + fieldError.getField());
            }
        });
    }
    @Test
    void handle_generic_exception_returns_internal_server_error() {
        // GIVEN
        Exception exception = new RuntimeException("Unexpected error");
        // WHEN
        ResponseEntity<?> response = sut.handleGenericException(exception);
        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
