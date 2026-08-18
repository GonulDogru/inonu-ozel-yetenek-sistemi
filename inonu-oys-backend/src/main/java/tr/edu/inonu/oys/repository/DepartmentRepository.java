package tr.edu.inonu.oys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.edu.inonu.oys.model.Department;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // Bölüm ismine göre arama yapabilmek için (Örn: "Müzik Öğretmenliği")
    Optional<Department> findByName(String name);
    Optional<Department> findByCode(String code);
}
