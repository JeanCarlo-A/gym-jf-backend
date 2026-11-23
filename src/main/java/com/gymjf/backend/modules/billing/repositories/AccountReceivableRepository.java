package com.gymjf.backend.modules.billing.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gymjf.backend.modules.billing.domain.AccountReceivable;

public interface AccountReceivableRepository extends JpaRepository<AccountReceivable, Integer> {
    List<AccountReceivable> findByUserId(Integer userId);

    /** Deudas con saldo abierto de un usuario (pass BigDecimal.ZERO). */
    List<AccountReceivable> findByUserIdAndPendingBalanceGreaterThan(Integer userId, BigDecimal minimum);

    /** Todas las deudas con saldo abierto (pass BigDecimal.ZERO). */
    List<AccountReceivable> findByPendingBalanceGreaterThan(BigDecimal minimum);

    @Query("select coalesce(sum(a.pendingBalance), 0) from AccountReceivable a where a.pendingBalance > 0")
    BigDecimal totalOpenDebt();

    long countByPendingBalanceGreaterThan(BigDecimal minimum);
}
