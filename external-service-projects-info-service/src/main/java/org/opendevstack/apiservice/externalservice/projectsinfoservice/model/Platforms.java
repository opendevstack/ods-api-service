package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import java.util.List;

/**
 * Represents the platforms information associated to a project.
 */
public record Platforms(List<PlatformSection> sections) {
}
