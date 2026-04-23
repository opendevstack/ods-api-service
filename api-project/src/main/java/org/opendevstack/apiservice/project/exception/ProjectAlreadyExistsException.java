package org.opendevstack.apiservice.project.exception;

import lombok.Getter;

@Getter
public class ProjectAlreadyExistsException extends RuntimeException {

    public ProjectAlreadyExistsException() {
        super();
    }
}

