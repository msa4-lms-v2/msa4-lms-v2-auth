package com.msa4lmsv2auth.global.jwt;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtConfig(
        boolean secure
        , String issuer
        , String type
        , int accessTokenExpiry
        , int refreshTokenExpiry
        , String refreshTokenCookieName
        , int refreshTokenCookieExpiry
        , String kid
        , String privateKeyB64
        , String publicKeyB64
        , String accessAudience
        , String refreshAudience
        , String headerKey
        , String scheme
        , String reissueUri
) {
}