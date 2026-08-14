package com.msa4lmsv2auth.domain.auth.response;

import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.account.response.AccountResponseDTO;
import com.msa4lmsv2auth.domain.auth.session.RefreshSession;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 response")
public record AuthResponseDTO(
        AccountResponseDTO account,
        String accessToken
) {
    public static AuthResponseDTO from(Account account, String accessToken) {
        return new AuthResponseDTO(AccountResponseDTO.from(account), accessToken);
    }

    public static AuthResponseDTO from(RefreshSession session, String accessToken) {
        return new AuthResponseDTO(AccountResponseDTO.from(session), accessToken);
    }
}
