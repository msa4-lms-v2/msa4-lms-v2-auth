package com.msa4lmsv2auth.domain.account.controller;

import com.msa4lmsv2auth.domain.account.request.ProfessorAccountCreateRequestDTO;
import com.msa4lmsv2auth.domain.account.request.StudentAccountCreateRequestDTO;
import com.msa4lmsv2auth.domain.account.response.AccountResponseDTO;
import com.msa4lmsv2auth.domain.account.service.AccountService;
import com.msa4lmsv2auth.global.config.openapi.CustomApiResponse;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/accounts")
@Tag(name = "Account", description = "관리자용 학생·교수 계정 생성 API")
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "학생 계정 생성",
            description = "관리자가 학생 정보를 등록합니다. Academic에서 학번을 생성한 후 Auth 계정을 활성화합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @CustomApiResponse(value = {
            CustomResponseCode.UNAUTHENTICATED_ERROR,
            CustomResponseCode.FORBIDDEN_ERROR,
            CustomResponseCode.DUPLICATE_ERROR,
            CustomResponseCode.VALIDATION_ERROR,
            CustomResponseCode.SERVICE_UNAVAILABLE_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/students")
    public ResponseEntity<GlobalResponseDTO<AccountResponseDTO>> createStudentAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = StudentAccountCreateRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "학생 계정 생성",
                                    value = "{\"name\":\"홍길동\",\"email\":\"student@example.com\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울특별시\",\"departmentId\":5,\"admissionYear\":2026}"
                            )
                    )
            )
            @Valid @RequestBody StudentAccountCreateRequestDTO request
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(accountService.createStudent(request)));
    }

    @Operation(
            summary = "교수 계정 생성",
            description = "관리자가 교수 정보를 등록합니다. Academic에서 교번을 생성한 후 Auth 계정을 활성화합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @CustomApiResponse(value = {
            CustomResponseCode.UNAUTHENTICATED_ERROR,
            CustomResponseCode.FORBIDDEN_ERROR,
            CustomResponseCode.DUPLICATE_ERROR,
            CustomResponseCode.VALIDATION_ERROR,
            CustomResponseCode.SERVICE_UNAVAILABLE_ERROR,
            CustomResponseCode.DB_ERROR,
            CustomResponseCode.SYSTEM_ERROR
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/professors")
    public ResponseEntity<GlobalResponseDTO<AccountResponseDTO>> createProfessorAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProfessorAccountCreateRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "교수 계정 생성",
                                    value = "{\"name\":\"김교수\",\"email\":\"professor@example.com\",\"phoneNumber\":\"010-9876-5432\",\"address\":\"서울특별시\",\"departmentId\":5,\"hireYear\":2026}"
                            )
                    )
            )
            @Valid @RequestBody ProfessorAccountCreateRequestDTO request
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(accountService.createProfessor(request)));
    }

}
