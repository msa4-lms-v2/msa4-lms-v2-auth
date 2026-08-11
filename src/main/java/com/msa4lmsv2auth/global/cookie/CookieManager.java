package com.msa4lmsv2auth.global.cookie;

import com.msa4lmsv2auth.global.jwt.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

    // 쿠키 생성 메소드
    // 쿠킹 세팅만 하면 되니 리턴 할 값이 없음 void로 생성
    private void setCookie(HttpServletResponse response, String name, String value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, value); // 해당 이름과 값으로 쿠키 인스턴스 생성
        cookie.setPath(path); // 쿠키를 사용할 path 설정
        cookie.setMaxAge(maxAge); // 쿠키 유효 시간 설정
        cookie.setHttpOnly(true); // HTTPOnly 설정: XSS 공격 방지 설정 (설정시 자바스크립트로는 쿠키에 접근 불가)
        cookie.setSecure(jwtConfig.secure()); // Secure 설정: true시 HTTPS 사용 (MITM 공격 방지)

        response.addCookie(cookie);
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
