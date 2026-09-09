package com.msa4lmsv2auth.domain.account.response;

import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;

public record AccountRegistrationResponseDTO(Long id, String loginId, AccountStatus status, String provisioningStatus) {
    public static AccountRegistrationResponseDTO from(Account account, String provisioningStatus) {
        return new AccountRegistrationResponseDTO(account.getId(), account.getLoginId(), account.getStatus(), provisioningStatus);
    }
}
