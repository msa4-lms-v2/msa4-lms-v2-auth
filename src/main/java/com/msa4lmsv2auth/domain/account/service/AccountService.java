package com.msa4lmsv2auth.domain.account.service;

import com.msa4lmsv2auth.domain.account.client.AcademicClient;
import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.account.repository.AccountRepository;
import com.msa4lmsv2auth.domain.account.request.ProfessorAccountCreateRequestDTO;
import com.msa4lmsv2auth.domain.account.request.ProfessorProvisioningRequestDTO;
import com.msa4lmsv2auth.domain.account.request.StudentAccountCreateRequestDTO;
import com.msa4lmsv2auth.domain.account.request.StudentProvisioningRequestDTO;
import com.msa4lmsv2auth.domain.account.response.AccountResponseDTO;
import com.msa4lmsv2auth.domain.account.response.ProfessorProvisioningResponseDTO;
import com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO;
import com.msa4lmsv2auth.global.security.constant.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {
    private static final String TEMPORARY_PASSWORD = "password123!";

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final AcademicClient academicClient;

    // 학생 계정 생성
    public AccountResponseDTO createStudent(
            StudentAccountCreateRequestDTO request
    ) {
        // auth 계정 생성
        Account account = new Account();
        account.setLoginId(null);
        account.setPassword(
                passwordEncoder.encode(TEMPORARY_PASSWORD)
        );
        account.setRole(Role.STUDENT);
        account.setStatus(AccountStatus.PENDING_PROVISIONING);
        account.setRequiresPasswordChange(true);

        // auth DB 저장
        Account saveAccount = accountRepository.save(account);

        // academic으로 학생 정보 전달
        StudentProvisioningRequestDTO academicRequest =
                new StudentProvisioningRequestDTO(
                        saveAccount.getId(),
                        request.name(),
                        request.email(),
                        request.phoneNumber(),
                        request.address(),
                        request.departmentId(),
                        request.majorId(),
                        request.admissionYear()
                );
        // academic으로 전송해 학번 생성 후 auth로 데이터를 받음
        StudentProvisioningResponseDTO academicResponse = academicClient.createStudent(academicRequest);

        // academic에서 받은 학번을 auth 계정에 반영
        saveAccount.setLoginId(academicResponse.loginId());
        saveAccount.setStatus(AccountStatus.ACTIVE);

        Account completeAccount = accountRepository.save(saveAccount);

        // 프론트 응답 형태로 반환
        return AccountResponseDTO.from(completeAccount);
    }


    // 교수 계정 생성
    public AccountResponseDTO createProfessor(
            ProfessorAccountCreateRequestDTO request
    ) {
        // auth 계정 생성
        Account account = new Account();
        account.setLoginId(null);
        account.setPassword(
                passwordEncoder.encode(TEMPORARY_PASSWORD)
        );
        account.setRole(Role.PROFESSOR);
        account.setStatus(AccountStatus.PENDING_PROVISIONING);
        account.setRequiresPasswordChange(true);

        // auth DB 저장
        Account saveAccount = accountRepository.save(account);

        // academic으로 교수 정보 전달
        ProfessorProvisioningRequestDTO academicRequest =
                new ProfessorProvisioningRequestDTO(
                        saveAccount.getId(),
                        request.name(),
                        request.email(),
                        request.phoneNumber(),
                        request.address(),
                        request.departmentId(),
                        request.hireYear()
                );
        // academic으로 전송해 학번 생성 후 auth로 데이터를 받음
        ProfessorProvisioningResponseDTO academicResponse = academicClient.createProfessor(academicRequest);

        // academic에서 받은 학번을 auth 계정에 반영
        saveAccount.setLoginId(academicResponse.loginId());
        saveAccount.setStatus(AccountStatus.ACTIVE);

        Account completeAccount = accountRepository.save(saveAccount);

        // 프론트 응답 형태로 반환
        return AccountResponseDTO.from(completeAccount);
    }
}
