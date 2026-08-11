package com.msa4lmsv2auth.domain.auth.controller;

import com.msa4lmsv2auth.domain.auth.request.LoginRequestDTO;
import com.msa4lmsv2auth.domain.auth.response.AuthResponseDTO;
import com.msa4lmsv2auth.domain.auth.service.AuthService;
import com.msa4lmsv2auth.global.config.openapi.CustomApiResponse;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;
import com.msa4lmsv2auth.global.security.constant.Role;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @SecurityRequirements // 인증 없이 사용
    @CustomApiResponse(value = {
            CustomResponseCode.NOT_REGISTERED_ERROR,
            CustomResponseCode.INVALID_PARAMETER_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/student/login")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> studentLogin(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(
                authService.login(response, loginRequestDTO, Role.STUDENT)
        ));
    }

    @SecurityRequirements
    @CustomApiResponse(value = {
            CustomResponseCode.NOT_REGISTERED_ERROR,
            CustomResponseCode.INVALID_PARAMETER_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/professor/login")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> professorLogin(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(
                authService.login(response, loginRequestDTO, Role.PROFESSOR)
        ));
    }

    @SecurityRequirements
    @CustomApiResponse(value = {
            CustomResponseCode.NOT_REGISTERED_ERROR,
            CustomResponseCode.INVALID_PARAMETER_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/admin/login")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> adminLogin(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(
                authService.login(response, loginRequestDTO, Role.ADMIN)
        ));
    }


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
