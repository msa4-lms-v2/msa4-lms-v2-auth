package com.msa4lmsv2auth.domain.account.repository;

import com.msa4lmsv2auth.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByLoginId(String loginId);

}
