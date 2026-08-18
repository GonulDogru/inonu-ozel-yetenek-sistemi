package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.edu.inonu.oys.model.JuryInactiveSlot;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JuryInactiveSlotRepository extends JpaRepository<JuryInactiveSlot, Long> {
    List<JuryInactiveSlot> findByDepartmentIdAndInactiveDate(Long departmentId, LocalDate inactiveDate);
    List<JuryInactiveSlot> findByJuryIdOrderByInactiveDateAscStartTimeAsc(Long juryId);
}
