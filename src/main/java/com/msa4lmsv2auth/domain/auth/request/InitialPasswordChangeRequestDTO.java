package com.msa4lmsv2auth.domain.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "최초 로그인 비밀번호 변경 요청")
public record InitialPasswordChangeRequestDTO(

        @Schema(
                description = "변경할 새 비밀번호",
                example = "NewPassword123!"
        )
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^[0-9a-zA-Z!@#$%^&*()]{8,20}$",
                message = "허용하지 않는 비밀번호 양식입니다."
        )
        String newPassword
) {
}