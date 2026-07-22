package org.opendevstack.apiservice.projectv1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.opendevstack.apiservice.projectv1.client.model.ProjectsResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
@Component("projectMapperV1")
public interface ProjectMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "formatDateTime")
    ProjectsResponse toProjectsResponse(ProjectSummary projectSummary);

    List<ProjectsResponse> toProjectsResponse(List<ProjectSummary> projectSummaryList);

    @Named("mapStatus")
    default String mapStatus(Status status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Named("formatDateTime")
    default String formatDateTime(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }
}