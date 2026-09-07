package com.msa4lmsv2auth.domain.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.account.repository.AccountRepository;
import com.msa4lmsv2auth.domain.account.request.ProfessorAccountCreateRequestDTO;
import com.msa4lmsv2auth.domain.account.request.StudentAccountCreateRequestDTO;
import com.msa4lmsv2auth.domain.account.response.AccountResponseDTO;
import com.msa4lmsv2auth.domain.outbox.constant.AccountSyncEventType;
import com.msa4lmsv2auth.domain.outbox.service.AccountSyncOutboxService;
import com.msa4lmsv2auth.global.security.constant.Role;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountSyncOutboxService accountSyncOutboxService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void should_createPendingAccountAndRecordOutboxEvent_when_studentAccountIsCreated() {
        StudentAccountCreateRequestDTO request = new StudentAccountCreateRequestDTO(
                "홍길동", "student@example.com", "010-1234-5678", "서울특별시", 5L, (short) 2026
        );
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(1L);
            return account;
        });

        AccountResponseDTO response = accountService.createStudent(request);

        assertThat(response.status()).isEqualTo(AccountStatus.PENDING_PROVISIONING);
        assertThat(response.loginId()).isNull();

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(accountSyncOutboxService).record(
                eq("ACCOUNT"),
                eq(1L),
                eq(AccountSyncEventType.STUDENT_PROVISIONING_REQUESTED),
                payloadCaptor.capture(),
                eq(1L)
        );
        assertThat(payloadCaptor.getValue())
                .containsEntry("userId", 1L)
                .containsEntry("name", "홍길동")
                .containsEntry("email", "student@example.com")
                .containsEntry("departmentId", 5L)
                .containsEntry("admissionYear", (short) 2026);
    }

    @Test
    void should_createPendingAccountAndRecordOutboxEvent_when_professorAccountIsCreated() {
        ProfessorAccountCreateRequestDTO request = new ProfessorAccountCreateRequestDTO(
                "김교수", "professor@example.com", "010-9876-5432", "서울특별시", 5L, (short) 2026
        );
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(2L);
            return account;
        });

        AccountResponseDTO response = accountService.createProfessor(request);

        assertThat(response.status()).isEqualTo(AccountStatus.PENDING_PROVISIONING);
        assertThat(response.role()).isEqualTo(Role.PROFESSOR);

        verify(accountSyncOutboxService).record(
                eq("ACCOUNT"),
                eq(2L),
                eq(AccountSyncEventType.PROFESSOR_PROVISIONING_REQUESTED),
                anyMap(),
                eq(1L)
        );
    }
}
