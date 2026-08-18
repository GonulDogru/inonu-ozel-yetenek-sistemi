package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.NotNull;

public record UserDepartmentAssignmentRequest(
        @NotNull Long departmentId,
        @NotNull Boolean assigned
) {
}
