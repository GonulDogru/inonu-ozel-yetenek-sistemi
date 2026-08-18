package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ApplicationAcademicRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("500.0") Double obp
) {}
