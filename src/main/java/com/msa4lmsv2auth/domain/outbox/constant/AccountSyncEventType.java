package com.msa4lmsv2auth.domain.outbox.constant;

// account_sync_outbox.event_type에 저장되는 값. AccountService(발행)와
// AccountSyncOutboxBatchProcessor(처리)가 함께 참조하는 계약이라 별도 상수로 둔다.
public final class AccountSyncEventType {
    public static final String STUDENT_PROVISIONING_REQUESTED = "StudentProvisioningRequested";
    public static final String PROFESSOR_PROVISIONING_REQUESTED = "ProfessorProvisioningRequested";

    private AccountSyncEventType() {
    }
}
