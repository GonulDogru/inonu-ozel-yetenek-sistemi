package tr.edu.inonu.oys.dto;

import java.util.List;

public class AutoScheduleResultDTO {
    private final Long departmentId;
    private final String departmentName;
    private final String examType;
    private final int scheduledCandidateCount;
    private final int sessionCount;
    private final List<ExamSessionDTO> sessions;

    public AutoScheduleResultDTO(Long departmentId, String departmentName, String examType,
                                 int scheduledCandidateCount, List<ExamSessionDTO> sessions) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.examType = examType;
        this.scheduledCandidateCount = scheduledCandidateCount;
        this.sessionCount = sessions.size();
        this.sessions = sessions;
    }

    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getExamType() { return examType; }
    public int getScheduledCandidateCount() { return scheduledCandidateCount; }
    public int getSessionCount() { return sessionCount; }
    public List<ExamSessionDTO> getSessions() { return sessions; }
}
