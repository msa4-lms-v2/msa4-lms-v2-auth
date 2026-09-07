package com.msa4lmsv2auth.domain.outbox.repository;

import com.msa4lmsv2auth.domain.outbox.entity.AccountSyncOutbox;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountSyncOutboxRepository extends JpaRepository<AccountSyncOutbox, Long> {

    @Query(value = "SELECT * FROM account_sync_outbox "
            + "WHERE status = 'PENDING' AND next_attempt_at <= :now "
            + "ORDER BY id ASC LIMIT :batchSize FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<AccountSyncOutbox> lockNextBatch(@Param("now") LocalDateTime now, @Param("batchSize") int batchSize);

    // 24시간 넘게 완료되지 못한 채 남아있는 행을 골라 수동 검토 대상으로 종결한다.
    @Query(value = "SELECT * FROM account_sync_outbox "
            + "WHERE status IN ('PENDING', 'PROCESSING') AND created_at <= :staleBefore "
            + "ORDER BY id ASC LIMIT :batchSize FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<AccountSyncOutbox> lockStaleBatch(@Param("staleBefore") LocalDateTime staleBefore, @Param("batchSize") int batchSize);
}
