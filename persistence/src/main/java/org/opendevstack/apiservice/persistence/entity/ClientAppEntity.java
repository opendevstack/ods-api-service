package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * JPA entity mapping the {@code client_apps} table.
 *
 * <p>Represents a registered Azure AD client that is authorised to call the
 * service APIs. Each client may have zero or more
 * {@link ClientAppProjectFlavorEntity project flavors} that control how
 * projects are created.</p>
 *
 * <p>Schema is managed externally via Liquibase
 * ({@code 002_create_client_apps_table.sql}). Hibernate is configured with
 * {@code ddl-auto=validate}.</p>
 */
@Entity
@Table(name = "client_apps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "projectFlavors")
public class ClientAppEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	/** Azure AD Application (client) UUID. Unique index {@code uq_client_apps_client_id}. */
	@Column(name = "client_id", nullable = false, unique = true, length = 36)
	private String clientId;

	/** Azure AD application display name. */
	@Column(name = "client_name", length = 255)
	private String clientName;

	/**
	 * Granted permissions. Known values: {@code project:add}, {@code project:detail},
	 * {@code project:list}.
	 */
	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "permissions", nullable = false, columnDefinition = "TEXT[]")
	@Builder.Default
	private String[] permissions = {};

	/** OAuth2 scope or role granted to this client (e.g. {@code api.read}, {@code api.write}). */
	@Column(name = "role_scope")
	private String roleScope;

	/** When {@code false} the client is denied access without removing the row. */
	@Column(name = "enabled", nullable = false)
	@Builder.Default
	private boolean enabled = true;

	/** Project flavor configurations associated with this client. */
	@OneToMany(mappedBy = "clientApp", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ClientAppProjectFlavorEntity> projectFlavors = new ArrayList<>();

	/** Original creation timestamp (UTC). Set automatically on first persist. */
	@Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime createdAt;

	/** Timestamp of last update (UTC). Updated automatically on every merge. */
	@Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime updatedAt;

	@PrePersist
	void onPrePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onPreUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

}