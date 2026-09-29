package com.gymjf.backend.modules.memberships.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.memberships.domain.SubscriptionPlan;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {
}
