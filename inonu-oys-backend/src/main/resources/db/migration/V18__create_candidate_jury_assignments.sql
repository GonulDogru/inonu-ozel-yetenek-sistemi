CREATE TABLE IF NOT EXISTS candidate_jury_assignments (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    jury_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    match_score INTEGER NOT NULL DEFAULT 0,
    matched_areas TEXT,
    approved_at TIMESTAMP,
    approved_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_candidate_jury_application FOREIGN KEY (application_id) REFERENCES applications(id),
    CONSTRAINT fk_candidate_jury_jury FOREIGN KEY (jury_id) REFERENCES users(id),
    CONSTRAINT fk_candidate_jury_approved_by FOREIGN KEY (approved_by_user_id) REFERENCES users(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_candidate_jury_assignment
    ON candidate_jury_assignments(application_id, jury_id);

CREATE INDEX IF NOT EXISTS idx_candidate_jury_status
    ON candidate_jury_assignments(application_id, status);
