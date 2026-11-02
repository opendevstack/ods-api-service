package org.opendevstack.apiservice.persistence.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for feedback submissions.
 */
@Repository
public interface FeedbackSubmissionRepository extends JpaRepository<FeedbackSubmissionEntity, UUID> {

	Page<FeedbackSubmissionEntity> findBySubmittedAtGreaterThanEqualOrderBySubmittedAtDesc(
			OffsetDateTime startDate, Pageable pageable);

	Page<FeedbackSubmissionEntity> findBySubmittedAtLessThanEqualOrderBySubmittedAtDesc(
			OffsetDateTime endDate, Pageable pageable);

	Page<FeedbackSubmissionEntity> findBySubmittedAtBetweenOrderBySubmittedAtDesc(
			OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);

}