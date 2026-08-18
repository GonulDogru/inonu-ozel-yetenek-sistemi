package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.edu.inonu.oys.model.JuryAvailability;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JuryAvailabilityRepository extends JpaRepository<JuryAvailability, Long> {
    List<JuryAvailability> findByDepartmentIdOrderByAvailableDateAscStartTimeAsc(Long departmentId);
    List<JuryAvailability> findByJuryIdOrderByAvailableDateAscStartTimeAsc(Long juryId);
    List<JuryAvailability> findByDepartmentIdAndAvailableDateOrderByStartTimeAsc(Long departmentId, LocalDate availableDate);
}
