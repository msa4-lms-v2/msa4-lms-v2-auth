package com.msa4lmsv2auth.domain.account.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record ProfessorAccountCreateRequestDTO(
        @NotBlank String name,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Email String email,
        String phoneNumber,
        String address,
        @NotNull  @Positive  Long departmentId,
        @NotNull Short hireYear
) {
    public ProfessorAccountCreateRequestDTO(String name, String email, String phoneNumber, String address,
                                             Long departmentId, Short hireYear) {
        this(name, null, email, phoneNumber, address, departmentId, hireYear);
    }
}
