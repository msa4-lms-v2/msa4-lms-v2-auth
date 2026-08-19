package com.msa4lmsv2auth.global.cookie;

import com.msa4lmsv2auth.global.jwt.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;



@Component
@RequiredArgsConstructor
// Request Header에서 특정 쿠키를 획득 (null이 올 수 있기 때문에 Optional로 반환)
public class CookieManager {
    private final JwtConfig jwtConfig;

    // refreshToken 저장 처리
    public void setRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        this.setCookie(
                response
                , jwtConfig.refreshTokenCookieName()
                , refreshToken
                , jwtConfig.refreshTokenCookieExpiry()
                , jwtConfig.reissueUri()
        );
    }

    public Optional<String> getRefreshTokenToCookie(HttpServletRequest request) {
        return this.getCookie(request, jwtConfig.refreshTokenCookieName())
                .map(Cookie::getValue); // 해당 쿠키의 값만 Optional로 가져옴
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        // 쿠키 존재 여부 확인
        if(request.getCookies() == null) {
            return Optional.empty();
        }

        // name에 맞는 쿠키 획득
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst(); // Optional객체를 반환
    }

    // 쿠키 생성 메소드 - jakarta.servlet.http.Cookie는 SameSite를 지원하지 않아 Spring ResponseCookie로 직접 Set-Cookie 헤더를 만든다.
    // Refresh 쿠키는 reissue-uri 하나에서만 쓰이고 SameSite=Strict로도 정상 동작하므로,
    // 별도 CSRF 토큰 체계 없이 SameSite=Strict 자체를 이 쿠키의 CSRF 방어로 삼는다(5.5).
    private void setCookie(HttpServletResponse response, String name, String value, int maxAge, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, value == null ? "" : value)
                .path(path)
                .maxAge(Duration.ofSeconds(Math.max(maxAge, 0)))
                .httpOnly(true) // HTTPOnly 설정: XSS 공격 방지 설정 (설정시 자바스크립트로는 쿠키에 접근 불가)
                .secure(jwtConfig.secure()) // Secure 설정: true시 HTTPS 사용 (MITM 공격 방지)
                .sameSite("Strict") // 같은 사이트 요청에서만 전송 - CSRF 방어
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void removeRefreshTokenToCookie(HttpServletResponse response) {
        this.setCookie(
                response
                , jwtConfig.refreshTokenCookieName()
                , null
                , 0
                , jwtConfig.reissueUri()
        );
    }
}
