package tr.edu.inonu.oys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.ExamSessionDTO;
import tr.edu.inonu.oys.dto.ExamSessionRequest;
import tr.edu.inonu.oys.model.*;
import tr.edu.inonu.oys.repository.ApplicationRepository;
import tr.edu.inonu.oys.repository.CandidateJuryAssignmentRepository;
import tr.edu.inonu.oys.repository.ClassroomRepository;
import tr.edu.inonu.oys.repository.DepartmentRepository;
import tr.edu.inonu.oys.repository.ExamSessionJuryRepository;
import tr.edu.inonu.oys.repository.ExamSessionRepository;

import java.time.LocalTime;
import java.util.List;

@Service
public class ExamSessionService {
    private final ExamSessionRepository examSessionRepository;
    private final DepartmentRepository departmentRepository;
    private final ApplicationRepository applicationRepository;
    private final ClassroomRepository classroomRepository;
    private final ExamSessionJuryRepository examSessionJuryRepository;
    private final CandidateJuryAssignmentRepository candidateJuryAssignmentRepository;

    public ExamSessionService(ExamSessionRepository examSessionRepository,
                              DepartmentRepository departmentRepository,
                              ApplicationRepository applicationRepository,
                              ClassroomRepository classroomRepository,
                              ExamSessionJuryRepository examSessionJuryRepository,
                              CandidateJuryAssignmentRepository candidateJuryAssignmentRepository) {
        this.examSessionRepository = examSessionRepository;
        this.departmentRepository = departmentRepository;
        this.applicationRepository = applicationRepository;
        this.classroomRepository = classroomRepository;
        this.examSessionJuryRepository = examSessionJuryRepository;
        this.candidateJuryAssignmentRepository = candidateJuryAssignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<ExamSessionDTO> findAll() {
        return examSessionRepository.findAllByOrderByExamDateAscStartTimeAsc().stream()
                .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ExamSessionDTO> findByJury(User jury) {
        if (jury == null || jury.getRole() != Role.JURY) {
            throw new RuntimeException("Gecerli bir juri kullanicisi gerekli.");
        }
        return examSessionJuryRepository.findByJuryIdOrderByExamSessionExamDateAscExamSessionStartTimeAsc(jury.getId())
                .stream()
                .map(assignment -> toDto(assignment.getExamSession()))
                .distinct()
                .toList();
    }

    @Transactional
    public ExamSessionDTO create(ExamSessionRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı."));
        validateRequest(request);

        ExamSession session = new ExamSession();
        apply(session, department, request);
        return toDto(examSessionRepository.save(session));
    }

    @Transactional
    public ExamSessionDTO update(Long id, ExamSessionRequest request) {
        ExamSession session = examSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sınav oturumu bulunamadı."));
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı."));
        validateRequest(request);
        apply(session, department, request);
        ExamSession saved = examSessionRepository.save(session);
        assignCandidates(saved);
        return toDto(saved);
    }

    @Transactional
    public List<Application> assignCandidates(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sınav oturumu bulunamadı."));
        return assignCandidates(session);
    }

    @Transactional
    public void publish(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sınav oturumu bulunamadı."));
        assignCandidates(session);
        session.setPublished(true);
        examSessionRepository.save(session);
    }

    private void apply(ExamSession session, Department department, ExamSessionRequest request) {
        session.setDepartment(department);
        session.setSessionType(request.sessionType());
        session.setExamDate(request.examDate());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setLocation(request.location());
        session.setRoom(request.room());
        classroomRepository.findByDepartmentIdAndNameIgnoreCase(department.getId(), request.room())
                .ifPresent(session::setClassroom);
        session.setCandidateIntervalMinutes(request.candidateIntervalMinutes());
        session.setPublished(request.published());
    }

    private List<Application> assignCandidates(ExamSession session) {
        List<Application> applications = applicationRepository
                .findByDepartmentIdAndStatusNotOrderByApplicantLastNameAscApplicantFirstNameAsc(
                        session.getDepartment().getId(), ApplicationStatus.REJECTED)
                .stream()
                .filter(this::hasApprovedJuryPanel)
                .toList();
        LocalTime cursor = session.getStartTime();
        int interval = session.getCandidateIntervalMinutes() != null ? session.getCandidateIntervalMinutes() : 10;

        for (int index = 0; index < applications.size(); index++) {
            Application application = applications.get(index);
            application.setExamSession(session);
            application.setExamOrder(index + 1);
            if (session.getSessionType() == ExamSessionType.INDIVIDUAL) {
                application.setAppointmentStartTime(cursor);
                application.setAppointmentEndTime(cursor.plusMinutes(interval));
                cursor = cursor.plusMinutes(interval);
            } else {
                application.setAppointmentStartTime(session.getStartTime());
                application.setAppointmentEndTime(session.getEndTime());
            }
        }
        return applicationRepository.saveAll(applications);
    }

    private boolean hasApprovedJuryPanel(Application application) {
        return candidateJuryAssignmentRepository.countByApplicationIdAndStatus(
                application.getId(), CandidateJuryAssignmentStatus.APPROVED) >= 3;
    }

    private void validateRequest(ExamSessionRequest request) {
        if (request.sessionType() == ExamSessionType.INDIVIDUAL
                && (request.candidateIntervalMinutes() == null || request.candidateIntervalMinutes() <= 0)) {
            throw new RuntimeException("Bireysel sınavlarda aday başına süre girilmelidir.");
        }
        if (request.endTime() != null && !request.endTime().isAfter(request.startTime())) {
            throw new RuntimeException("Bitiş saati başlangıç saatinden sonra olmalıdır.");
        }
    }

    private ExamSessionDTO toDto(ExamSession session) {
        return new ExamSessionDTO(session, examSessionJuryRepository.findByExamSessionId(session.getId()));
    }
}
