package org.opendevstack.apiservice.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientAppRepository extends JpaRepository<ClientAppEntity, UUID> {

	@EntityGraph(attributePaths = "projectFlavors")
	Optional<ClientAppEntity> findByClientId(String clientId);

	boolean existsByClientId(String clientId);

}

