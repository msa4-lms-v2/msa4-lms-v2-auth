package com.msa4lmsv2auth.domain.auth.service;

import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.auth.repository.AuthRepository;
import com.msa4lmsv2auth.domain.auth.request.LoginRequestDTO;
import com.msa4lmsv2auth.domain.auth.response.AuthResponseDTO;
import com.msa4lmsv2auth.global.cookie.CookieManager;
import com.msa4lmsv2auth.global.error.custom.business.InvalidTokenException;
import com.msa4lmsv2auth.global.error.custom.business.NotRegisteredException;
import com.msa4lmsv2auth.global.jwt.JwtProvider;
import com.msa4lmsv2auth.global.security.constant.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CookieManager cookieManager;

    // 로그인
    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO login(
            HttpServletResponse response,
            LoginRequestDTO loginRequestDTO,
            Role expectedRole
    ) {

        // user 정보 획득, 가입 여부 체크
        Account account = authRepository.findByLoginId(loginRequestDTO.loginId())
                .orElseThrow(() -> new NotRegisteredException("아이디와 비밀번호를 확인해주세요."));

        // 로그인 유형과 계정 역할 확인
        if (!account.getRole().equals(expectedRole)) {
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }

        // 계정 상태 확인 (활성 계정만 로그인 허용)
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }

        // 잠금 여부 확인
        if (account.getLockedUntil() != null && account.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }

        // 비밀번호 체크
        if (!passwordEncoder.matches(loginRequestDTO.password(), account.getPassword())) {
            registerFailedAttempt(account);
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }

        account.setFailedLoginAttempts(0);
        account.setLockedUntil(null);
        authRepository.save(account);

        return this.generateAuthentication(response, account);
    }

    private void registerFailedAttempt(Account account) {
        int attempts = account.getFailedLoginAttempts() + 1;
        account.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            account.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        authRepository.save(account);
    }

    // 토큰 생성
    private AuthResponseDTO generateAuthentication(HttpServletResponse response, Account account) {
        String accessToken = jwtProvider.generateAccessToken(account);
        String refreshToken = jwtProvider.generateRefreshToken(account);

        // refresh DB 저장
        account.setRefreshToken(refreshToken);
        authRepository.save(account);

        // refresh cookie 저장
        cookieManager.setRefreshTokenToCookie(response, refreshToken);

        return AuthResponseDTO.from(account, accessToken);
    }

    // 토큰 재발급
    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieManager.getRefreshTokenToCookie(request)
                .orElseThrow(() -> new InvalidTokenException("리프래시 토큰 없음"));

        long userId = Long.parseLong(jwtProvider.extractClaims(refreshToken).getSubject());

        Account account = authRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 회원의 토큰입니다."));

        if(account.getRefreshToken() == null) {
            throw new InvalidTokenException("비로그인 상태입니다.");
        }

        if(!account.getRefreshToken().equals(refreshToken)) {
            throw new InvalidTokenException("토큰이 일치하지 않습니다.");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTokenException("계정 상태로 인해 재발급할 수 없습니다.");
        }

        return this.generateAuthentication(response, account);
    }

    // 로그아웃
    @Transactional(rollbackFor = Exception.class)
    public void logout(HttpServletResponse response, long userId) {
        Account account = authRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 회원입니다."));

        account.setRefreshToken(null);
        authRepository.save(account);

        cookieManager.removeRefreshTokenToCookie(response);

    }
}
