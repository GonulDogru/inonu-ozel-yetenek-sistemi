ALTER TABLE departments ADD COLUMN IF NOT EXISTS trim_scores BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE applications ADD COLUMN IF NOT EXISTS obp DOUBLE PRECISION;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS standardized_oysp_score DOUBLE PRECISION;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS placement_rank INTEGER;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS placement_status VARCHAR(32);
ALTER TABLE applications ADD COLUMN IF NOT EXISTS result_published_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_applications_placement
    ON applications(department_id, placement_status, placement_rank);
