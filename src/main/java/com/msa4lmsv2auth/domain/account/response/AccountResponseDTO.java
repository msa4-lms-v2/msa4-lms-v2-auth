package com.msa4lmsv2auth.domain.account.response;

import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.global.security.constant.Role;
import com.msa4lmsv2auth.domain.auth.session.RefreshSession;

import java.time.LocalDateTime;

public record AccountResponseDTO(
        Long id
        , String loginId
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

    public static AccountResponseDTO from(RefreshSession session) {
        return new AccountResponseDTO(
                session.userId(),
                session.loginId(),
                session.role(),
                AccountStatus.ACTIVE,
                session.requiresPasswordChange(),
                session.accountCreatedAt()
        );
    }
}
