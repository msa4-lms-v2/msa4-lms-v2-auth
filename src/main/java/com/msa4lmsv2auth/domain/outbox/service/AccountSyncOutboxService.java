package com.msa4lmsv2auth.domain.outbox.service;

import com.msa4lmsv2auth.domain.outbox.entity.AccountSyncOutbox;
import com.msa4lmsv2auth.domain.outbox.repository.AccountSyncOutboxRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountSyncOutboxService {

    private final AccountSyncOutboxRepository accountSyncOutboxRepository;

    // 호출부(AccountService)의 계정 저장 트랜잭션에 그대로 합류한다 - 별도 트랜잭션을 새로 열지 않는다.
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String aggregateType, Long aggregateId, String eventType,
                       Map<String, Object> payload, Long sourceVersion) {
        accountSyncOutboxRepository.save(
                AccountSyncOutbox.create(aggregateType, aggregateId, eventType, payload, sourceVersion)
        );
    }
}
