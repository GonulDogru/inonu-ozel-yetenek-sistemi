package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AutoScheduleRequest(
        @NotNull Long departmentId,
        @NotNull LocalDate examDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        List<Long> classroomIds,
        boolean published
) {}
