--liquibase formatted sql

--changeset ods:012-seed-project-technologies-api-definition
-- Description: Registers the Project Technologies API. Access remains denied until an
--   ALLOWED_CLIENTS policy is configured for this API definition.
INSERT INTO api_definitions (api_id, name, base_path, version, auth_types, is_public, enabled)
VALUES ('project-technologies-v1', 'Project Technologies API v1', 'projects/*/technologies', 'v1', ARRAY['CLIENT_CREDENTIALS'], FALSE, TRUE)
ON CONFLICT (api_id) DO NOTHING;

--rollback DELETE FROM api_definitions WHERE api_id = 'project-technologies-v1';