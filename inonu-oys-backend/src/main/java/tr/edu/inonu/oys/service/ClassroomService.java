package tr.edu.inonu.oys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.ClassroomDTO;
import tr.edu.inonu.oys.dto.ClassroomRequest;
import tr.edu.inonu.oys.model.Classroom;
import tr.edu.inonu.oys.model.Department;
import tr.edu.inonu.oys.repository.ClassroomRepository;
import tr.edu.inonu.oys.repository.DepartmentRepository;

import java.util.List;

@Service
public class ClassroomService {
    private final ClassroomRepository classroomRepository;
    private final DepartmentRepository departmentRepository;

    public ClassroomService(ClassroomRepository classroomRepository, DepartmentRepository departmentRepository) {
        this.classroomRepository = classroomRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<ClassroomDTO> findByDepartment(Long departmentId) {
        return classroomRepository.findByDepartmentIdOrderByNameAsc(departmentId).stream()
                .map(ClassroomDTO::new)
                .toList();
    }

    @Transactional
    public ClassroomDTO create(ClassroomRequest request) {
        Classroom classroom = new Classroom();
        apply(classroom, request);
        return new ClassroomDTO(classroomRepository.save(classroom));
    }

    @Transactional
    public ClassroomDTO update(Long id, ClassroomRequest request) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salon bulunamadi."));
        apply(classroom, request);
        return new ClassroomDTO(classroomRepository.save(classroom));
    }

    private void apply(Classroom classroom, ClassroomRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new RuntimeException("Bolum bulunamadi."));
        classroom.setDepartment(department);
        classroom.setName(request.name());
        classroom.setCapacity(request.capacity());
        classroom.setBuilding(request.building());
        classroom.setActive(request.active());
    }
}
