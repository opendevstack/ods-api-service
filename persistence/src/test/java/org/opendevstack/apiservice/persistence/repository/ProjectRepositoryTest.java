package org.opendevstack.apiservice.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for {@link ProjectRepository}.
 *
 * <p>
 * Uses a real PostgreSQL instance via Testcontainers with Hibernate
 * {@code create-drop} so the tests are fully schema-autonomous — no dependency
 * on the Liquibase migration files at test time. This keeps the persistence
 * module independently testable.
 * </p>
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProjectRepositoryTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
		.withDatabaseName("devstack_test")
		.withUsername("test")
		.withPassword("test");

	@DynamicPropertySource
	static void postgresProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		// Override ddl-auto for tests: let Hibernate create the schema from entities
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}

	@Autowired
	private ProjectRepository repository;

	private ProjectEntity activeProject;

	private ProjectEntity deletedProject;

	@BeforeEach
	void setUp() {
		repository.deleteAll();

		activeProject = repository.save(ProjectEntity.builder()
			.projectKey("ACTIVE-01")
			.projectName("Active Project One")
			.configurationItem("CI-001")
			.location("eu")
			.projectFlavor("AMP")
			.status(null) // null = completed in this domain
			.deleted(false)
			.build());

		deletedProject = repository.save(ProjectEntity.builder()
			.projectKey("DELETED-01")
			.projectName("Deleted Project One")
			.configurationItem("CI-002")
			.location("us")
			.projectFlavor("DLSS")
			.status("Failed")
			.deleted(true)
			.build());
	}

	// ── CRUD ──────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Basic CRUD")
	class BasicCrud {

		@Test
		@DisplayName("save() persists entity and auto-sets timestamps")
		void save_persistsEntityWithTimestamps() {
			assertThat(activeProject.getId()).isNotNull();
			assertThat(activeProject.getCreatedAt()).isNotNull();
			assertThat(activeProject.getUpdatedAt()).isNotNull();
		}

		@Test
		@DisplayName("findById() returns the saved entity")
		void findById_returnsEntity() {
			Optional<ProjectEntity> found = repository.findById(activeProject.getId());
			assertThat(found).isPresent();
			assertThat(found.get().getProjectKey()).isEqualTo("ACTIVE-01");
		}

		@Test
		@DisplayName("save() with duplicate project_key throws DataIntegrityViolationException")
		void save_duplicateProjectKey_throwsException() {
			ProjectEntity duplicate = ProjectEntity.builder()
				.projectKey("ACTIVE-01") // same key
				.configurationItem("CI-999")
				.location("eu")
				.build();

			assertThatThrownBy(() -> {
				repository.saveAndFlush(duplicate);
			}).isInstanceOf(DataIntegrityViolationException.class);
		}

	}

	// ── findByProjectKey ──────────────────────────────────────────────────────

	@Nested
	@DisplayName("findByProjectKey")
	class FindByProjectKey {

		@Test
		@DisplayName("returns project regardless of deleted flag")
		void returnsProjectRegardlessOfDeletedFlag() {
			assertThat(repository.findByProjectKey("ACTIVE-01")).isPresent();
			assertThat(repository.findByProjectKey("DELETED-01")).isPresent();
		}

		@Test
		@DisplayName("returns empty for unknown key")
		void returnsEmptyForUnknownKey() {
			assertThat(repository.findByProjectKey("UNKNOWN")).isEmpty();
		}

	}


	// ── findByDeletedFalse ────────────────────────────────────────────────────

	@Nested
	@DisplayName("findByDeletedFalse")
	class FindByDeletedFalse {

		@Test
		@DisplayName("returns only active projects")
		void returnsOnlyActiveProjects() {
			List<ProjectEntity> active = repository.findByDeletedFalse();
			assertThat(active).hasSize(1);
			assertThat(active.get(0).getProjectKey()).isEqualTo("ACTIVE-01");
		}

	}

}
