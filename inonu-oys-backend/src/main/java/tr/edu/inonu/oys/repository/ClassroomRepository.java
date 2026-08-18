package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.edu.inonu.oys.model.Classroom;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByDepartmentIdOrderByNameAsc(Long departmentId);
    List<Classroom> findByDepartmentIdAndActiveTrueOrderByNameAsc(Long departmentId);
    Optional<Classroom> findByDepartmentIdAndNameIgnoreCase(Long departmentId, String name);
}
