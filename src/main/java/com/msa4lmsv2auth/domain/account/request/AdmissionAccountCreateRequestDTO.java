package com.msa4lmsv2auth.domain.account.request;
import jakarta.validation.constraints.*;
public record AdmissionAccountCreateRequestDTO(
        @NotNull @Positive Long admissionCandidateId,
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Email @Size(max = 100) String email,
        @Size(max = 20) String phoneNumber,
        @Size(max = 255) String address,
        @NotNull @Positive Long departmentId,
        @NotNull @Positive Long advisorProfessorId,
        @NotNull @Min(1900) Short admissionYear
) {}
