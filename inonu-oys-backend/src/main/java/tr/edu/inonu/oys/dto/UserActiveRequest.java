package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.NotNull;

public record UserActiveRequest(@NotNull Boolean active) {
}
