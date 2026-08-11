package com.msa4lmsv2auth.domain.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "로그인 시 필요 데이터")
public record LoginRequestDTO(
        @NotBlank(message = "아이디는 필수입니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9]{4,20}$",
                message = "아이디 형식이 올바르지 않습니다."
        )
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = "^[0-9a-zA-Z!@#$%^&*()]{8,20}$"
                , message = "허용하지 않는 양식입니다."
        )
        String password
) {
}