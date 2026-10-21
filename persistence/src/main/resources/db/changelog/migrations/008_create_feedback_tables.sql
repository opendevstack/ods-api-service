--liquibase formatted sql

-- ============================================================================
-- changeset ods:011-create-feedback-tables
-- Description: Stores feedback questionnaire submissions, including client
--   metadata, the questions shown to the submitter, and their answers.
-- ============================================================================
CREATE TABLE IF NOT EXISTS feedback_submissions (
    id                         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    client_id                  UUID         NOT NULL,
    client_app_registration_id UUID         NOT NULL,
    client_name                VARCHAR(255) NOT NULL,
    submitter                  VARCHAR(255),
    submitted_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    questions                  JSONB        NOT NULL,
    answers                    JSONB        NOT NULL,
    CONSTRAINT fk_feedback_submissions_client_app
        FOREIGN KEY (client_id)
        REFERENCES client_apps (id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_feedback_submissions_questions_array
        CHECK (COALESCE(jsonb_typeof(questions) = 'array', FALSE)),
    CONSTRAINT chk_feedback_submissions_answers_object
        CHECK (COALESCE(jsonb_typeof(answers) = 'object', FALSE))
);

CREATE INDEX IF NOT EXISTS idx_feedback_submissions_client_submitted
    ON feedback_submissions (client_id, submitted_at DESC);

COMMENT ON TABLE feedback_submissions
    IS 'Questionnaire submissions with question and answer snapshots';
COMMENT ON COLUMN feedback_submissions.client_app_registration_id
    IS 'Azure Application registration ID (client_id), captured at submission time';
COMMENT ON COLUMN feedback_submissions.client_name
    IS 'Denormalized client application name captured at submission time';
COMMENT ON COLUMN feedback_submissions.questions
    IS 'Questionnaire definition shown to the submitter, stored as a JSON array';
COMMENT ON COLUMN feedback_submissions.answers
    IS 'Submitted answers keyed by question and follow-up IDs, stored as a JSON object';

--rollback DROP TABLE IF EXISTS feedback_submissions;
