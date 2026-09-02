package com.msa4lmsv2auth.domain.auth.response;

import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.account.response.AccountResponseDTO;
import com.msa4lmsv2auth.domain.auth.session.RefreshSession;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답. 일반 로그인과 최초 로그인에 따라 발급되는 토큰이 다릅니다.")
public record AuthResponseDTO(
        @Schema(description = "로그인 계정 정보")
        AccountResponseDTO account,

        @Schema(
                description = "일반 로그인 Access Token. 최초 로그인으로 비밀번호 변경이 필요하면 null입니다.",
                example = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImF1dGgta2V5LTEiLCJ0eXAiOiJKV1QifQ...",
                nullable = true
        )
        String accessToken,

        @Schema(
                description = "최초 로그인 비밀번호 변경 전용 토큰. 일반 로그인에서는 null이며 /api/auth/initial-password에서만 사용합니다.",
                example = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImF1dGgta2V5LTEiLCJ0eXAiOiJKV1QifQ...",
                nullable = true
        )
        String passwordChangeToken
) {
    public static AuthResponseDTO from(
            Account account,
            String accessToken
    ) {
        return new AuthResponseDTO(
                AccountResponseDTO.from(account),
                accessToken,
                null
        );
    }

    public static AuthResponseDTO from(
            RefreshSession session,
            String accessToken
    ) {
        return new AuthResponseDTO(
                AccountResponseDTO.from(session),
                accessToken,
                null
        );
    }

    public static AuthResponseDTO forPasswordChange(
            Account account,
            String passwordChangeToken
    ) {
        return new AuthResponseDTO(
                AccountResponseDTO.from(account),
                null,
                passwordChangeToken
        );
    }
}
