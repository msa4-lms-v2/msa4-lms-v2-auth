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
}
