package com.msa4lmsv2auth.domain.account.request;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record AdmissionAccountCreateRequestDTO(
        @NotNull @Positive Long admissionCandidateId,
        @NotBlank @Size(max = 50) String name,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Email @Size(max = 100) String email,
        @Size(max = 20) String phoneNumber,
        @Size(max = 255) String address,
        @NotNull @Positive Long departmentId,
        @NotNull @Positive Long advisorProfessorId,
        @NotNull @Min(1900) Short admissionYear
) {
    public AdmissionAccountCreateRequestDTO(Long admissionCandidateId, String name, String email,
                                             String phoneNumber, String address, Long departmentId,
                                             Long advisorProfessorId, Short admissionYear) {
        this(admissionCandidateId, name, null, email, phoneNumber, address, departmentId,
                advisorProfessorId, admissionYear);
    }
}
