package com.msa4lmsv2auth.domain.account.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record StudentAccountCreateRequestDTO(
        @NotBlank
        String name,

        @NotNull
        @Past
        LocalDate birthDate,

        @NotBlank
        @Email
        String email,

        String phoneNumber,

        String address,

        @NotNull
        Long departmentId,

        @NotNull
        Short admissionYear
) {
    public StudentAccountCreateRequestDTO(String name, String email, String phoneNumber, String address,
                                          Long departmentId, Short admissionYear) {
        this(name, null, email, phoneNumber, address, departmentId, admissionYear);
    }
}
