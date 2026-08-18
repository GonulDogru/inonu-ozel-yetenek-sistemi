ALTER TABLE users
    DROP CONSTRAINT IF EXISTS chk_users_role_allowed;

ALTER TABLE users
    ADD CONSTRAINT chk_users_role_allowed
    CHECK (role IN ('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_ADMIN', 'JURY', 'APPLICANT'));
