UPDATE departments
SET name = 'Müzik Öğretmenliği',
    code = 'MUZIK_OGRETMENLIGI',
    exam_type = 'INDIVIDUAL',
    default_candidate_interval_minutes = 20,
    default_session_duration_minutes = NULL,
    default_break_minutes = 5
WHERE name IN ('MÃ¼zik ÃÄretmenliÄi', 'Muzik Ogretmenligi', 'Müzik Öğretmenliği');

UPDATE departments
SET name = 'Beden Eğitimi ve Spor Öğretmenliği',
    code = 'BEDEN_EGITIMI_VE_SPOR_OGRETMENLIGI',
    exam_type = 'TRACK',
    default_candidate_interval_minutes = 5,
    default_session_duration_minutes = NULL,
    default_break_minutes = 5
WHERE name IN ('Beden EÄitimi ve Spor ÃÄretmenliÄi', 'Beden Egitimi ve Spor Ogretmenligi', 'Beden Eğitimi ve Spor Öğretmenliği');

UPDATE departments
SET name = 'Antrenörlük Eğitimi',
    code = 'ANTRENORLUK_EGITIMI',
    exam_type = 'TRACK',
    default_candidate_interval_minutes = 5,
    default_session_duration_minutes = NULL,
    default_break_minutes = 5
WHERE name IN ('AntrenÃ¶rlÃ¼k EÄitimi', 'Antrenorluk Egitimi', 'Antrenörlük Eğitimi');

UPDATE departments
SET name = 'Spor Yöneticiliği',
    code = 'SPOR_YONETICILIGI',
    exam_type = 'TRACK',
    default_candidate_interval_minutes = 5,
    default_session_duration_minutes = NULL,
    default_break_minutes = 5
WHERE name IN ('Spor YÃ¶neticiliÄi', 'Spor Yoneticiligi', 'Spor Yöneticiliği');

UPDATE departments
SET name = 'Engellilerde Egzersiz ve Spor Eğitimi',
    code = 'ENGELLILERDE_EGZERSIZ_VE_SPOR_EGITIMI',
    exam_type = 'TRACK',
    default_candidate_interval_minutes = 5,
    default_session_duration_minutes = NULL,
    default_break_minutes = 5
WHERE name IN ('Engellilerde Egzersiz ve Spor EÄitimi', 'Engellilerde Egzersiz ve Spor Egitimi', 'Engellilerde Egzersiz ve Spor Eğitimi');

UPDATE departments
SET name = 'Resim-İş Öğretmenliği',
    code = 'RESIM_IS_OGRETMENLIGI',
    exam_type = 'GROUP',
    default_candidate_interval_minutes = NULL,
    default_session_duration_minutes = 120,
    default_break_minutes = 5
WHERE name IN ('Resim-Ä°Å ÃÄretmenliÄi', 'Resim-Is Ogretmenligi', 'Resim-İş Öğretmenliği');

UPDATE departments
SET name = 'Grafik Tasarımı',
    code = 'GRAFIK_TASARIMI',
    exam_type = 'GROUP',
    default_candidate_interval_minutes = NULL,
    default_session_duration_minutes = 120,
    default_break_minutes = 5
WHERE name IN ('Grafik TasarÄ±mÄ±', 'Grafik Tasarimi', 'Grafik Tasarımı');

UPDATE departments
SET name = 'Müzik Bilimleri (Müzikoloji)',
    code = 'MUZIK_BILIMLERI_MUZIKOLOJI',
    exam_type = 'INDIVIDUAL',
    default_candidate_interval_minutes = 20,
    default_session_duration_minutes = NULL,
    default_break_minutes = 5
WHERE name IN ('MÃ¼zik Bilimleri (MÃ¼zikoloji)', 'Muzik Bilimleri', 'Müzik Bilimleri (Müzikoloji)');

UPDATE departments
SET name = 'Seramik Bölümü',
    code = 'SERAMIK_BOLUMU',
    exam_type = 'GROUP',
    default_candidate_interval_minutes = NULL,
    default_session_duration_minutes = 120,
    default_break_minutes = 5
WHERE name IN ('Seramik BÃ¶lÃ¼mÃ¼', 'Seramik Bolumu', 'Seramik Bölümü');
