package com.msa4lmsv2auth.global.jwt;

import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.global.cookie.CookieManager;
import com.msa4lmsv2auth.global.error.custom.business.InvalidTokenException;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtConfig jwtConfig;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtProvider(JwtConfig jwtConfig, CookieManager cookieManager) {
        this.jwtConfig = jwtConfig;
        this.privateKey = loadPrivateKey(jwtConfig.privateKeyB64());
        this.publicKey = loadPublicKey(jwtConfig.publicKeyB64());
    }

    public String generateAccessToken(Account account) {
        return generateToken(account, jwtConfig.accessTokenExpiry(), TOKEN_TYPE_ACCESS, jwtConfig.accessAudience(), null);
    }

    public String generateRefreshToken(Account account) {
        return generateToken(account, jwtConfig.refreshTokenExpiry(), TOKEN_TYPE_REFRESH, jwtConfig.refreshAudience(), UUID.randomUUID().toString());
    }

    private String generateToken(Account account, int ttl, String tokenType, String audience, String jti) {
        Date now = new Date();

        JwtBuilder builder = Jwts.builder()
                .header()
                .type(jwtConfig.type())
                .keyId(jwtConfig.kid())
                .and()
                .subject(String.valueOf(account.getId()))
                .issuer(jwtConfig.issuer())
                .audience().add(audience)
                .and()
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl))
                .claim("role", account.getRole())
                .claim("token_type", tokenType);

        if (jti != null) {
            builder.id(jti);
        }

        return builder.signWith(privateKey).compact();
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("토큰이 만료됐습니다.");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("서명이 위조된 토큰입니다.");
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("토큰 형식이 올바르지 않습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("토큰 검증에 실패했습니다.");
        }
    }

    private PrivateKey loadPrivateKey(String base64Pem) {
        try {
            String pem = stripPem(base64Pem);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
        } catch (Exception e) {
            throw new IllegalStateException("JWT 개인키를 불러올 수 없습니다.", e);
        }
    }

    private PublicKey loadPublicKey(String base64Pem) {
        try {
            String pem = stripPem(base64Pem);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pem)));
        } catch (Exception e) {
            throw new IllegalStateException("JWT 공개키를 불러올 수 없습니다.", e);
        }
    }

    private String stripPem(String base64OfPem) {
        String pem = new String(Base64.getDecoder().decode(base64OfPem));
        return pem
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
    }
}
