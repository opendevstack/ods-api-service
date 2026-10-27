package org.opendevstack.apiservice.persistence.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/** Integration tests for feedback submission filtering against PostgreSQL. */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FeedbackSubmissionRepositoryIntegrationTest {

	@Container
	@SuppressWarnings("resource")
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
		.withDatabaseName("feedback_test")
		.withUsername("test")
		.withPassword("test");

	@DynamicPropertySource
	static void postgresProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.hikari.maximum-pool-size", () -> "2");
		registry.add("spring.datasource.hikari.minimum-idle", () -> "0");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private FeedbackSubmissionRepository repository;

	@Test
	void filtersByClientAndInclusiveDateBoundsInDescendingOrder() {
		UUID clientAppRegistrationId = UUID.randomUUID();
		OffsetDateTime startDate = OffsetDateTime.parse("2025-01-02T00:00:00Z");
		OffsetDateTime endDate = OffsetDateTime.parse("2025-03-01T00:00:00Z");

		UUID beforeStart = saveSubmission(clientAppRegistrationId, OffsetDateTime.parse("2025-01-01T00:00:00Z"));
		UUID atStart = saveSubmission(clientAppRegistrationId, startDate);
		UUID insideRange = saveSubmission(clientAppRegistrationId, OffsetDateTime.parse("2025-02-01T00:00:00Z"));
		UUID atEnd = saveSubmission(clientAppRegistrationId, endDate);
		UUID afterEnd = saveSubmission(clientAppRegistrationId, OffsetDateTime.parse("2025-03-02T00:00:00Z"));
		saveSubmission(UUID.randomUUID(), OffsetDateTime.parse("2025-02-15T00:00:00Z"));

		assertThat(repository.findByClientAppRegistrationIdOrderBySubmittedAtDesc(clientAppRegistrationId,
				PageRequest.of(0, 20)).getContent())
			.extracting(FeedbackSubmissionEntity::getId)
			.containsExactly(afterEnd, atEnd, insideRange, atStart, beforeStart);
		assertThat(repository
			.findByClientAppRegistrationIdAndSubmittedAtGreaterThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
					startDate, PageRequest.of(0, 20)).getContent())
			.extracting(FeedbackSubmissionEntity::getId)
			.containsExactly(afterEnd, atEnd, insideRange, atStart);
		assertThat(repository
			.findByClientAppRegistrationIdAndSubmittedAtLessThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
					endDate, PageRequest.of(0, 20)).getContent())
			.extracting(FeedbackSubmissionEntity::getId)
			.containsExactly(atEnd, insideRange, atStart, beforeStart);
		assertThat(repository
			.findByClientAppRegistrationIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(clientAppRegistrationId,
					startDate, endDate, PageRequest.of(0, 20)).getContent())
			.extracting(FeedbackSubmissionEntity::getId)
			.containsExactly(atEnd, insideRange, atStart);

		Page<FeedbackSubmissionEntity> firstPage = repository
			.findByClientAppRegistrationIdOrderBySubmittedAtDesc(clientAppRegistrationId, PageRequest.of(0, 2));
		assertThat(firstPage.getContent()).extracting(FeedbackSubmissionEntity::getId).containsExactly(afterEnd, atEnd);
		assertThat(firstPage.getTotalElements()).isEqualTo(5);
		assertThat(firstPage.getTotalPages()).isEqualTo(3);
		Page<FeedbackSubmissionEntity> secondPage = repository
			.findByClientAppRegistrationIdOrderBySubmittedAtDesc(clientAppRegistrationId, PageRequest.of(1, 2));
		assertThat(secondPage.getContent()).extracting(FeedbackSubmissionEntity::getId)
			.containsExactly(insideRange, atStart);
	}

	private UUID saveSubmission(UUID clientAppRegistrationId, OffsetDateTime submittedAt) {
		UUID id = UUID.randomUUID();
		jdbcTemplate.update("""
			INSERT INTO feedback_submissions
				(id, client_id, client_app_registration_id, client_name, submitted_at, questions, answers)
			VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb))
			""", id, UUID.randomUUID(), clientAppRegistrationId, "Feedback client", submittedAt, "[]", "{}");
		return id;
	}

}