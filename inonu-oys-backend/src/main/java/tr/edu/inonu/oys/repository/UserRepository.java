package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.edu.inonu.oys.model.Role;
import tr.edu.inonu.oys.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.assignedDepartments WHERE u.username = :username")
    Optional<User> findByUsernameWithDepartments(@Param("username") String username);

    // YENİ METOT: Kullanıcıları rollere göre, departman atamalarıyla birlikte "istekli" olarak çeker.
    // "LEFT JOIN FETCH" komutu, LazyInitializationException'ı kökünden çözer.
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.assignedDepartments WHERE u.role = :role")
    List<User> findByRoleWithDepartments(@Param("role") Role role);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.assignedDepartments WHERE u.role IN :roles ORDER BY u.lastName, u.firstName")
    List<User> findByRolesWithDepartments(@Param("roles") List<Role> roles);

    @Query("""
            SELECT DISTINCT u FROM User u
            JOIN FETCH u.assignedDepartments d
            WHERE d.id = :departmentId
              AND u.role = tr.edu.inonu.oys.model.Role.JURY
              AND u.active = true
            ORDER BY u.lastName, u.firstName
            """)
    List<User> findActiveJuriesByDepartmentWithDepartments(@Param("departmentId") Long departmentId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.assignedDepartments WHERE u.id = :id")
    Optional<User> findByIdWithDepartments(@Param("id") Long id);

    @Query(value = """
            SELECT u.* FROM users u
            JOIN jury_assignments ja ON ja.user_id = u.id
            WHERE ja.department_id = :departmentId
              AND ja.assignment_role = :assignmentRole
              AND u.role = 'JURY'
              AND u.active = true
            ORDER BY u.last_name, u.first_name
            """, nativeQuery = true)
    List<User> findJuriesByDepartmentAndAssignmentRole(@Param("departmentId") Long departmentId,
                                                       @Param("assignmentRole") String assignmentRole);

    @Modifying
    @Query(value = """
            UPDATE jury_assignments
            SET assignment_role = :assignmentRole
            WHERE user_id = :juryId AND department_id = :departmentId
            """, nativeQuery = true)
    void updateJuryAssignmentRole(@Param("juryId") Long juryId,
                                  @Param("departmentId") Long departmentId,
                                  @Param("assignmentRole") String assignmentRole);

    @Query(value = """
            SELECT department_id, assignment_role
            FROM jury_assignments
            WHERE user_id = :juryId
            """, nativeQuery = true)
    List<Object[]> findJuryAssignmentRoles(@Param("juryId") Long juryId);
}
