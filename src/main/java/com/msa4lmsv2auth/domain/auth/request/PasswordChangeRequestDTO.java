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
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,20}$"
                , message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다."
        )
        String newPassword
) {
}
