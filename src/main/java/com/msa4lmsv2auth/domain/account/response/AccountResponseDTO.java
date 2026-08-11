package com.msa4lmsv2auth.domain.account.response;

import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.global.security.constant.Role;

import java.time.LocalDateTime;

public record AccountResponseDTO(
        Long id
        , String login_id
        , Role role
        , AccountStatus status
        , boolean requiresPasswordChange
        , LocalDateTime createdAt
) {
    public static AccountResponseDTO from(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getLoginId(),
                account.getRole(),
                account.getStatus(),
                account.isRequiresPasswordChange(),
                account.getCreatedAt()
        );
    }
}
