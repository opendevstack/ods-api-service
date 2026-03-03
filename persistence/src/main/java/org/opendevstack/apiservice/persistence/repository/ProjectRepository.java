package org.opendevstack.apiservice.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ProjectEntity}.
 *
 * <p>
 * Provides standard CRUD operations inherited from {@link JpaRepository} plus
 * domain-specific derived query methods. All query methods that return project
 * collections exclude soft-deleted records (i.e. {@code deleted = false}) by
 * convention — use {@code findAllIncludingDeleted()} variants explicitly when
 * deleted records are needed.
 * </p>
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

	/**
	 * Finds a project by its unique business key regardless of soft-delete status. Use
	 * this when you explicitly need to handle deleted projects (e.g. restore flows).
	 * @param projectKey the Atlassian-style project key
	 * @return the matching project, or {@link Optional#empty()} if it has never existed
	 */
	Optional<ProjectEntity> findByProjectKey(String projectKey);

	/**
	 * Returns all active (non-deleted) projects.
	 * @return list of active projects; empty list if none exist
	 */
	List<ProjectEntity> findByDeletedFalse();

	/**
	 * Checks whether an active project with the given key already exists.
	 * @param projectKey the project key to look up
	 * @return {@code true} if an active project with this key exists
	 */
	boolean existsByProjectKey(String projectKey);

}
