ALTER TABLE applications ADD COLUMN IF NOT EXISTS department_id BIGINT;

INSERT INTO departments (name, quota, base_score_requirement)
SELECT 'Beden Eğitimi ve Spor Öğretmenliği', 0, 150
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Beden Eğitimi ve Spor Öğretmenliği');
INSERT INTO departments (name, quota, base_score_requirement)
SELECT 'Antrenörlük Eğitimi', 0, 150
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Antrenörlük Eğitimi');
INSERT INTO departments (name, quota, base_score_requirement)
SELECT 'Spor Yöneticiliği', 0, 150
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Spor Yöneticiliği');
INSERT INTO departments (name, quota, base_score_requirement)
SELECT 'Engellilerde Egzersiz ve Spor Eğitimi', 0, 150
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Engellilerde Egzersiz ve Spor Eğitimi');
INSERT INTO departments (name, quota, base_score_requirement)
SELECT 'Resim-İş Öğretmenliği', 0, 150
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Resim-İş Öğretmenliği');
INSERT INTO departments (name, quota, base_score_requirement)
SELECT 'Grafik Tasarımı', 0, 150
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Grafik Tasarımı');
INSERT INTO departments (name, quota, base_score_requirement)
SELECT 'Müzik Bilimleri (Müzikoloji)', 0, 150
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Müzik Bilimleri (Müzikoloji)');
INSERT INTO departments (name, quota, base_score_requirement)
SELECT 'Seramik Bölümü', 0, 150
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Seramik Bölümü');

UPDATE applications
SET department_id = (
    SELECT departments.id
    FROM departments
    WHERE departments.name = applications.program_name
)
WHERE department_id IS NULL
  AND program_name IS NOT NULL
  AND EXISTS (
      SELECT 1 FROM departments WHERE departments.name = applications.program_name
  );

ALTER TABLE applications
    ADD CONSTRAINT fk_application_department
    FOREIGN KEY (department_id) REFERENCES departments(id);

CREATE INDEX IF NOT EXISTS idx_applications_department ON applications(department_id);
