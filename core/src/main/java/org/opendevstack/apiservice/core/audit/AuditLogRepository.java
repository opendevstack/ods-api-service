package org.opendevstack.apiservice.core.audit;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link AuditLogEntry} persistence.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, UUID> {

}
