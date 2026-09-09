package com.msa4lmsv2auth.domain.account.service;

import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.account.repository.AccountRepository;
import com.msa4lmsv2auth.domain.account.request.ProfessorAccountCreateRequestDTO;
import com.msa4lmsv2auth.domain.account.request.StudentAccountCreateRequestDTO;
import com.msa4lmsv2auth.domain.account.response.AccountResponseDTO;
import com.msa4lmsv2auth.domain.outbox.constant.AccountSyncEventType;
import com.msa4lmsv2auth.domain.outbox.service.AccountSyncOutboxService;
import com.msa4lmsv2auth.global.security.constant.Role;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private static final String TEMPORARY_PASSWORD = "password123!";

    private static final String AGGREGATE_TYPE_ACCOUNT = "ACCOUNT";
    private static final long INITIAL_SOURCE_VERSION = 1L;

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final AccountSyncOutboxService accountSyncOutboxService;
    private final com.msa4lmsv2auth.domain.outbox.repository.AccountSyncOutboxRepository accountSyncOutboxRepository;

    // 학생 계정 생성
    // 계정 저장과 Outbox 이벤트 기록을 같은 트랜잭션에서 처리한다. Academic 호출은 이 자리에서 하지 않고
    // Auth Pod 내부 Worker(AccountSyncOutboxBatchProcessor)가 비동기로 재시도하며 성공 후에만 ACTIVE로 전환한다.
    @Transactional(rollbackFor = Exception.class)
    public AccountResponseDTO createStudent(
            StudentAccountCreateRequestDTO request
    ) {
        Account account = new Account();
        account.setLoginId(null);
        account.setPassword(
                passwordEncoder.encode(TEMPORARY_PASSWORD)
        );
        account.setRole(Role.STUDENT);
        account.setStatus(AccountStatus.PENDING_PROVISIONING);
        account.setRequiresPasswordChange(true);

        Account savedAccount = accountRepository.save(account);

        accountSyncOutboxService.record(
                AGGREGATE_TYPE_ACCOUNT,
                savedAccount.getId(),
                AccountSyncEventType.STUDENT_PROVISIONING_REQUESTED,
                studentProvisioningPayload(savedAccount.getId(), request),
                INITIAL_SOURCE_VERSION
        );

        return AccountResponseDTO.from(savedAccount);
    }

    // 교수 계정 생성
    @Transactional(rollbackFor = Exception.class)
    public AccountResponseDTO createProfessor(
            ProfessorAccountCreateRequestDTO request
    ) {
        Account account = new Account();
        account.setLoginId(null);
        account.setPassword(
                passwordEncoder.encode(TEMPORARY_PASSWORD)
        );
        account.setRole(Role.PROFESSOR);
        account.setStatus(AccountStatus.PENDING_PROVISIONING);
        account.setRequiresPasswordChange(true);

        Account savedAccount = accountRepository.save(account);

        accountSyncOutboxService.record(
                AGGREGATE_TYPE_ACCOUNT,
                savedAccount.getId(),
                AccountSyncEventType.PROFESSOR_PROVISIONING_REQUESTED,
                professorProvisioningPayload(savedAccount.getId(), request),
                INITIAL_SOURCE_VERSION
        );

        return AccountResponseDTO.from(savedAccount);
    }

    @Transactional
    public AccountResponseDTO createAdmission(
            com.msa4lmsv2auth.domain.account.request.AdmissionAccountCreateRequestDTO request) {
        Account existing = findAdmissionAccount(request.admissionCandidateId());
        if (existing != null) return AccountResponseDTO.from(existing);
        Account account = new Account();
        account.setPassword(passwordEncoder.encode(TEMPORARY_PASSWORD));
        account.setRole(Role.STUDENT);
        account.setStatus(AccountStatus.PENDING_PROVISIONING);
        account.setRequiresPasswordChange(true);
        Account saved = accountRepository.save(account);
        Map<String, Object> payload = studentProvisioningPayload(saved.getId(), new StudentAccountCreateRequestDTO(
                request.name(), request.email(), request.phoneNumber(), request.address(), request.departmentId(), request.admissionYear()));
        payload.put("admissionCandidateId", request.admissionCandidateId());
        payload.put("advisorProfessorId", request.advisorProfessorId());
        accountSyncOutboxService.record(AGGREGATE_TYPE_ACCOUNT, saved.getId(),
                AccountSyncEventType.STUDENT_PROVISIONING_REQUESTED, payload, INITIAL_SOURCE_VERSION);
        return AccountResponseDTO.from(saved);
    }

    @Transactional(readOnly = true)
    public com.msa4lmsv2auth.domain.account.response.AccountRegistrationResponseDTO getAccount(Long id) {
        return registrationStatus(accountRepository.findById(id).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public com.msa4lmsv2auth.domain.account.response.AccountRegistrationResponseDTO getAdmissionAccount(Long candidateId) {
        Account account = findAdmissionAccount(candidateId);
        return account == null ? null : registrationStatus(account);
    }

    private Account findAdmissionAccount(Long candidateId) {
        return accountSyncOutboxRepository.findAdmissionProvisioningEvent(candidateId)
                .flatMap(event -> accountRepository.findById(event.getAggregateId()))
                .orElse(null);
    }

    private com.msa4lmsv2auth.domain.account.response.AccountRegistrationResponseDTO registrationStatus(Account account) {
        String state = accountSyncOutboxRepository.findFirstByAggregateIdOrderByIdDesc(account.getId())
                .map(event -> event.getStatus().name()).orElse(null);
        return com.msa4lmsv2auth.domain.account.response.AccountRegistrationResponseDTO.from(account, state);
    }

    private Map<String, Object> studentProvisioningPayload(Long accountId, StudentAccountCreateRequestDTO request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", accountId);
        payload.put("name", request.name());
        payload.put("email", request.email());
        payload.put("phoneNumber", request.phoneNumber());
        payload.put("address", request.address());
        payload.put("departmentId", request.departmentId());
        payload.put("admissionYear", request.admissionYear());
        return payload;
    }

    private Map<String, Object> professorProvisioningPayload(Long accountId, ProfessorAccountCreateRequestDTO request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", accountId);
        payload.put("name", request.name());
        payload.put("email", request.email());
        payload.put("phoneNumber", request.phoneNumber());
        payload.put("address", request.address());
        payload.put("departmentId", request.departmentId());
        payload.put("hireYear", request.hireYear());
        return payload;
    }
}
