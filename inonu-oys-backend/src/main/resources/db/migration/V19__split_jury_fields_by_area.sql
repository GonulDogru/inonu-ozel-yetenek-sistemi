UPDATE users
SET jury_field = 'MUSIC'
WHERE role = 'JURY'
  AND (
      upper(first_name) LIKE '%MUZIK%'
      OR upper(first_name) LIKE '%MÜZIK%'
      OR upper(first_name) LIKE '%MÜZİK%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%ENSTR%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%RITIM%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%RİTİM%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%MELODI%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%MELODİ%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%İŞİTME%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%ISITME%'
  );

UPDATE users
SET jury_field = 'ART'
WHERE role = 'JURY'
  AND (
      upper(first_name) LIKE '%RESIM%'
      OR upper(first_name) LIKE '%RESİM%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%ÇIZIM%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%ÇİZİM%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%CIZIM%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%DESEN%'
  );

UPDATE users
SET jury_field = 'CERAMIC'
WHERE role = 'JURY'
  AND (
      upper(first_name) LIKE '%SERAMIK%'
      OR upper(first_name) LIKE '%SERAMİK%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%SERAMIK%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%SERAMİK%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%HACIM%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%HACİM%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%MODELLEME%'
  );

UPDATE users
SET jury_field = 'SPOR'
WHERE role = 'JURY'
  AND (
      upper(first_name) LIKE '%SPOR%'
      OR upper(first_name) LIKE '%ANTRENOR%'
      OR upper(first_name) LIKE '%ANTRENÖR%'
      OR upper(first_name) LIKE '%BEDEN%'
      OR upper(first_name) LIKE '%EGZERSIZ%'
      OR upper(first_name) LIKE '%EGZERSİZ%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%PARKUR%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%SPRINT%'
      OR upper(coalesce(jury_specialties, '')) LIKE '%SLALOM%'
  );

DELETE FROM jury_assignments ja
USING users u, departments d
WHERE ja.user_id = u.id
  AND ja.department_id = d.id
  AND u.role = 'JURY'
  AND (
      (u.jury_field = 'MUSIC' AND NOT (upper(d.name) LIKE '%MÜZİK%' OR upper(d.name) LIKE '%MUZIK%' OR upper(d.name) LIKE '%MÜZIKOLOJI%' OR upper(d.name) LIKE '%MÜZİKOLOJİ%'))
      OR (u.jury_field = 'ART' AND NOT (upper(d.name) LIKE '%RESİM%' OR upper(d.name) LIKE '%RESIM%'))
      OR (u.jury_field = 'CERAMIC' AND NOT (upper(d.name) LIKE '%SERAMİK%' OR upper(d.name) LIKE '%SERAMIK%'))
      OR (u.jury_field = 'SPOR' AND NOT (upper(d.name) LIKE '%SPOR%' OR upper(d.name) LIKE '%ANTRENÖR%' OR upper(d.name) LIKE '%ANTRENOR%' OR upper(d.name) LIKE '%BEDEN%' OR upper(d.name) LIKE '%EGZERSİZ%' OR upper(d.name) LIKE '%EGZERSIZ%'))
  );
