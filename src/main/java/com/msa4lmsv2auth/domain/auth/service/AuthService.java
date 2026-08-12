package com.msa4lmsv2auth.domain.auth.service;

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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CookieManager cookieManager;

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
        Account account = authRepository.findByLoginId(loginRequestDTO.loginId())
                .orElseThrow(() -> new NotRegisteredException("아이디와 비밀번호를 확인해주세요."));

        // 로그인 유형과 계정 역할 확인
        if (!account.getRole().equals(expectedRole)) {
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }

        // 잠긴 계정인지 확인
        if(account.isLocked()) {
            if(account.isLockExpired()) {
                account.unlock();
            } else {
                throw new IllegalStateException("잠긴 계정입니다.");
            }
        }


        // 비밀번호 체크
        if(!passwordEncoder.matches(loginRequestDTO.password(), account.getPassword())) {
            account.increaseFailedLoginAttempts();
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }
        account.resetFailedLoginAttempts();

        // 최초 로그인 여부 확인
        if(account.isRequiresPasswordChange()) {
            return generatePasswordChangeAuthentication(account);
        }

        return this.generateAuthentication(response, account);
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

    // 최초 로그인 시 임시 토큰 발급
    private AuthResponseDTO generatePasswordChangeAuthentication(Account account) {
        String passwordChangeToken  = jwtProvider.generatePasswordChangeToken(account);

        return AuthResponseDTO.forPasswordChange(account, passwordChangeToken);
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
