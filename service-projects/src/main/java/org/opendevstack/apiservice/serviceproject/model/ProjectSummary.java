package org.opendevstack.apiservice.serviceproject.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Read-only projection used for list operations (e.g. GET /projects).
 * Carries the full set of fields needed by the API layer, including
 * audit timestamps and display fields not present in {@link ProjectResponse}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSummary {

    private String projectKey;

    private String projectName;

    private String projectFlavor;

    private String location;

    private Status status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}

