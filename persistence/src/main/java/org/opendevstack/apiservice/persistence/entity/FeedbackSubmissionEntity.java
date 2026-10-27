package org.opendevstack.apiservice.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

/**
 * JPA entity mapping the {@code feedback_submissions} table.
 */
@Entity
@Table(name = "feedback_submissions")
@Getter
@Setter
@NoArgsConstructor
public class FeedbackSubmissionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "client_id", nullable = false)
	private UUID clientId;

	@Column(name = "client_app_registration_id", nullable = false)
	private UUID clientAppRegistrationId;

	@Column(name = "client_name", nullable = false, length = 255)
	private String clientName;

	@Column(name = "project_id")
	private UUID projectId;

	@Column(name = "project_key", length = 100)
	private String projectKey;

	@Column(name = "submitter", length = 255)
	private String submitter;

	@Column(name = "submitted_at", nullable = false, insertable = false, updatable = false,
			columnDefinition = "TIMESTAMPTZ")
	@Generated(event = EventType.INSERT)
	private OffsetDateTime submittedAt;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "questions", columnDefinition = "jsonb", nullable = false)
	private JsonNode questions;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "answers", columnDefinition = "jsonb", nullable = false)
	private JsonNode answers;

}