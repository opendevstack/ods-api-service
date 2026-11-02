package org.opendevstack.apiservice.feedback.mapper;

import java.util.Optional;
import java.util.UUID;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionRequest;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.openapitools.jackson.nullable.JsonNullable;

/** Maps between feedback API models and persisted submissions. */
@Mapper(componentModel = "spring", uses = FeedbackSubmissionJsonMapper.class,
		injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface FeedbackSubmissionMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "clientId", source = "clientApp.id")
	@Mapping(target = "clientAppRegistrationId", source = "clientApp.clientId")
	@Mapping(target = "clientName", source = "clientApp.clientName")
	@Mapping(target = "projectId", source = "project", qualifiedByName = "toProjectId")
	@Mapping(target = "projectKey",
			expression = "java(project.map(ProjectEntity::getProjectKey).orElse(request.getProjectKey()))")
	@Mapping(target = "submitter", source = "request.submitter")
	@Mapping(target = "submittedAt", ignore = true)
	@Mapping(target = "questions", source = "request.questions", qualifiedByName = "toQuestionsJson")
	@Mapping(target = "answers", source = "request.answers", qualifiedByName = "toAnswersJson")
	FeedbackSubmissionEntity toEntity(FeedbackSubmissionRequest request, Optional<ProjectEntity> project,
			ClientAppEntity clientApp);

	@Mapping(target = "projectKey", source = "projectKey", qualifiedByName = "toJsonNullableString")
	@Mapping(target = "questions", source = "questions", qualifiedByName = "toQuestionsList")
	@Mapping(target = "answers", source = "answers", qualifiedByName = "toAnswersMap")
	FeedbackSubmission toApiModel(FeedbackSubmissionEntity entity);

	@Named("toProjectId")
	default UUID toProjectId(Optional<ProjectEntity> project) {
		return project.map(ProjectEntity::getId).orElse(null);
	}

	@Named("toJsonNullableString")
	default JsonNullable<String> toJsonNullableString(String value) {
		return JsonNullable.of(value);
	}

}