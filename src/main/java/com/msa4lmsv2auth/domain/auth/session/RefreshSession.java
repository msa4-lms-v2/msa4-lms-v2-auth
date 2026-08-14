package com.msa4lmsv2auth.domain.auth.session;

import com.msa4lmsv2auth.global.security.constant.Role;

import java.time.Instant;
import java.time.LocalDateTime;

public record RefreshSession(
        long userId,
        String jtiHash,
        String loginId,
        Role role,
        boolean requiresPasswordChange,
        LocalDateTime accountCreatedAt,
        Instant lastDbVerifiedAt,
        Instant expiresAt
) {
}
