package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import java.util.List;

/**
 * Represents a section of links from a specific platform associated to a project.
 */
public record PlatformSection (
        String section,
        String tooltip,
        List<PlatformSectionLink> links
){
}
