package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.edu.inonu.oys.model.CandidateJuryAssignment;
import tr.edu.inonu.oys.model.CandidateJuryAssignmentStatus;

import java.util.List;
import java.util.Optional;

public interface CandidateJuryAssignmentRepository extends JpaRepository<CandidateJuryAssignment, Long> {
    List<CandidateJuryAssignment> findByApplicationIdOrderByMatchScoreDescIdAsc(Long applicationId);
    List<CandidateJuryAssignment> findByJuryIdAndStatusOrderByApplicationApplicantLastNameAscApplicationApplicantFirstNameAsc(
            Long juryId, CandidateJuryAssignmentStatus status);
    boolean existsByApplicationIdAndJuryIdAndStatus(Long applicationId, Long juryId, CandidateJuryAssignmentStatus status);
    long countByApplicationIdAndStatus(Long applicationId, CandidateJuryAssignmentStatus status);
    Optional<CandidateJuryAssignment> findByApplicationIdAndJuryId(Long applicationId, Long juryId);
    void deleteByApplicationIdAndStatus(Long applicationId, CandidateJuryAssignmentStatus status);
}
