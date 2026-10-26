package org.opendevstack.apiservice.persistence.repository;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for feedback submissions.
 */
@Repository
public interface FeedbackSubmissionRepository extends JpaRepository<FeedbackSubmissionEntity, UUID> {

	List<FeedbackSubmissionEntity> findByClientAppRegistrationIdOrderBySubmittedAtDesc(UUID clientAppRegistrationId);

	List<FeedbackSubmissionEntity> findByClientAppRegistrationIdAndSubmittedAtGreaterThanEqualOrderBySubmittedAtDesc(
			UUID clientAppRegistrationId, OffsetDateTime startDate);

	List<FeedbackSubmissionEntity> findByClientAppRegistrationIdAndSubmittedAtLessThanEqualOrderBySubmittedAtDesc(
			UUID clientAppRegistrationId, OffsetDateTime endDate);

	List<FeedbackSubmissionEntity> findByClientAppRegistrationIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
			UUID clientAppRegistrationId, OffsetDateTime startDate, OffsetDateTime endDate);

}