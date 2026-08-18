CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT PRIMARY KEY,
    applications_open BOOLEAN NOT NULL DEFAULT TRUE,
    application_start_date DATE,
    application_end_date DATE,
    min_tyt_score DOUBLE PRECISION DEFAULT 150,
    require_obp BOOLEAN NOT NULL DEFAULT FALSE,
    require_osym_document BOOLEAN NOT NULL DEFAULT TRUE,
    require_diploma_document BOOLEAN NOT NULL DEFAULT TRUE,
    require_health_document BOOLEAN NOT NULL DEFAULT TRUE,
    require_photo_document BOOLEAN NOT NULL DEFAULT TRUE,
    require_national_document BOOLEAN NOT NULL DEFAULT TRUE,
    require_disabled_document BOOLEAN NOT NULL DEFAULT TRUE,
    exam_document_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    result_document_enabled BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO system_settings (
    id, applications_open, min_tyt_score, require_obp,
    require_osym_document, require_diploma_document, require_health_document, require_photo_document,
    require_national_document, require_disabled_document, exam_document_enabled, result_document_enabled
)
SELECT 1, TRUE, 150, FALSE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM system_settings WHERE id = 1);
