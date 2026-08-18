package tr.edu.inonu.oys.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.inonu.oys.dto.AutoScheduleRequest;
import tr.edu.inonu.oys.dto.AutoScheduleResultDTO;
import tr.edu.inonu.oys.dto.ExamSessionDTO;
import tr.edu.inonu.oys.model.*;
import tr.edu.inonu.oys.repository.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AutoSchedulerService {
    private final DepartmentRepository departmentRepository;
    private final ApplicationRepository applicationRepository;
    private final ClassroomRepository classroomRepository;
    private final JuryInactiveSlotRepository inactiveSlotRepository;
    private final ExamSessionRepository examSessionRepository;
    private final ExamSessionJuryRepository examSessionJuryRepository;
    private final UserRepository userRepository;

    public AutoSchedulerService(DepartmentRepository departmentRepository,
                                ApplicationRepository applicationRepository,
                                ClassroomRepository classroomRepository,
                                JuryInactiveSlotRepository inactiveSlotRepository,
                                ExamSessionRepository examSessionRepository,
                                ExamSessionJuryRepository examSessionJuryRepository,
                                UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.applicationRepository = applicationRepository;
        this.classroomRepository = classroomRepository;
        this.inactiveSlotRepository = inactiveSlotRepository;
        this.examSessionRepository = examSessionRepository;
        this.examSessionJuryRepository = examSessionJuryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AutoScheduleResultDTO autoSchedule(AutoScheduleRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new RuntimeException("Bolum bulunamadi."));
        List<Application> candidates = applicationRepository
                .findByDepartmentIdAndStatusOrderByApplicantLastNameAscApplicantFirstNameAsc(
                        department.getId(), ApplicationStatus.PENDING_EVALUATION);
        if (candidates.isEmpty()) {
            throw new RuntimeException("Cizelgelenecek onayli aday bulunamadi.");
        }

        List<Classroom> classrooms = classroomRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(department.getId());
        if (request.classroomIds() != null && !request.classroomIds().isEmpty()) {
            Set<Long> selectedIds = request.classroomIds().stream().collect(Collectors.toSet());
            classrooms = classrooms.stream()
                    .filter(classroom -> selectedIds.contains(classroom.getId()))
                    .toList();
        }
        if (classrooms.isEmpty()) {
            throw new RuntimeException("Secilen bolum icin aktif ve kullanilabilir salon bulunamadi.");
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new RuntimeException("Bitis saati baslangic saatinden sonra olmalidir.");
        }

        List<ExamSession> sessions = new ArrayList<>();
        int scheduled = switch (department.getExamType()) {
            case GROUP -> scheduleGroup(department, candidates, classrooms, request, sessions);
            case INDIVIDUAL, TRACK -> scheduleSequential(department, candidates, classrooms, request, sessions);
        };

        if (scheduled < candidates.size()) {
            throw new RuntimeException("Tum adaylar cizelgelenemedi. Aday: " + candidates.size()
                    + ", cizelgelenen: " + scheduled + ". Salon veya juri musaitligi artirilmali.");
        }

        List<ExamSession> savedSessions = examSessionRepository.saveAll(sessions);
        assignJuries(department, request, savedSessions);
        applicationRepository.saveAll(candidates.subList(0, scheduled));
        return new AutoScheduleResultDTO(department.getId(), department.getName(), department.getExamType().name(),
                scheduled, savedSessions.stream().map(ExamSessionDTO::new).toList());
    }

    private int scheduleSequential(Department department, List<Application> candidates, List<Classroom> classrooms,
                                   AutoScheduleRequest request, List<ExamSession> sessions) {
        int candidateIndex = 0;
        int interval = department.getDefaultCandidateIntervalMinutes() != null
                ? department.getDefaultCandidateIntervalMinutes()
                : (department.getExamType() == ExamType.TRACK ? 5 : 20);
        int breakMinutes = department.getDefaultBreakMinutes() != null ? department.getDefaultBreakMinutes() : 0;

        for (Classroom classroom : classrooms) {
            if (candidateIndex >= candidates.size()) return candidateIndex;
            if (hasClassroomConflict(classroom, request.startTime(), request.endTime(), request)) continue;
            LocalTime cursor = request.startTime();
            ExamSession session = newSession(department, classroom, request, ExamSessionType.INDIVIDUAL);
            int order = 1;

            while (candidateIndex < candidates.size() && !cursor.plusMinutes(interval).isAfter(request.endTime())) {
                Application candidate = candidates.get(candidateIndex++);
                candidate.setExamSession(session);
                candidate.setExamOrder(order++);
                candidate.setAppointmentStartTime(cursor);
                candidate.setAppointmentEndTime(cursor.plusMinutes(interval));
                cursor = cursor.plusMinutes(interval + breakMinutes);
            }

            if (order > 1) {
                session.setEndTime(cursor.minusMinutes(breakMinutes));
                sessions.add(session);
            }
        }
        return candidateIndex;
    }

    private int scheduleGroup(Department department, List<Application> candidates, List<Classroom> classrooms,
                              AutoScheduleRequest request, List<ExamSession> sessions) {
        int candidateIndex = 0;
        int duration = department.getDefaultSessionDurationMinutes() != null
                ? department.getDefaultSessionDurationMinutes() : 120;

        for (Classroom classroom : classrooms) {
            LocalTime cursor = request.startTime();
            while (candidateIndex < candidates.size() && !cursor.plusMinutes(duration).isAfter(request.endTime())) {
                LocalTime sessionEnd = cursor.plusMinutes(duration);
                if (hasClassroomConflict(classroom, cursor, sessionEnd, request)) {
                    cursor = sessionEnd;
                    continue;
                }
                ExamSession session = newSession(department, classroom, request, ExamSessionType.GROUP);
                session.setStartTime(cursor);
                session.setEndTime(sessionEnd);
                int order = 1;
                int capacity = classroom.getCapacity();

                while (candidateIndex < candidates.size() && order <= capacity) {
                    Application candidate = candidates.get(candidateIndex++);
                    candidate.setExamSession(session);
                    candidate.setExamOrder(order++);
                    candidate.setAppointmentStartTime(session.getStartTime());
                    candidate.setAppointmentEndTime(session.getEndTime());
                }

                sessions.add(session);
                cursor = cursor.plusMinutes(duration);
            }
            if (candidateIndex >= candidates.size()) return candidateIndex;
        }
        return candidateIndex;
    }

    private ExamSession newSession(Department department, Classroom classroom, AutoScheduleRequest request,
                                   ExamSessionType sessionType) {
        ExamSession session = new ExamSession();
        session.setDepartment(department);
        session.setSessionType(sessionType);
        session.setExamDate(request.examDate());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setLocation(classroom.getBuilding() != null && !classroom.getBuilding().isBlank()
                ? classroom.getBuilding() : department.getName());
        session.setRoom(classroom.getName());
        session.setClassroom(classroom);
        session.setCandidateIntervalMinutes(sessionType == ExamSessionType.INDIVIDUAL
                ? department.getDefaultCandidateIntervalMinutes() : null);
        session.setPublished(request.published());
        return session;
    }

    private void assignJuries(Department department, AutoScheduleRequest request, List<ExamSession> sessions) {
        List<User> primary = availableJuries(department, request, JuryAssignmentRole.PRIMARY);
        List<User> backup = availableJuries(department, request, JuryAssignmentRole.BACKUP);
        int requiredPrimary = department.getRequiredPrimaryJuryCount() != null ? department.getRequiredPrimaryJuryCount() : 3;
        if (primary.size() + backup.size() < requiredPrimary) {
            throw new RuntimeException("Yeterli aktif juri yok. Gerekli asgari juri: " + requiredPrimary
                    + ", aktif juri: " + (primary.size() + backup.size()));
        }

        for (ExamSession session : sessions) {
            List<ExamSessionJury> assignments = new ArrayList<>();
            int primaryCount = Math.min(requiredPrimary, primary.size());
            for (int i = 0; i < primaryCount; i++) {
                assignments.add(sessionJury(session, primary.get(i), JuryAssignmentRole.PRIMARY, false));
            }
            int missing = requiredPrimary - primaryCount;
            List<Long> selectedPrimaryIds = primary.subList(0, primaryCount).stream()
                    .map(User::getId)
                    .toList();
            List<User> usableBackup = backup.stream()
                    .filter(jury -> !selectedPrimaryIds.contains(jury.getId()))
                    .toList();
            if (usableBackup.size() < missing) {
                throw new RuntimeException("Eksik asıl juri icin yeterli yedek juri bulunamadi.");
            }
            for (int i = 0; i < missing; i++) {
                assignments.add(sessionJury(session, usableBackup.get(i), JuryAssignmentRole.BACKUP, true));
            }
            examSessionJuryRepository.saveAll(assignments);
        }
    }

    private List<User> availableJuries(Department department, AutoScheduleRequest request, JuryAssignmentRole role) {
        List<Long> inactiveIds = inactiveSlotRepository
                .findByDepartmentIdAndInactiveDate(department.getId(), request.examDate()).stream()
                .filter(slot -> overlaps(slot.getStartTime(), slot.getEndTime(), request.startTime(), request.endTime()))
                .map(slot -> slot.getJury().getId())
                .distinct()
                .toList();
        return userRepository.findJuriesByDepartmentAndAssignmentRole(department.getId(), role.name()).stream()
                .filter(jury -> !inactiveIds.contains(jury.getId()))
                .filter(jury -> !hasJuryConflict(jury, request.startTime(), request.endTime(), request))
                .toList();
    }

    private boolean overlaps(LocalTime inactiveStart, LocalTime inactiveEnd, LocalTime examStart, LocalTime examEnd) {
        if (inactiveStart == null || inactiveEnd == null) return true;
        return inactiveStart.isBefore(examEnd) && inactiveEnd.isAfter(examStart);
    }

    private boolean hasClassroomConflict(Classroom classroom, LocalTime startTime, LocalTime endTime,
                                         AutoScheduleRequest request) {
        return !examSessionRepository
                .findClassroomConflicts(classroom.getId(), request.examDate(), startTime, endTime)
                .isEmpty();
    }

    private boolean hasJuryConflict(User jury, LocalTime startTime, LocalTime endTime, AutoScheduleRequest request) {
        return !examSessionJuryRepository
                .findJuryConflicts(jury.getId(), request.examDate(), startTime, endTime)
                .isEmpty();
    }

    private ExamSessionJury sessionJury(ExamSession session, User jury, JuryAssignmentRole role, boolean replacement) {
        ExamSessionJury assignment = new ExamSessionJury();
        assignment.setExamSession(session);
        assignment.setJury(jury);
        assignment.setAssignmentRole(role);
        assignment.setReplacement(replacement);
        return assignment;
    }
}
