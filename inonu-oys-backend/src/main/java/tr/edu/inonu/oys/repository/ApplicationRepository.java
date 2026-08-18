package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.edu.inonu.oys.dto.ApplicationDTO;
import tr.edu.inonu.oys.model.Application;
import tr.edu.inonu.oys.model.ApplicationStatus;
import tr.edu.inonu.oys.model.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByApplicantUsername(String username);

    @Query("SELECT a FROM Application a JOIN a.applicant u WHERE u.role = :role")
    List<Application> findByApplicantRole(@Param("role") Role role);

    @Query("SELECT a FROM Application a JOIN a.department d WHERE d.name IN :programNames AND a.status = :status")
    List<Application> findJuryApplications(@Param("programNames") List<String> programNames, @Param("status") ApplicationStatus status);

    @Query("""
            SELECT DISTINCT a FROM Application a
            JOIN a.department d
            LEFT JOIN a.juryScores js
            WHERE d.name IN :programNames
              AND (a.status = :pendingStatus OR js.jury.id = :juryId)
            """)
    List<Application> findJuryWorkspaceApplications(@Param("programNames") List<String> programNames,
                                                    @Param("pendingStatus") ApplicationStatus pendingStatus,
                                                    @Param("juryId") Long juryId);

    @Query("SELECT new tr.edu.inonu.oys.dto.ApplicationDTO(a) FROM Application a JOIN a.applicant u WHERE u.username = :username")
    Optional<ApplicationDTO> findApplicationByUsernameAsDTO(@Param("username") String username);

    boolean existsByApplicantUsername(String username);

    List<Application> findByDepartmentIdAndStatus(Long departmentId, ApplicationStatus status);

    List<Application> findByDepartmentIdAndStatusOrderByApplicantLastNameAscApplicantFirstNameAsc(
            Long departmentId, ApplicationStatus status);

    List<Application> findByDepartmentIdAndStatusNotOrderByApplicantLastNameAscApplicantFirstNameAsc(
            Long departmentId, ApplicationStatus status);
}
