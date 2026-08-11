package com.msa4lmsv2auth.domain.auth.response;

import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.account.response.AccountResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 response")
public record AuthResponseDTO(
        AccountResponseDTO account,
        String accessToken,
        String passwordChangeToken
) {
    // 정상 로그인
    public static AuthResponseDTO from(Account account, String accessToken) {
        return new AuthResponseDTO(AccountResponseDTO.from(account), accessToken, null);
    }

    // 최초 로그인
    public static AuthResponseDTO forPasswordChange(Account account, String passwordChangeToken) {
        return new AuthResponseDTO(AccountResponseDTO.from(account), null, passwordChangeToken);
    }

}
