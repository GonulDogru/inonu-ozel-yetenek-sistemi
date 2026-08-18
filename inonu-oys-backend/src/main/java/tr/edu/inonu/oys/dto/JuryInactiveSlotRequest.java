package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record JuryInactiveSlotRequest(
        @NotNull Long departmentId,
        @NotNull LocalDate inactiveDate,
        LocalTime startTime,
        LocalTime endTime,
        String reason
) {}
