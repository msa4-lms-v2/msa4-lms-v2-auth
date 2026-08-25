package com.msa4lmsv2auth.domain.auth.controller;

import com.msa4lmsv2auth.domain.auth.request.InitialPasswordChangeRequestDTO;
import com.msa4lmsv2auth.domain.auth.request.LoginRequestDTO;
import com.msa4lmsv2auth.domain.auth.request.PasswordChangeRequestDTO;
import com.msa4lmsv2auth.domain.auth.response.AuthResponseDTO;
import com.msa4lmsv2auth.domain.auth.service.AuthService;
import com.msa4lmsv2auth.global.config.openapi.CustomApiResponse;
import com.msa4lmsv2auth.global.error.custom.business.InvalidTokenException;
import com.msa4lmsv2auth.global.jwt.JwtProvider;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;
import com.msa4lmsv2auth.global.security.constant.Role;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;


@Tag(name = "Auth", description = "로그인, 토큰 재발급 및 로그아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @Operation(
            summary = "학생 로그인",
            description = "로그인 ID와 비밀번호를 학생 계정으로 검증합니다. 최초 로그인 계정에는 일반 Access Token 대신 비밀번호 변경 전용 토큰을 발급합니다.",
            security = {}
    )
    @SecurityRequirements // 인증 없이 사용
    @CustomApiResponse(value = {
            CustomResponseCode.LOGIN_FAILED_ERROR,
            CustomResponseCode.VALIDATION_ERROR,
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
            description = "로그인 ID와 비밀번호를 교수 계정으로 검증합니다. 최초 로그인 계정에는 일반 Access Token 대신 비밀번호 변경 전용 토큰을 발급합니다.",
            security = {}
    )
    @SecurityRequirements
    @CustomApiResponse(value = {
            CustomResponseCode.LOGIN_FAILED_ERROR,
            CustomResponseCode.VALIDATION_ERROR,
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
            description = "로그인 ID와 비밀번호를 관리자 계정으로 검증합니다. 최초 로그인 계정에는 일반 Access Token 대신 비밀번호 변경 전용 토큰을 발급합니다.",
            security = {}
    )
    @SecurityRequirements
    @CustomApiResponse(value = {
            CustomResponseCode.LOGIN_FAILED_ERROR,
            CustomResponseCode.VALIDATION_ERROR,
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


    // 일반 비밀번호 변경: AccessToken 사용
    @Operation(
            summary = "비밀번호 변경",
            description = "현재 비밀번호를 확인한 뒤 새 비밀번호로 교체합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @CustomApiResponse(value = {
            CustomResponseCode.LOGIN_FAILED_ERROR,
            CustomResponseCode.UNAUTHENTICATED_ERROR,
            CustomResponseCode.VALIDATION_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/password")
    public ResponseEntity<GlobalResponseDTO<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequestDTO request
    ) {
        long userId = Long.parseLong(authentication.getName());

        authService.changePassword(userId, request);

        return ResponseEntity.ok(GlobalResponseDTO.success());
    }

    // 최초 비밀번호 변경: Password Change Token 사용
    @Operation(
            summary = "최초 로그인 비밀번호 변경",
            description = "최초 로그인 응답으로 발급된 비밀번호 변경 전용 토큰을 사용합니다. 현재 비밀번호 입력 없이 새 비밀번호로 변경하며, 일반 Access Token은 사용할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @CustomApiResponse(value = {
            CustomResponseCode.INVALID_TOKEN_ERROR,
            CustomResponseCode.VALIDATION_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PatchMapping("/initial-password")
    public ResponseEntity<GlobalResponseDTO<Void>> changeInitialPassword(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody InitialPasswordChangeRequestDTO request
    ) {
        String token = resolveBearerToken(authorization);

        Claims claims =
                jwtProvider.extractPasswordChangeClaims(token);

        long userId = Long.parseLong(claims.getSubject());

        authService.changeInitialPassword(userId, request);

        return ResponseEntity.ok(GlobalResponseDTO.success());
    }

    // Authorization 헤더에서 Bearer 토큰의 실제 토큰 문자열만 추출하는 코드
    private String resolveBearerToken(String authorization) {
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            throw new InvalidTokenException(
                    "Authorization 헤더가 올바르지 않습니다."
            );
        }

        String token = authorization.substring(7);

        if (token.isBlank()) {
            throw new InvalidTokenException(
                    "토큰이 존재하지 않습니다."
            );
        }

        return token;
    }
}
