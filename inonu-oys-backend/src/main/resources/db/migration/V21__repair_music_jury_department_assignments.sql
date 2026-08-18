INSERT INTO jury_assignments (user_id, department_id, assignment_role)
SELECT u.id,
       d.id,
       CASE
           WHEN u.username IN ('03000000182', '03000000192') THEN 'BACKUP'
           ELSE 'PRIMARY'
       END
FROM users u
JOIN departments d ON (
    (u.username IN ('03000000152', '03000000162', '03000000172', '03000000182', '03000000192')
        AND (upper(d.name) LIKE '%MÜZİK%ÖĞRETMEN%' OR upper(d.name) LIKE '%MUZIK%OGRETMEN%'))
    OR
    (u.username IN ('03000000052', '03000000062', '03000000072', '03000000082', '03000000092')
        AND (upper(d.name) LIKE '%MÜZİK%BİLİMLERİ%' OR upper(d.name) LIKE '%MUZIK%BILIMLERI%' OR upper(d.name) LIKE '%MÜZİKOLOJİ%' OR upper(d.name) LIKE '%MUZIKOLOJI%'))
)
WHERE u.role = 'JURY'
  AND NOT EXISTS (
      SELECT 1
      FROM jury_assignments existing
      WHERE existing.user_id = u.id
        AND existing.department_id = d.id
  );
