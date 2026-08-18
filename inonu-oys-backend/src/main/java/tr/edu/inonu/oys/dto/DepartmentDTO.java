package tr.edu.inonu.oys.dto;

import tr.edu.inonu.oys.model.ExamType;

public class DepartmentDTO {
    private Long id;
    private String name;
    private String code;
    private ExamType examType;
    private int quota;
    private double baseScoreRequirement;
    private boolean talentAdmissionEnabled;
    private boolean trimScores;
    private Integer defaultCandidateIntervalMinutes;
    private Integer defaultSessionDurationMinutes;
    private Integer defaultBreakMinutes;
    private Integer requiredPrimaryJuryCount;
    private Integer requiredBackupJuryCount;
    private Integer jurySelfInactiveDeadlineHours;

    public DepartmentDTO(Long id, String name, String code, ExamType examType, int quota,
                         double baseScoreRequirement, boolean trimScores,
                         boolean talentAdmissionEnabled,
                         Integer defaultCandidateIntervalMinutes,
                         Integer defaultSessionDurationMinutes,
                         Integer defaultBreakMinutes,
                         Integer requiredPrimaryJuryCount,
                         Integer requiredBackupJuryCount,
                         Integer jurySelfInactiveDeadlineHours) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.examType = examType;
        this.quota = quota;
        this.baseScoreRequirement = baseScoreRequirement;
        this.talentAdmissionEnabled = talentAdmissionEnabled;
        this.trimScores = trimScores;
        this.defaultCandidateIntervalMinutes = defaultCandidateIntervalMinutes;
        this.defaultSessionDurationMinutes = defaultSessionDurationMinutes;
        this.defaultBreakMinutes = defaultBreakMinutes;
        this.requiredPrimaryJuryCount = requiredPrimaryJuryCount;
        this.requiredBackupJuryCount = requiredBackupJuryCount;
        this.jurySelfInactiveDeadlineHours = jurySelfInactiveDeadlineHours;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public ExamType getExamType() { return examType; }
    public int getQuota() { return quota; }
    public double getBaseScoreRequirement() { return baseScoreRequirement; }
    public boolean isTalentAdmissionEnabled() { return talentAdmissionEnabled; }
    public boolean isTrimScores() { return trimScores; }
    public Integer getDefaultCandidateIntervalMinutes() { return defaultCandidateIntervalMinutes; }
    public Integer getDefaultSessionDurationMinutes() { return defaultSessionDurationMinutes; }
    public Integer getDefaultBreakMinutes() { return defaultBreakMinutes; }
    public Integer getRequiredPrimaryJuryCount() { return requiredPrimaryJuryCount; }
    public Integer getRequiredBackupJuryCount() { return requiredBackupJuryCount; }
    public Integer getJurySelfInactiveDeadlineHours() { return jurySelfInactiveDeadlineHours; }
}
