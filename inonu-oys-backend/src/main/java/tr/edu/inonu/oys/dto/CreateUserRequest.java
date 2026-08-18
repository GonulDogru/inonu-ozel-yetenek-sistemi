package tr.edu.inonu.oys.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tr.edu.inonu.oys.model.Role;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Role role,
        String juryField
) {}
