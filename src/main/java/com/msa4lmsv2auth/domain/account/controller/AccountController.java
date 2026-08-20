package com.msa4lmsv2auth.domain.account.controller;

import com.msa4lmsv2auth.domain.account.request.StudentAccountCreateRequestDTO;
import com.msa4lmsv2auth.domain.account.response.AccountResponseDTO;
import com.msa4lmsv2auth.domain.account.service.AccountService;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/students")
    public ResponseEntity<GlobalResponseDTO<AccountResponseDTO>> createStudentAccount(
            @Valid @RequestBody StudentAccountCreateRequestDTO request
    ) {
        return ResponseEntity.ok(GlobalResponseDTO.success(accountService.createStudent(request)));
    }

}
