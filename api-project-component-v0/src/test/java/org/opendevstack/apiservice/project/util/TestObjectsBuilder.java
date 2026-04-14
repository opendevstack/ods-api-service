package org.opendevstack.apiservice.project.util;

import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionParameter;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class TestObjectsBuilder {

    private TestObjectsBuilder() {
    }

    public static Component buildTestComponent() {
        Component component = new Component();
        component.setId("testId");
        component.setName("testComponentName");
        component.environment("testEnv");
        component.setComponentType("testComponentType");
        return component;
    }

    public static ProjectComponentInfo buildTestMarketplaceComponent() {
        ProjectComponentInfo component = new ProjectComponentInfo();
        component.setComponentId("testComponentId");
        component.setCanBeDeleted(false);
        component.setComponentUrl("http://test.component.url");
        return component;
    }

    public static CreateComponentRequest buildTestCreateComponentRequest() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("testComponentName");
        request.setProductId("testProductId");
        return request;
    }

    public static List<ProvisionActionParameter> buildTestMarketplaceCreateComponentParameters() {
        List<ProvisionActionParameter> parameters = new ArrayList<>();
        parameters.add(new ProvisionActionParameter().name("name").type("string").value("testComponentName"));
        parameters.add(new ProvisionActionParameter().name("productId").type("string").value("testProductId"));
        return parameters;
    }

    public static CreateComponentResponse buildTestCreateComponentResponseSuccess(String projectId) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.CREATED.value());
        response.setMessage("Component created successfully in project " + projectId);
        return response;
    }

    public static CreateComponentResponse buildTestCreateComponentResponseFailure(String projectId) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("Failed to create component for project '" + projectId + "'");
        return response;
    }
}
