package org.opendevstack.apiservice.serviceproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectResponse {
    
    private String projectKey;
    
    private String status;
    
    private String message;
    
    private String error;
    
    private String errorKey;
    
    private String errorDescription;
}
