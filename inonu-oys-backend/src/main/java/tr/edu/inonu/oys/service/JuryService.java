package tr.edu.inonu.oys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.ApplicationDTO;
import tr.edu.inonu.oys.dto.JuryInactiveSlotDTO;
import tr.edu.inonu.oys.dto.JuryInactiveSlotRequest;
import tr.edu.inonu.oys.dto.JuryScoreRequest;
import tr.edu.inonu.oys.model.Application;
import tr.edu.inonu.oys.model.ApplicationStatus;
import tr.edu.inonu.oys.model.Department;
import tr.edu.inonu.oys.model.JuryAssignmentRole;
import tr.edu.inonu.oys.model.JuryInactiveSlot;
import tr.edu.inonu.oys.model.JuryScore;
import tr.edu.inonu.oys.model.Role;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.repository.ApplicationRepository;
import tr.edu.inonu.oys.repository.DepartmentRepository;
import tr.edu.inonu.oys.repository.JuryInactiveSlotRepository;
import tr.edu.inonu.oys.repository.JuryScoreRepository;
import tr.edu.inonu.oys.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class JuryService {
    private final JuryScoreRepository juryScoreRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final JuryInactiveSlotRepository inactiveSlotRepository;
    private final CandidateJuryAssignmentService candidateJuryAssignmentService;

    public JuryService(JuryScoreRepository juryScoreRepository, ApplicationRepository applicationRepository,
                       UserRepository userRepository, DepartmentRepository departmentRepository,
                       JuryInactiveSlotRepository inactiveSlotRepository,
                       CandidateJuryAssignmentService candidateJuryAssignmentService) {
        this.juryScoreRepository = juryScoreRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.inactiveSlotRepository = inactiveSlotRepository;
        this.candidateJuryAssignmentService = candidateJuryAssignmentService;
    }

    @Transactional(readOnly = true)
    public List<ApplicationDTO> getAssignedApplications(User jury) {
        requireJury(jury);
        jury = userRepository.findById(jury.getId())
                .orElseThrow(() -> new RuntimeException("Juri bulunamadi."));
        Long juryId = jury.getId();
        return candidateJuryAssignmentService.approvedApplicationsForJury(juryId)
                .stream().map(application -> {
                    ApplicationDTO dto = new ApplicationDTO(application);
                    dto.setJuryAssignments(candidateJuryAssignmentService.approvedForApplication(application.getId()));
                    application.getJuryScores().stream()
                            .filter(score -> score.getJury() != null && score.getJury().getId().equals(juryId))
                            .findFirst()
                            .ifPresent(score -> {
                                dto.setCurrentJuryScore(score.getScore());
                                dto.setCurrentJuryScoredAt(score.getTimestamp());
                            });
                    return dto;
                })
                .toList();
    }

    @Transactional
    public JuryScore saveScore(JuryScoreRequest request, User jury) {
        requireJury(jury);
        jury = userRepository.findById(jury.getId())
                .orElseThrow(() -> new RuntimeException("Juri bulunamadi."));
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Basvuru bulunamadi."));

        if (application.getStatus() != ApplicationStatus.PENDING_EVALUATION
                && application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new RuntimeException("Bu basvuru degerlendirmeye acik degil.");
        }

        boolean assigned = jury.getAssignedDepartments().stream()
                .anyMatch(department -> application.getDepartment() != null
                        && department.getId().equals(application.getDepartment().getId()));
        if (!assigned) {
            throw new RuntimeException("Basvuru juriye atanmis bir bolume ait degil.");
        }
        if (!candidateJuryAssignmentService.isApprovedJury(application.getId(), jury.getId())) {
            throw new RuntimeException("Bu aday icin onayli juri atamaniz bulunmuyor.");
        }
        if (juryScoreRepository.existsByJuryIdAndApplicationId(jury.getId(), application.getId())) {
            throw new RuntimeException("Bu basvuruya daha once puan verdiniz.");
        }

        JuryScore score = new JuryScore();
        score.setApplication(application);
        score.setJury(jury);
        score.setScore(request.getScore());
        score.setComment(request.getComment());
        score.setCriteriaScores(request.getCriteriaScores());
        score.setTimestamp(LocalDateTime.now());
        JuryScore saved = juryScoreRepository.save(score);
        calculateAndSetApplicationScore(application);
        return saved;
    }

    @Transactional
    public void assignDepartment(Long juryId, Long departmentId, JuryAssignmentRole assignmentRole) {
        User jury = getJury(juryId);
        if (!jury.isActive()) {
            throw new RuntimeException("Pasif juriye bolum atamasi yapilamaz.");
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Bolum bulunamadi."));
        jury.getAssignedDepartments().add(department);
        userRepository.save(jury);
        userRepository.updateJuryAssignmentRole(juryId, departmentId,
                (assignmentRole != null ? assignmentRole : JuryAssignmentRole.PRIMARY).name());
    }

    @Transactional
    public void removeDepartment(Long juryId, Long departmentId) {
        User jury = getJury(juryId);
        jury.getAssignedDepartments().removeIf(department -> department.getId().equals(departmentId));
        userRepository.save(jury);
    }

    @Transactional(readOnly = true)
    public List<JuryInactiveSlotDTO> getInactiveSlots(User jury) {
        requireJury(jury);
        return inactiveSlotRepository.findByJuryIdOrderByInactiveDateAscStartTimeAsc(jury.getId()).stream()
                .map(JuryInactiveSlotDTO::new)
                .toList();
    }

    @Transactional
    public JuryInactiveSlotDTO createInactiveSlot(JuryInactiveSlotRequest request, User currentJury) {
        requireJury(currentJury);
        User jury = getJury(currentJury.getId());
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new RuntimeException("Bolum bulunamadi."));
        boolean assigned = jury.getAssignedDepartments().stream()
                .anyMatch(item -> item.getId().equals(department.getId()));
        if (!assigned) throw new RuntimeException("Bu bolum icin juri atamaniz bulunmuyor.");
        if ((request.startTime() == null) != (request.endTime() == null)) {
            throw new RuntimeException("Saat araligi icin baslangic ve bitis birlikte girilmelidir.");
        }
        if (request.startTime() != null && !request.endTime().isAfter(request.startTime())) {
            throw new RuntimeException("Bitis saati baslangic saatinden sonra olmalidir.");
        }
        int deadlineHours = department.getJurySelfInactiveDeadlineHours() != null
                ? department.getJurySelfInactiveDeadlineHours() : 24;
        LocalDateTime inactiveStart = request.inactiveDate()
                .atTime(request.startTime() != null ? request.startTime() : LocalTime.MIN);
        if (LocalDateTime.now().isAfter(inactiveStart.minusHours(deadlineHours))) {
            throw new RuntimeException("Juri pasiflik bildirimi icin son sure gecmistir.");
        }

        JuryInactiveSlot slot = new JuryInactiveSlot();
        slot.setJury(jury);
        slot.setDepartment(department);
        slot.setInactiveDate(request.inactiveDate());
        slot.setStartTime(request.startTime());
        slot.setEndTime(request.endTime());
        slot.setReason(request.reason());
        return new JuryInactiveSlotDTO(inactiveSlotRepository.save(slot));
    }

    private void calculateAndSetApplicationScore(Application application) {
        List<JuryScore> scores = juryScoreRepository.findByApplicationId(application.getId());
        if (scores.size() >= 3) {
            double average = scores.stream().mapToDouble(JuryScore::getScore).average().orElse(0);
            double rounded = Math.round(average * 100.0) / 100.0;
            application.setOyspScore(rounded);
            application.setAverageScore(rounded);
            application.setFinalPlacementScore(rounded);
            application.setStatus(ApplicationStatus.COMPLETED);
            applicationRepository.save(application);
        }
    }

    private User getJury(Long juryId) {
        User jury = userRepository.findById(juryId)
                .orElseThrow(() -> new RuntimeException("Juri bulunamadi."));
        requireJury(jury);
        return jury;
    }

    private void requireJury(User jury) {
        if (jury == null || jury.getRole() != Role.JURY) {
            throw new RuntimeException("Gecerli bir juri kullanicisi gerekli.");
        }
    }
}
