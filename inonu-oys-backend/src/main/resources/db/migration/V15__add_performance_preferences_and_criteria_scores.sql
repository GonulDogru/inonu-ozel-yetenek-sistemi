ALTER TABLE applications ADD COLUMN IF NOT EXISTS performance_preferences TEXT;

ALTER TABLE jury_scores ADD COLUMN IF NOT EXISTS criteria_scores TEXT;
