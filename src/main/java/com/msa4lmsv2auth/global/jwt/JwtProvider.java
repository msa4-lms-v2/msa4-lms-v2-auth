package com.msa4lmsv2auth.global.jwt;

import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.global.cookie.CookieManager;
import com.msa4lmsv2auth.global.error.custom.business.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {
    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;
    private static final long PASSWORD_CHANGE_TOKEN_EXPIRE_TIME = 10 * 60 * 1000L; // 10분

    public JwtProvider(JwtConfig jwtConfig, CookieManager cookieManager) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtConfig.secret()));
    }

   public String generateToken(Account account, int ttl) {
       Date now = new Date();

       return Jwts.builder()
               .header()
               .type(jwtConfig.type())
               .and()
               .subject(String.valueOf(account.getId()))
               .issuer((jwtConfig.issuer()))
               .issuedAt(now)
               .expiration(new Date(now.getTime()+ttl))
               .claim("role",account.getRole())
               .signWith(secretKey)
               .compact();
   }

   // 최초 로그인 임시 토큰 생성
    public String generatePasswordChangeToken(Account account) {
        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + PASSWORD_CHANGE_TOKEN_EXPIRE_TIME
        );
        return Jwts.builder()
                .subject(account.getLoginId())
                .claim("role", account.getRole())
                .claim("purpose", "PASSWORD_CHANGE")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

   public String generateAccessToken(Account account) {
        return this.generateToken(account, jwtConfig.accessTokenExpiry());
   }

   public String generateRefreshToken(Account account) {
        return this.generateToken(account, jwtConfig.refreshTokenExpiry());
   }

   public Claims extractClaims(String token) {
        try{
            return Jwts.parser()
                    .verifyWith(this.secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    ;
        } catch (ExpiredJwtException e){
            throw new InvalidTokenException("토큰이 만료됐습니다.");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("서명이 위조된 토큰입니다.");
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("토큰 형식이 올바르지 않습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("토큰 검증에 실패했습니다.");
        }
   }

   // 비밀번호 변경 용 JWT 확인
    public Claims parsePasswordChangeToken(String token) {
        Claims claims = extractClaims(token);

        String purpose = claims.get("purpose", String.class);

        if(!"PASSWORD_CHANGE".equals(purpose)) {
            throw new InvalidTokenException("비밀번호 변경용 토큰이 아닙니다.");
        }
        return claims;
    }


}
