package org.opendevstack.apiservice.serviceproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateProjectRequest {
    
    private String projectKey;
    
    private String projectKeyPattern;
    
    private String projectName;
    
    private String projectDescription;
}
