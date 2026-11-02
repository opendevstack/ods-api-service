package org.opendevstack.apiservice.persistence.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for feedback submissions.
 */
@Repository
public interface FeedbackSubmissionRepository extends JpaRepository<FeedbackSubmissionEntity, UUID> {

	@Query("""
        SELECT submission
        FROM FeedbackSubmissionEntity submission
        WHERE submission.submittedAt >= COALESCE(:startDate, submission.submittedAt)
            AND submission.submittedAt <= COALESCE(:endDate, submission.submittedAt)
        """)
	Page<FeedbackSubmissionEntity> findBySubmittedAtRange(@Param("startDate") OffsetDateTime startDate,
			@Param("endDate") OffsetDateTime endDate, Pageable pageable);

}