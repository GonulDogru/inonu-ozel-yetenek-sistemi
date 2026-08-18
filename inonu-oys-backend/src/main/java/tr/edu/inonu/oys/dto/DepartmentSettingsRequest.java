package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tr.edu.inonu.oys.model.ExamType;

public record DepartmentSettingsRequest(
        String code,
        ExamType examType,
        @NotNull @Min(0) Integer quota,
        @NotNull @DecimalMin("0.0") Double baseScoreRequirement,
        Boolean talentAdmissionEnabled,
        @NotNull Boolean trimScores,
        @Min(1) Integer defaultCandidateIntervalMinutes,
        @Min(1) Integer defaultSessionDurationMinutes,
        @Min(0) Integer defaultBreakMinutes,
        @Min(1) Integer requiredPrimaryJuryCount,
        @Min(0) Integer requiredBackupJuryCount,
        @Min(0) Integer jurySelfInactiveDeadlineHours
) {}
