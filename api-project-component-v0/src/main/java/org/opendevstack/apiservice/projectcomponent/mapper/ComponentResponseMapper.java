package org.opendevstack.apiservice.projectcomponent.mapper;

import org.mapstruct.Mapper;
import org.opendevstack.apiservice.projectcomponent.client.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Mapper(componentModel = "spring")
public interface ComponentResponseMapper {

    default ResponseEntity<CreateComponentResponse> toResponseEntity(CreateComponentResponse response) {
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getHttpStatus()));
    }
}
