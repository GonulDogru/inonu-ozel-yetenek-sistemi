package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ApproveCandidateJuryAssignmentsRequest(
        @NotNull Long applicationId,
        @NotEmpty List<Long> juryIds
) {}
