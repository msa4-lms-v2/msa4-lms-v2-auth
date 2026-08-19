package com.msa4lmsv2auth.domain.auth.session;

import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.global.error.custom.business.InvalidTokenException;
import com.msa4lmsv2auth.global.error.custom.business.RefreshSessionUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshSessionService {

    private final RefreshSessionRepository refreshSessionRepository;

    public RefreshSession create(Account account, String jti, Instant expiresAt, Instant lastDbVerifiedAt) {
        RefreshSession session = fromAccount(account, hash(jti), expiresAt, lastDbVerifiedAt);
        execute(() -> refreshSessionRepository.save(session));
        return session;
    }

    public RefreshSession validate(long userId, String jti) {
        RefreshSession session = executeWithResult(() -> refreshSessionRepository.findByUserId(userId))
                .orElseThrow(() -> new InvalidTokenException("폐기되었거나 존재하지 않는 Refresh Session입니다."));
        if (!MessageDigest.isEqual(
                session.jtiHash().getBytes(StandardCharsets.UTF_8),
                hash(jti).getBytes(StandardCharsets.UTF_8)
        )) {
            throw new InvalidTokenException("이미 회전되었거나 유효하지 않은 Refresh Token입니다.");
        }
        return session;
    }

    public RefreshSession rotate(
            RefreshSession current,
            Account account,
            String newJti,
            Instant expiresAt,
            Instant lastDbVerifiedAt
    ) {
        RefreshSession next = fromAccount(account, hash(newJti), expiresAt, lastDbVerifiedAt);
        return rotate(current, next);
    }

    public RefreshSession rotateWithoutDbVerification(
            RefreshSession current,
            String newJti,
            Instant expiresAt
    ) {
        RefreshSession next = new RefreshSession(
                current.userId(),
                hash(newJti),
                current.loginId(),
                current.role(),
                current.requiresPasswordChange(),
                current.accountCreatedAt(),
                current.lastDbVerifiedAt(),
                expiresAt
        );
        return rotate(current, next);
    }

    public void delete(long userId) {
        execute(() -> refreshSessionRepository.deleteByUserId(userId));
    }

    private RefreshSession rotate(RefreshSession current, RefreshSession next) {
        boolean rotated = executeWithResult(() -> refreshSessionRepository.rotate(current.jtiHash(), next));
        if (!rotated) {
            throw new InvalidTokenException("이미 사용되었거나 폐기된 Refresh Token입니다.");
        }
        return next;
    }

    private RefreshSession fromAccount(Account account, String jtiHash, Instant expiresAt, Instant lastDbVerifiedAt) {
        return new RefreshSession(
                account.getId(),
                jtiHash,
                account.getLoginId(),
                account.getRole(),
                account.isRequiresPasswordChange(),
                account.getCreatedAt(),
                lastDbVerifiedAt,
                expiresAt
        );
    }

    static String hash(String jti) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(jti.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    private void execute(Runnable action) {
        try {
            action.run();
        } catch (DataAccessException e) {
            throw new RefreshSessionUnavailableException("Refresh Session 저장소를 사용할 수 없습니다.");
        }
    }

    private <T> T executeWithResult(SupplierWithDataAccess<T> action) {
        try {
            return action.get();
        } catch (DataAccessException e) {
            throw new RefreshSessionUnavailableException("Refresh Session 저장소를 사용할 수 없습니다.");
        }
    }

    @FunctionalInterface
    private interface SupplierWithDataAccess<T> {
        T get();
    }
}
