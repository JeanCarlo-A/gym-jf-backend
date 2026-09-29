package com.gymjf.backend.modules.memberships.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.memberships.domain.UserSubscription;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
    List<UserSubscription> findByUserId(Integer userId);
}
