package com.msa4lmsv2auth.domain.auth.session;

import java.util.Optional;

public interface RefreshSessionRepository {

    Optional<RefreshSession> findByUserId(long userId);

    void save(RefreshSession session);

    boolean rotate(String expectedJtiHash, RefreshSession session);

    void deleteByUserId(long userId);
}
