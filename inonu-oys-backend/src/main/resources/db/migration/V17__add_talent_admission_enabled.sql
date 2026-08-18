ALTER TABLE departments ADD COLUMN IF NOT EXISTS talent_admission_enabled BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE departments
SET talent_admission_enabled = FALSE,
    quota = 0
WHERE code = 'GRAFIK_TASARIMI'
   OR name IN ('Grafik Tasarımı', 'Grafik Tasarimi', 'Grafik TasarÄ±mÄ±');
