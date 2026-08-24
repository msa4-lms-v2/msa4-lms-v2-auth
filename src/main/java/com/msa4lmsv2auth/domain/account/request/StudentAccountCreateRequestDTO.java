package com.msa4lmsv2auth.domain.account.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentAccountCreateRequestDTO(
        @NotBlank
        String name,

        @NotBlank
        @Email
        String email,

        String phoneNumber,

        String address,

        @NotNull
        Long departmentId,

        Long majorId,

        @NotNull
        Short admissionYear
) {
}
