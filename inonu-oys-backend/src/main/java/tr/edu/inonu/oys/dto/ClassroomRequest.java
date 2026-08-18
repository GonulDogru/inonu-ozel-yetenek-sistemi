package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClassroomRequest(
        @NotNull Long departmentId,
        @NotBlank String name,
        @Min(1) int capacity,
        String building,
        boolean active
) {}
