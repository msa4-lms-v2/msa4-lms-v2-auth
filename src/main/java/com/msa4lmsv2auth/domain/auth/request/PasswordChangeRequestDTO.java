package com.msa4lmsv2auth.domain.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 변경 요청")
public record PasswordChangeRequestDTO(
        @Schema(description = "현재 사용 중인 비밀번호", example = "CurrentPassword123!")
        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        String currentPassword,

        @Schema(description = "변경할 새 비밀번호(8~20자)", example = "NewPassword123!")
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(regexp = "^[0-9a-zA-Z!@#$%^&*()]{8,20}$"
                , message = "허용하지 않는 양식입니다."
        )
        String newPassword
) {
}
