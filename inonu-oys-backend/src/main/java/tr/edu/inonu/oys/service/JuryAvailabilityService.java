package tr.edu.inonu.oys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.JuryAvailabilityDTO;
import tr.edu.inonu.oys.dto.JuryAvailabilityRequest;
import tr.edu.inonu.oys.model.Department;
import tr.edu.inonu.oys.model.JuryAvailability;
import tr.edu.inonu.oys.model.Role;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.repository.DepartmentRepository;
import tr.edu.inonu.oys.repository.JuryAvailabilityRepository;
import tr.edu.inonu.oys.repository.UserRepository;

import java.util.List;

@Service
public class JuryAvailabilityService {
    private final JuryAvailabilityRepository availabilityRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public JuryAvailabilityService(JuryAvailabilityRepository availabilityRepository,
                                   DepartmentRepository departmentRepository,
                                   UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<JuryAvailabilityDTO> findByDepartment(Long departmentId) {
        return availabilityRepository.findByDepartmentIdOrderByAvailableDateAscStartTimeAsc(departmentId).stream()
                .map(JuryAvailabilityDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JuryAvailabilityDTO> findByJury(Long juryId) {
        return availabilityRepository.findByJuryIdOrderByAvailableDateAscStartTimeAsc(juryId).stream()
                .map(JuryAvailabilityDTO::new)
                .toList();
    }

    @Transactional
    public JuryAvailabilityDTO create(JuryAvailabilityRequest request) {
        validateTimeRange(request);
        JuryAvailability availability = new JuryAvailability();
        apply(availability, request);
        return new JuryAvailabilityDTO(availabilityRepository.save(availability));
    }

    @Transactional
    public JuryAvailabilityDTO update(Long id, JuryAvailabilityRequest request) {
        validateTimeRange(request);
        JuryAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Juri musaitligi bulunamadi."));
        apply(availability, request);
        return new JuryAvailabilityDTO(availabilityRepository.save(availability));
    }

    private void apply(JuryAvailability availability, JuryAvailabilityRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new RuntimeException("Bolum bulunamadi."));
        User jury = userRepository.findById(request.juryId())
                .orElseThrow(() -> new RuntimeException("Juri bulunamadi."));
        if (jury.getRole() != Role.JURY) {
            throw new RuntimeException("Musaitlik sadece juri kullanicilari icin tanimlanabilir.");
        }
        availability.setDepartment(department);
        availability.setJury(jury);
        availability.setAvailableDate(request.availableDate());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
    }

    private void validateTimeRange(JuryAvailabilityRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new RuntimeException("Bitis saati baslangic saatinden sonra olmalidir.");
        }
    }
}
