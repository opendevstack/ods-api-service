--liquibase formatted sql

-- ============================================================================
-- changeset ods:011-create-feedback-tables
-- Description: Creates feedback source systems and stores questionnaire submissions,
--   including the submitted answers and the questions shown to the submitter.
-- ============================================================================
CREATE TABLE IF NOT EXISTS feedback_source_systems (
    id          UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    project_id  UUID            NOT NULL,
    project_key VARCHAR(100)    NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_feedback_source_systems_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE RESTRICT,
    CONSTRAINT uq_feedback_source_systems_project_name
        UNIQUE (project_id, name),
    CONSTRAINT uq_feedback_source_systems_id_project
        UNIQUE (id, project_id)
);

CREATE INDEX IF NOT EXISTS idx_feedback_source_systems_project_enabled
    ON feedback_source_systems (project_id, enabled);

CREATE TABLE IF NOT EXISTS feedback_submissions (
    id               UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    source_system_id UUID            NOT NULL,
    source_system    VARCHAR(255)    NOT NULL,
    project_id       UUID            NOT NULL,
    project_key      VARCHAR(100)    NOT NULL,
    submitter        VARCHAR(255),
    submitted_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    feedback         JSONB           NOT NULL,
    CONSTRAINT fk_feedback_submissions_source_system
        FOREIGN KEY (source_system_id, project_id)
        REFERENCES feedback_source_systems (id, project_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_feedback_submissions_feedback_object
        CHECK (jsonb_typeof(feedback) = 'object'),
    CONSTRAINT chk_feedback_submissions_questions_array
        CHECK (COALESCE(jsonb_typeof(feedback -> 'questions') = 'array', FALSE)),
    CONSTRAINT chk_feedback_submissions_answers_object
        CHECK (COALESCE(jsonb_typeof(feedback -> 'answers') = 'object', FALSE))
);

CREATE INDEX IF NOT EXISTS idx_feedback_submissions_source_submitted
    ON feedback_submissions (source_system_id, submitted_at DESC);

CREATE INDEX IF NOT EXISTS idx_feedback_submissions_project_submitted
    ON feedback_submissions (project_id, submitted_at DESC);

COMMENT ON TABLE feedback_source_systems
    IS 'Feedback-producing systems configured for a project';
COMMENT ON COLUMN feedback_source_systems.project_key
    IS 'Denormalized project key captured with the feedback source system';
COMMENT ON COLUMN feedback_source_systems.enabled
    IS 'When FALSE, the source system is disabled for new feedback';
COMMENT ON TABLE feedback_submissions
    IS 'Questionnaire submissions with question and answer snapshots';
COMMENT ON COLUMN feedback_submissions.source_system
    IS 'Denormalized source system name captured at submission time';
COMMENT ON COLUMN feedback_submissions.project_key
    IS 'Denormalized project key captured at submission time';
COMMENT ON COLUMN feedback_submissions.feedback
    IS 'JSON object containing questions and answers';

--rollback DROP TABLE IF EXISTS feedback_submissions;
--rollback DROP TABLE IF EXISTS feedback_source_systems;
