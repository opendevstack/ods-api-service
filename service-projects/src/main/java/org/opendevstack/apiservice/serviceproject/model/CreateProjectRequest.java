package org.opendevstack.apiservice.serviceproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    
    private String projectKey;
    
    private String projectKeyPattern;
    
    private String projectName;
    
    private String projectDescription;
}
