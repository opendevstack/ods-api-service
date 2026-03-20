package org.opendevstack.apiservice.project.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectRequestValidatorTest {

    private ProjectRequestValidator sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectRequestValidator();
    }

    @Test
    void validate_throws_exception_when_project_flavor_and_config_item_both_null() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid Name");
        request.setProjectFlavor(null);
        request.setConfigurationItem(null);

        ProjectValidationException exception = assertThrows(
            ProjectValidationException.class,
            () -> sut.validate(request)
        );

        assertEquals(ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM, exception.getErrorKey());
    }

    @Test
    void validate_throws_exception_when_project_flavor_and_config_item_both_empty() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid Name");
        request.setProjectFlavor("");
        request.setConfigurationItem("  ");

        ProjectValidationException exception = assertThrows(
            ProjectValidationException.class,
            () -> sut.validate(request)
        );

        assertEquals(ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM, exception.getErrorKey());
    }

    @Test
    void validate_succeeds_when_project_flavor_provided() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid Name");
        request.setProjectFlavor("STANDARD");
        request.setConfigurationItem(null);

        assertDoesNotThrow(() -> sut.validate(request));
    }

    @Test
    void validate_succeeds_when_config_item_provided() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid Name");
        request.setProjectFlavor(null);
        request.setConfigurationItem("JIRA");

        assertDoesNotThrow(() -> sut.validate(request));
    }

    @Test
    void validate_succeeds_when_both_flavor_and_config_item_provided() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid Name");
        request.setProjectFlavor("STANDARD");
        request.setConfigurationItem("JIRA");

        assertDoesNotThrow(() -> sut.validate(request));
    }

    @Test
    void validate_throws_exception_for_invalid_project_key_format() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid Name");
        request.setProjectFlavor("STANDARD");
        request.setProjectKey("invalid-key"); // Invalid format

        ProjectValidationException exception = assertThrows(
            ProjectValidationException.class,
            () -> sut.validate(request)
        );

        assertEquals(ErrorKey.PROJECT_KEY_INVALID_FORMAT, exception.getErrorKey());
    }

    @Test
    void validate_throws_exception_for_invalid_project_name_format() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Invalid@Name#");
        request.setProjectFlavor("STANDARD");

        ProjectValidationException exception = assertThrows(
            ProjectValidationException.class,
            () -> sut.validate(request)
        );

        assertEquals(ErrorKey.PROJECT_NAME_INVALID_FORMAT, exception.getErrorKey());
    }
}