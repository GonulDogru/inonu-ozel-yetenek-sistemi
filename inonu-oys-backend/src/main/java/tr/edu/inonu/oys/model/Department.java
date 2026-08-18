package tr.edu.inonu.oys.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamType examType = ExamType.INDIVIDUAL;

    private int quota;
    private double baseScoreRequirement;
    private boolean talentAdmissionEnabled = true;
    private boolean trimScores;
    private Integer defaultCandidateIntervalMinutes;
    private Integer defaultSessionDurationMinutes;
    private Integer defaultBreakMinutes;
    private Integer requiredPrimaryJuryCount = 3;
    private Integer requiredBackupJuryCount = 1;
    private Integer jurySelfInactiveDeadlineHours = 24;

    @ManyToMany(mappedBy = "assignedDepartments", fetch = FetchType.LAZY)
    @JsonBackReference("user-departments")
    private Set<User> juryMembers = new HashSet<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public ExamType getExamType() { return examType; }
    public void setExamType(ExamType examType) { this.examType = examType; }
    public int getQuota() { return quota; }
    public void setQuota(int quota) { this.quota = quota; }
    public double getBaseScoreRequirement() { return baseScoreRequirement; }
    public void setBaseScoreRequirement(double baseScoreRequirement) { this.baseScoreRequirement = baseScoreRequirement; }
    public boolean isTalentAdmissionEnabled() { return talentAdmissionEnabled; }
    public void setTalentAdmissionEnabled(boolean talentAdmissionEnabled) { this.talentAdmissionEnabled = talentAdmissionEnabled; }
    public boolean isTrimScores() { return trimScores; }
    public void setTrimScores(boolean trimScores) { this.trimScores = trimScores; }
    public Integer getDefaultCandidateIntervalMinutes() { return defaultCandidateIntervalMinutes; }
    public void setDefaultCandidateIntervalMinutes(Integer defaultCandidateIntervalMinutes) { this.defaultCandidateIntervalMinutes = defaultCandidateIntervalMinutes; }
    public Integer getDefaultSessionDurationMinutes() { return defaultSessionDurationMinutes; }
    public void setDefaultSessionDurationMinutes(Integer defaultSessionDurationMinutes) { this.defaultSessionDurationMinutes = defaultSessionDurationMinutes; }
    public Integer getDefaultBreakMinutes() { return defaultBreakMinutes; }
    public void setDefaultBreakMinutes(Integer defaultBreakMinutes) { this.defaultBreakMinutes = defaultBreakMinutes; }
    public Integer getRequiredPrimaryJuryCount() { return requiredPrimaryJuryCount; }
    public void setRequiredPrimaryJuryCount(Integer requiredPrimaryJuryCount) { this.requiredPrimaryJuryCount = requiredPrimaryJuryCount; }
    public Integer getRequiredBackupJuryCount() { return requiredBackupJuryCount; }
    public void setRequiredBackupJuryCount(Integer requiredBackupJuryCount) { this.requiredBackupJuryCount = requiredBackupJuryCount; }
    public Integer getJurySelfInactiveDeadlineHours() { return jurySelfInactiveDeadlineHours; }
    public void setJurySelfInactiveDeadlineHours(Integer jurySelfInactiveDeadlineHours) { this.jurySelfInactiveDeadlineHours = jurySelfInactiveDeadlineHours; }
    public Set<User> getJuryMembers() { return juryMembers; }
    public void setJuryMembers(Set<User> juryMembers) { this.juryMembers = juryMembers; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
