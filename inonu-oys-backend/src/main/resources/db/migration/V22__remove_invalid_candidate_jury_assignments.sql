DELETE FROM candidate_jury_assignments cja
USING applications a, departments d, users u
WHERE cja.application_id = a.id
  AND a.department_id = d.id
  AND cja.jury_id = u.id
  AND (
      u.role <> 'JURY'
      OR u.active = false
      OR NOT EXISTS (
          SELECT 1
          FROM jury_assignments ja
          WHERE ja.user_id = u.id
            AND ja.department_id = d.id
      )
      OR (u.jury_field = 'MUSIC' AND NOT (upper(d.code) LIKE 'MUZIK%'))
      OR (u.jury_field = 'ART' AND NOT (upper(d.code) LIKE 'RESIM%'))
      OR (u.jury_field = 'CERAMIC' AND NOT (upper(d.code) LIKE 'SERAMIK%'))
      OR (u.jury_field = 'SPOR' AND NOT (
          upper(d.code) LIKE 'BEDEN%'
          OR upper(d.code) LIKE 'ANTRENOR%'
          OR upper(d.code) LIKE 'SPOR%'
          OR upper(d.code) LIKE 'ENGELLILER%'
      ))
  );
