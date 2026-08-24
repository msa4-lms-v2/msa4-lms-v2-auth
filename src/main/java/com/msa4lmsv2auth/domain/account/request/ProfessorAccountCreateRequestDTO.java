package com.msa4lmsv2auth.domain.account.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProfessorAccountCreateRequestDTO(
        @NotBlank String name,
        @NotBlank @Email String email,
        String phoneNumber,
        String address,
        @NotNull  @Positive  Long departmentId,
        @NotNull Short hireYear
) {
}