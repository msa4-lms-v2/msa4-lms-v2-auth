package com.msa4lmsv2auth.global.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.msa4lmsv2auth.domain.account.client.AcademicClient;
import com.msa4lmsv2auth.domain.account.client.AcademicProvisioningRejectedException;
import com.msa4lmsv2auth.domain.account.constant.AccountStatus;
import com.msa4lmsv2auth.domain.account.entity.Account;
import com.msa4lmsv2auth.domain.account.repository.AccountRepository;
import com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO;
import com.msa4lmsv2auth.domain.outbox.constant.AccountSyncEventType;
import com.msa4lmsv2auth.domain.outbox.entity.AccountSyncOutbox;
import com.msa4lmsv2auth.domain.outbox.entity.AccountSyncOutboxStatus;
import com.msa4lmsv2auth.domain.outbox.repository.AccountSyncOutboxRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class AccountSyncOutboxBatchProcessorTest {

    @Mock
    private AccountSyncOutboxRepository accountSyncOutboxRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AcademicClient academicClient;

    @InjectMocks
    private AccountSyncOutboxBatchProcessor batchProcessor;

    @Test
    void should_activateAccountAndCompleteEvent_when_academicProvisioningSucceeds() {
        AccountSyncOutbox event = studentEvent(1L);
        event.getPayload().put("admissionCandidateId", 7L);
        Account account = pendingAccount(1L);

        when(accountSyncOutboxRepository.lockNextBatch(any(), anyInt())).thenReturn(List.of(event));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(academicClient.createStudent(any())).thenReturn(new StudentProvisioningResponseDTO(1L, "26001001"));

        batchProcessor.publishPendingBatch();

        org.mockito.ArgumentCaptor<com.msa4lmsv2auth.domain.account.request.StudentProvisioningRequestDTO> captor = org.mockito.ArgumentCaptor.forClass(com.msa4lmsv2auth.domain.account.request.StudentProvisioningRequestDTO.class);
        org.mockito.Mockito.verify(academicClient).createStudent(captor.capture());
        assertThat(captor.getValue().admissionCandidateId()).isEqualTo(7L);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getLoginId()).isEqualTo("26001001");
        assertThat(event.getStatus()).isEqualTo(AccountSyncOutboxStatus.COMPLETED);
    }

    @Test
    void should_retryLater_when_academicCallFailsTransiently() {
        AccountSyncOutbox event = studentEvent(1L);
        Account account = pendingAccount(1L);

        when(accountSyncOutboxRepository.lockNextBatch(any(), anyInt())).thenReturn(List.of(event));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(academicClient.createStudent(any())).thenThrow(new ResourceAccessException("connection timed out"));

        batchProcessor.publishPendingBatch();

        assertThat(event.getStatus()).isEqualTo(AccountSyncOutboxStatus.PENDING);
        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING_PROVISIONING);
    }

    @Test
    void should_requireManualReview_when_academicRejectsRequestPermanently() {
        AccountSyncOutbox event = studentEvent(1L);
        Account account = pendingAccount(1L);

        when(accountSyncOutboxRepository.lockNextBatch(any(), anyInt())).thenReturn(List.of(event));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(academicClient.createStudent(any()))
                .thenThrow(new AcademicProvisioningRejectedException("duplicated email"));

        batchProcessor.publishPendingBatch();

        assertThat(event.getStatus()).isEqualTo(AccountSyncOutboxStatus.MANUAL_REVIEW_REQUIRED);
        assertThat(event.getLastErrorCode()).isEqualTo("ACADEMIC_REJECTED");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING_PROVISIONING);
    }

    @Test
    void should_requireManualReview_when_pendingEventIsOlderThanStaleThreshold() {
        AccountSyncOutbox event = studentEvent(1L);
        when(accountSyncOutboxRepository.lockStaleBatch(any(), anyInt())).thenReturn(List.of(event));

        batchProcessor.expireStaleEntries(Duration.ofHours(24));

        assertThat(event.getStatus()).isEqualTo(AccountSyncOutboxStatus.MANUAL_REVIEW_REQUIRED);
        assertThat(event.getLastErrorCode()).isEqualTo("STALE_PENDING_OVER_24H");
    }

    private AccountSyncOutbox studentEvent(Long accountId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", accountId);
        payload.put("name", "홍길동");
        payload.put("email", "student@example.com");
        payload.put("phoneNumber", "010-1234-5678");
        payload.put("address", "서울특별시");
        payload.put("departmentId", 5L);
        payload.put("admissionYear", (short) 2026);
        return AccountSyncOutbox.create(
                "ACCOUNT", accountId, AccountSyncEventType.STUDENT_PROVISIONING_REQUESTED, payload, 1L
        );
    }

    private Account pendingAccount(Long id) {
        Account account = new Account();
        account.setId(id);
        account.setPassword("encoded-password");
        account.setStatus(AccountStatus.PENDING_PROVISIONING);
        account.setRequiresPasswordChange(true);
        account.setCreatedAt(LocalDateTime.now());
        return account;
    }
}
