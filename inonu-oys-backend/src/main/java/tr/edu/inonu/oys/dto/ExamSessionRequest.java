package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tr.edu.inonu.oys.model.ExamSessionType;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamSessionRequest(
        @NotNull Long departmentId,
        @NotNull ExamSessionType sessionType,
        @NotNull LocalDate examDate,
        @NotNull LocalTime startTime,
        LocalTime endTime,
        @NotBlank String location,
        @NotBlank String room,
        @Positive Integer candidateIntervalMinutes,
        boolean published
) {}
