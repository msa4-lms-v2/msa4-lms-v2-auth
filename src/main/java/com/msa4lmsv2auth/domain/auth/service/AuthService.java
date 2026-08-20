package com.msa4lmsv2auth.domain.auth.service;

import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.account.repository.AccountRepository;
import com.msa4lmsv2auth.domain.auth.request.LoginRequestDTO;
import com.msa4lmsv2auth.domain.auth.request.PasswordChangeRequestDTO;
import com.msa4lmsv2auth.domain.auth.response.AuthResponseDTO;
import com.msa4lmsv2auth.domain.auth.session.RefreshSession;
import com.msa4lmsv2auth.domain.auth.session.RefreshSessionService;
import com.msa4lmsv2auth.global.cookie.CookieManager;
import com.msa4lmsv2auth.global.error.custom.BusinessException;
import com.msa4lmsv2auth.global.error.custom.business.InvalidTokenException;
import com.msa4lmsv2auth.global.error.custom.business.NotRegisteredException;
import com.msa4lmsv2auth.global.error.custom.business.RefreshSessionUnavailableException;
import com.msa4lmsv2auth.global.jwt.JwtProvider;
import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;
import com.msa4lmsv2auth.global.security.constant.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration DB_OUTAGE_REFRESH_GRACE_PERIOD = Duration.ofHours(3);

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CookieManager cookieManager;
    private final RefreshSessionService refreshSessionService;
    private final AccountRepository accountRepository;

    // 로그인
    @Transactional(
            rollbackFor = Exception.class,
            noRollbackFor = NotRegisteredException.class
    )
    public AuthResponseDTO login(
            HttpServletResponse response,
            LoginRequestDTO loginRequestDTO,
            Role expectedRole
    ) {

        // user 정보 획득, 가입 여부 체크
        Account account = accountRepository.findByLoginId(loginRequestDTO.loginId())
                .orElseThrow(() -> new NotRegisteredException("아이디와 비밀번호를 확인해주세요."));

        // 로그인 유형과 계정 역할 확인
        if (!account.getRole().equals(expectedRole)) {
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }

        // 계정 잠금은 상태로 판단하고, 만료 시각이 지난 경우 원래 상태로 복원한다.
        if (account.isLocked()) {
            if (account.isLockExpired()) {
                account.unlock();
            } else {
                throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
            }
        }

        // 잠금 해제 후에도 활성 계정만 일반 로그인을 허용한다.
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }

        // 비밀번호 체크
        if (!passwordEncoder.matches(loginRequestDTO.password(), account.getPassword())) {
            account.increaseFailedLoginAttempts();
            accountRepository.save(account);
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }

        account.resetFailedLoginAttempts();
        accountRepository.save(account);

        return this.generateAuthentication(response, account);
    }

    // 토큰 생성
    private AuthResponseDTO generateAuthentication(HttpServletResponse response, Account account) {
        String accessToken = jwtProvider.generateAccessToken(account);
        String refreshToken = jwtProvider.generateRefreshToken(account);

        Claims refreshClaims = jwtProvider.extractRefreshClaims(refreshToken);
        refreshSessionService.create(
                account,
                refreshClaims.getId(),
                refreshClaims.getExpiration().toInstant(),
                Instant.now()
        );

        // refresh cookie 저장
        cookieManager.setRefreshTokenToCookie(response, refreshToken);

        return AuthResponseDTO.from(account, accessToken);
    }

    // 토큰 재발급
    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieManager.getRefreshTokenToCookie(request)
                .orElseThrow(() -> new InvalidTokenException("리프래시 토큰 없음"));

        Claims refreshClaims = jwtProvider.extractRefreshClaims(refreshToken);
        long userId = parseUserId(refreshClaims.getSubject());
        RefreshSession currentSession = refreshSessionService.validate(userId, refreshClaims.getId());

        Account account = null;
        boolean dbVerified = false;
        try {
            account = accountRepository.findById(userId).orElse(null);
            dbVerified = true;
        } catch (DataAccessException ignored) {
            if (Duration.between(currentSession.lastDbVerifiedAt(), Instant.now())
                    .compareTo(DB_OUTAGE_REFRESH_GRACE_PERIOD) > 0) {
                throw new RefreshSessionUnavailableException(
                        "Auth DB 확인 유예 시간이 만료되어 토큰을 재발급할 수 없습니다."
                );
            }
        }

        if (dbVerified && account == null) {
            refreshSessionService.delete(userId);
            throw new InvalidTokenException("유효하지 않은 회원의 토큰입니다.");
        }

        if (account != null && account.getStatus() != AccountStatus.ACTIVE) {
            refreshSessionService.delete(userId);
            throw new InvalidTokenException("계정 상태로 인해 재발급할 수 없습니다.");
        }

        Role role = account != null ? account.getRole() : currentSession.role();
        String accessToken = jwtProvider.generateAccessToken(userId, role);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId, role);
        Claims newRefreshClaims = jwtProvider.extractRefreshClaims(newRefreshToken);

        RefreshSession nextSession;
        if (account != null) {
            nextSession = refreshSessionService.rotate(
                    currentSession,
                    account,
                    newRefreshClaims.getId(),
                    newRefreshClaims.getExpiration().toInstant(),
                    Instant.now()
            );
        } else {
            nextSession = refreshSessionService.rotateWithoutDbVerification(
                    currentSession,
                    newRefreshClaims.getId(),
                    newRefreshClaims.getExpiration().toInstant()
            );
        }

        cookieManager.setRefreshTokenToCookie(response, newRefreshToken);
        return account != null
                ? AuthResponseDTO.from(account, accessToken)
                : AuthResponseDTO.from(nextSession, accessToken);
    }

    // 로그아웃
    @Transactional(rollbackFor = Exception.class)
    public void logout(HttpServletResponse response, long userId) {
        refreshSessionService.delete(userId);
        cookieManager.removeRefreshTokenToCookie(response);
    }

    // 비밀번호 변경
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(long userId, PasswordChangeRequestDTO request) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new NotRegisteredException("계정을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.currentPassword(), account.getPassword())) {
            throw new BusinessException(CustomResponseCode.LOGIN_FAILED_ERROR, "현재 비밀번호가 일치하지 않습니다.");
        }

        account.setPassword(passwordEncoder.encode(request.newPassword()));
        account.setRequiresPasswordChange(false);
    }

    private long parseUserId(String subject) {
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new InvalidTokenException("토큰의 사용자 식별자가 올바르지 않습니다.");
        }
    }
}
