package com.msa4lmsv2auth.domain.auth.session;

import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.global.error.custom.business.InvalidTokenException;
import com.msa4lmsv2auth.global.error.custom.business.RefreshSessionUnavailableException;
import com.msa4lmsv2auth.global.security.constant.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshSessionServiceTest {

    @Mock
    private RefreshSessionRepository refreshSessionRepository;

    @InjectMocks
    private RefreshSessionService refreshSessionService;

    @Test
    void should_storeOnlyHashedJti_when_sessionIsCreated() {
        Account account = account();
        Instant expiresAt = Instant.now().plusSeconds(3600);

        refreshSessionService.create(account, "plain-jti", expiresAt, Instant.now());

        ArgumentCaptor<RefreshSession> captor = ArgumentCaptor.forClass(RefreshSession.class);
        verify(refreshSessionRepository).save(captor.capture());
        assertThat(captor.getValue().jtiHash())
                .isEqualTo(RefreshSessionService.hash("plain-jti"))
                .doesNotContain("plain-jti");
    }

    @Test
    void should_rejectRefreshToken_when_jtiWasAlreadyRotated() {
        RefreshSession session = session("previous-jti");
        when(refreshSessionRepository.findByUserId(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> refreshSessionService.validate(1L, "replayed-jti"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void should_rejectConcurrentReplay_when_atomicRotationFails() {
        RefreshSession current = session("current-jti");
        when(refreshSessionRepository.rotate(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> refreshSessionService.rotateWithoutDbVerification(
                current,
                "next-jti",
                Instant.now().plusSeconds(3600)
        )).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void should_returnServiceUnavailable_when_redisIsDown() {
        doThrow(new RedisConnectionFailureException("redis down"))
                .when(refreshSessionRepository).deleteByUserId(1L);

        assertThatThrownBy(() -> refreshSessionService.delete(1L))
                .isInstanceOf(RefreshSessionUnavailableException.class);
    }

    private Account account() {
        Account account = new Account();
        account.setId(1L);
        account.setLoginId("26001001");
        account.setRole(Role.STUDENT);
        account.setStatus(AccountStatus.ACTIVE);
        account.setRequiresPasswordChange(false);
        account.setCreatedAt(LocalDateTime.of(2026, 8, 14, 9, 0));
        return account;
    }

    private RefreshSession session(String jti) {
        return new RefreshSession(
                1L,
                RefreshSessionService.hash(jti),
                "26001001",
                Role.STUDENT,
                false,
                LocalDateTime.of(2026, 8, 14, 9, 0),
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
    }
}
