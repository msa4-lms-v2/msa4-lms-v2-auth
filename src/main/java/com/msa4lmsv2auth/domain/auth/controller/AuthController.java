package com.msa4lmsv2auth.domain.auth.controller;

import com.msa4lmsv2auth.domain.auth.request.LoginRequestDTO;
import com.msa4lmsv2auth.domain.auth.response.AuthResponseDTO;
import com.msa4lmsv2auth.domain.auth.service.AuthService;
import com.msa4lmsv2auth.global.config.openapi.CustomApiResponse;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;
import com.msa4lmsv2auth.global.security.constant.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Auth", description = "로그인, 토큰 재발급 및 로그아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "학생 로그인",
            description = "로그인 ID와 비밀번호를 학생 계정으로 검증하고 JWT를 발급합니다.",
            security = {}
    )
    @SecurityRequirements // 인증 없이 사용
    @CustomApiResponse(value = {
            CustomResponseCode.NOT_REGISTERED_ERROR,
            CustomResponseCode.INVALID_PARAMETER_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/student/login")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> studentLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = LoginRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "학생 로그인",
                                    value = "{\"loginId\":\"26001001\",\"password\":\"qwer123!\"}"
                            )
                    )
            )
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(
                authService.login(response, loginRequestDTO, Role.STUDENT)
        ));
    }

    @Operation(
            summary = "교수 로그인",
            description = "로그인 ID와 비밀번호를 교수 계정으로 검증하고 JWT를 발급합니다.",
            security = {}
    )
    @SecurityRequirements
    @CustomApiResponse(value = {
            CustomResponseCode.NOT_REGISTERED_ERROR,
            CustomResponseCode.INVALID_PARAMETER_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/professor/login")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> professorLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = LoginRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "교수 로그인",
                                    value = "{\"loginId\":\"p26001001\",\"password\":\"qwer123!\"}"
                            )
                    )
            )
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(
                authService.login(response, loginRequestDTO, Role.PROFESSOR)
        ));
    }

    @Operation(
            summary = "관리자 로그인",
            description = "로그인 ID와 비밀번호를 관리자 계정으로 검증하고 JWT를 발급합니다.",
            security = {}
    )
    @SecurityRequirements
    @CustomApiResponse(value = {
            CustomResponseCode.NOT_REGISTERED_ERROR,
            CustomResponseCode.INVALID_PARAMETER_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/admin/login")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> adminLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = LoginRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "관리자 로그인",
                                    value = "{\"loginId\":\"a26001001\",\"password\":\"qwer123!\"}"
                            )
                    )
            )
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(
                authService.login(response, loginRequestDTO, Role.ADMIN)
        ));
    }


    @Operation(
            summary = "JWT 재발급",
            description = "Refresh Token을 검증하고 새로운 Access Token을 발급합니다.",
            security = {}
    )
    @SecurityRequirements
    @CustomApiResponse(value = {
            CustomResponseCode.INVALID_TOKEN_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/reissue-token")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> reissue(
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(authService.reissue(request, response)));
    }

    @Operation(
            summary = "로그아웃",
            description = "로그인 사용자의 Refresh Token을 폐기하고 인증 쿠키를 제거합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @CustomApiResponse(value = {
            CustomResponseCode.UNAUTHENTICATED_ERROR,
            CustomResponseCode.INVALID_TOKEN_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<GlobalResponseDTO<Void>> logout(
            HttpServletResponse response,
            Authentication authentication
    ) {
        long userId = Long.parseLong(authentication.getName());

        authService.logout(response, userId);

        return ResponseEntity.ok(GlobalResponseDTO.success());
    }
}
