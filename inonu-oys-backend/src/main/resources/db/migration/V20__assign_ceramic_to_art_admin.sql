INSERT INTO jury_assignments (user_id, department_id, assignment_role)
SELECT admin_user.id, department.id, 'PRIMARY'
FROM users admin_user
CROSS JOIN departments department
WHERE admin_user.username = '20000000006'
  AND admin_user.role = 'DEPARTMENT_ADMIN'
  AND department.talent_admission_enabled = true
  AND (upper(department.name) LIKE '%SERAMİK%' OR upper(department.name) LIKE '%SERAMIK%')
  AND NOT EXISTS (
      SELECT 1
      FROM jury_assignments existing
      WHERE existing.user_id = admin_user.id
        AND existing.department_id = department.id
  );
