package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.NotNull;
import tr.edu.inonu.oys.model.JuryAssignmentRole;

public record JuryAssignmentRequest(@NotNull Long juryId, @NotNull Long departmentId, JuryAssignmentRole assignmentRole) {}
