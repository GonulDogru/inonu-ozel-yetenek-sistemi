package tr.edu.inonu.oys.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tr.edu.inonu.oys.dto.DepartmentDTO;
import tr.edu.inonu.oys.model.Department;
import tr.edu.inonu.oys.repository.DepartmentRepository;
import tr.edu.inonu.oys.dto.DepartmentSettingsRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı! ID: " + id));
    }

    // YENİ METOT: Tüm bölümleri DTO olarak getir
    public List<DepartmentDTO> findAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DepartmentDTO updateSettings(Long id, DepartmentSettingsRequest request) {
        Department department = getDepartmentById(id);
        department.setCode(request.code());
        if (request.examType() != null) {
            department.setExamType(request.examType());
        }
        department.setQuota(request.quota());
        department.setBaseScoreRequirement(request.baseScoreRequirement());
        if (request.talentAdmissionEnabled() != null) {
            department.setTalentAdmissionEnabled(request.talentAdmissionEnabled());
        }
        department.setTrimScores(request.trimScores());
        department.setDefaultCandidateIntervalMinutes(request.defaultCandidateIntervalMinutes());
        department.setDefaultSessionDurationMinutes(request.defaultSessionDurationMinutes());
        department.setDefaultBreakMinutes(request.defaultBreakMinutes());
        if (request.requiredPrimaryJuryCount() != null) {
            department.setRequiredPrimaryJuryCount(request.requiredPrimaryJuryCount());
        }
        if (request.requiredBackupJuryCount() != null) {
            department.setRequiredBackupJuryCount(request.requiredBackupJuryCount());
        }
        if (request.jurySelfInactiveDeadlineHours() != null) {
            department.setJurySelfInactiveDeadlineHours(request.jurySelfInactiveDeadlineHours());
        }
        Department saved = departmentRepository.save(department);
        return toDto(saved);
    }

    private DepartmentDTO toDto(Department department) {
        return new DepartmentDTO(department.getId(), department.getName(), department.getCode(),
                department.getExamType(), department.getQuota(), department.getBaseScoreRequirement(),
                department.isTrimScores(), department.isTalentAdmissionEnabled(), department.getDefaultCandidateIntervalMinutes(),
                department.getDefaultSessionDurationMinutes(), department.getDefaultBreakMinutes(),
                department.getRequiredPrimaryJuryCount(), department.getRequiredBackupJuryCount(),
                department.getJurySelfInactiveDeadlineHours());
    }
}
