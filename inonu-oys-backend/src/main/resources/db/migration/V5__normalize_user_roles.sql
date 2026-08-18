UPDATE users
SET role = btrim(role)
WHERE role <> btrim(role);

ALTER TABLE users
    ADD CONSTRAINT chk_users_role_allowed
    CHECK (role IN ('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_ADMIN', 'JURY', 'APPLICANT'));
