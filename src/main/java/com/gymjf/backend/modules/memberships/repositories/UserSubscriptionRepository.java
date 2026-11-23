package com.gymjf.backend.modules.memberships.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gymjf.backend.modules.memberships.domain.SubscriptionStatus;
import com.gymjf.backend.modules.memberships.domain.UserSubscription;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
    List<UserSubscription> findByUserId(Integer userId);

    Optional<UserSubscription> findTopByUserIdOrderByIdDesc(Integer userId);

    List<UserSubscription> findByAccountReceivableId(Integer accountReceivableId);

    /** Suscripción activa que cubre la fecha dada (la de vencimiento más lejana si hay varias). */
    Optional<UserSubscription> findFirstByUserIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateDesc(
            Integer userId, SubscriptionStatus status, LocalDate start, LocalDate end);

    /** Pasa a EXPIRED las suscripciones ACTIVE cuyo último día ya pasó. */
    @Modifying
    @Query("update UserSubscription s set s.status = com.gymjf.backend.modules.memberships.domain.SubscriptionStatus.EXPIRED "
            + "where s.status = com.gymjf.backend.modules.memberships.domain.SubscriptionStatus.ACTIVE and s.endDate < :today")
    int expireDue(@Param("today") LocalDate today);
}
