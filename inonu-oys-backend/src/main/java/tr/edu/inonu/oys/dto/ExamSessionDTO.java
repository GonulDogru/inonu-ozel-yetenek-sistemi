package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.ExamSession;
import tr.edu.inonu.oys.model.ExamSessionJury;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ExamSessionDTO {
    private final Long id;
    private final Long departmentId;
    private final String departmentName;
    private final String sessionType;
    private final LocalDate examDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String location;
    private final String room;
    private final Long classroomId;
    private final List<ExamSessionJuryDTO> juries;
    private final Integer candidateIntervalMinutes;
    private final boolean published;

    public ExamSessionDTO(ExamSession session) {
        this(session, List.of());
    }

    public ExamSessionDTO(ExamSession session, List<ExamSessionJury> juryAssignments) {
        this.id = session.getId();
        this.departmentId = session.getDepartment().getId();
        this.departmentName = session.getDepartment().getName();
        this.sessionType = session.getSessionType().name();
        this.examDate = session.getExamDate();
        this.startTime = session.getStartTime();
        this.endTime = session.getEndTime();
        this.location = session.getLocation();
        this.room = session.getRoom();
        this.classroomId = session.getClassroom() != null ? session.getClassroom().getId() : null;
        this.juries = juryAssignments.stream().map(ExamSessionJuryDTO::new).toList();
        this.candidateIntervalMinutes = session.getCandidateIntervalMinutes();
        this.published = session.isPublished();
    }

    public Long getId() { return id; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getSessionType() { return sessionType; }
    public LocalDate getExamDate() { return examDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getRoom() { return room; }
    public Long getClassroomId() { return classroomId; }
    public List<ExamSessionJuryDTO> getJuries() { return juries; }
    public Integer getCandidateIntervalMinutes() { return candidateIntervalMinutes; }
    public boolean isPublished() { return published; }
}
