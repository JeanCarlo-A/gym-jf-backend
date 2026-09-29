package com.gymjf.backend.modules.memberships.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.memberships.domain.SubscriptionPlan;
import com.gymjf.backend.modules.memberships.domain.SubscriptionStatus;
import com.gymjf.backend.modules.memberships.domain.UserSubscription;
import com.gymjf.backend.modules.memberships.dtos.CreateUserSubscriptionRequest;
import com.gymjf.backend.modules.memberships.dtos.UpdateUserSubscriptionRequest;
import com.gymjf.backend.modules.memberships.dtos.UserSubscriptionResponse;
import com.gymjf.backend.modules.memberships.repositories.SubscriptionPlanRepository;
import com.gymjf.backend.modules.memberships.repositories.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;

    public List<UserSubscriptionResponse> getAll() {
        return userSubscriptionRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<UserSubscriptionResponse> getByUserId(Integer userId) {
        return userSubscriptionRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserSubscriptionResponse getById(Integer id) {
        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
        return mapToResponse(subscription);
    }

    public UserSubscriptionResponse create(CreateUserSubscriptionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan de suscripción no encontrado"));

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .plan(plan)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : SubscriptionStatus.ACTIVE)
                .build();

        userSubscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    public UserSubscriptionResponse update(Integer id, UpdateUserSubscriptionRequest request) {
        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));

        if (request.getStartDate() != null) {
            subscription.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            subscription.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            subscription.setStatus(request.getStatus());
        }

        userSubscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    public void delete(Integer id) {
        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
        userSubscriptionRepository.delete(subscription);
    }

    private UserSubscriptionResponse mapToResponse(UserSubscription subscription) {
        return UserSubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser().getId())
                .planId(subscription.getPlan().getId())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .build();
    }
}
