package org.opendevstack.apiservice.project.util;

import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class TestHelper {

    public static Component buildTestComponent() {
        Component component = new Component();
        component.setId("testId");
        component.setName("testComponentName");
        component.environment("testEnv");
        component.setComponentType("testComponentType");
        return component;
    }

    public static CreateComponentRequest buildTestCreateComponentRequest() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("testComponentName");
        request.setProductId("testProductId");
        return request;
    }

    public static CreateComponentResponse buildTestCreateComponentResponseSuccess() {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.CREATED.value());
        response.setMessage("success");
        return response;
    }

    public static CreateComponentResponse buildTestCreateComponentResponseFailure() {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("failure");
        return response;
    }
}
