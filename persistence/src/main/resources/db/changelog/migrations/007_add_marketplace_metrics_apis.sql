--liquibase formatted sql

-- ============================================================================
-- changeset ods:010-seed-marketplace-metrics-api-definitions
-- Description: Seed API definitions for Marketplace Metrics API.
-- ============================================================================
INSERT INTO api_definitions (api_id, name, base_path, version, auth_types, is_public, enabled)
VALUES
    ('marketplace-metrics-v1', 'Marketplace Metrics API v1', 'projects/metrics/marketplace/*', 'v1', ARRAY['CLIENT_CREDENTIALS'], FALSE, TRUE)
ON CONFLICT (api_id) DO NOTHING;

--rollback DELETE FROM api_definitions WHERE api_id IN ('marketplace-metrics-v1');
