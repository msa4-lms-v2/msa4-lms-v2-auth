package com.msa4lmsv2auth.domain.account.request;

public record StudentProvisioningRequestDTO(
        Long userId,       // Auth에서 추가되는 값
        String name,
        String email,
        String phoneNumber,
        String address,
        Long departmentId,
        Short admissionYear
) {
}
