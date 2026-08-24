package com.msa4lmsv2auth.domain.account.request;

// Auth → Academic RequestDTO
public record ProfessorProvisioningRequestDTO(
        Long userId,
        String name,
        String email,
        String phoneNumber,
        String address,
        Long departmentId,
        Short hireYear
) {
}
