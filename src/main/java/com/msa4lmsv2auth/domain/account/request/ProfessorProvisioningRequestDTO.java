package com.msa4lmsv2auth.domain.account.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

// Auth → Academic RequestDTO
public record ProfessorProvisioningRequestDTO(
        Long userId,
        String name,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate birthDate,
        String email,
        String phoneNumber,
        String address,
        Long departmentId,
        Short hireYear
) {
    public ProfessorProvisioningRequestDTO(Long userId, String name, String email, String phoneNumber,
                                            String address, Long departmentId, Short hireYear) {
        this(userId, name, null, email, phoneNumber, address, departmentId, hireYear);
    }
}
