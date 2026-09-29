package com.gymjf.backend.modules.billing.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.billing.domain.AccountReceivable;

public interface AccountReceivableRepository extends JpaRepository<AccountReceivable, Integer> {
    List<AccountReceivable> findByUserId(Integer userId);
}
