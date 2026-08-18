ALTER TABLE applications
    DROP CONSTRAINT IF EXISTS applications_status_check;

UPDATE applications
SET status = 'PENDING_EVALUATION'
WHERE status = 'PENDING';

UPDATE applications
SET status = 'PENDING_EVALUATION'
WHERE status = 'APPROVED';

ALTER TABLE applications
    ADD CONSTRAINT applications_status_check
    CHECK (status IN ('SUBMITTED', 'PENDING_EVALUATION', 'REJECTED', 'COMPLETED', 'CANCELLED'));
