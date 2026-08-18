package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record JuryAvailabilityRequest(
        @NotNull Long departmentId,
        @NotNull Long juryId,
        @NotNull LocalDate availableDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
) {}
