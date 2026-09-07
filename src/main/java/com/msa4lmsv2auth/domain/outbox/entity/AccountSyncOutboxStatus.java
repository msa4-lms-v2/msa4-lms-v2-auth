package com.msa4lmsv2auth.domain.outbox.entity;

public enum AccountSyncOutboxStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    MANUAL_REVIEW_REQUIRED
}
