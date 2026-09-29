package com.gymjf.backend.modules.memberships.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.memberships.domain.SubscriptionPlan;
import com.gymjf.backend.modules.memberships.dtos.CreateSubscriptionPlanRequest;
import com.gymjf.backend.modules.memberships.dtos.SubscriptionPlanResponse;
import com.gymjf.backend.modules.memberships.dtos.UpdateSubscriptionPlanRequest;
import com.gymjf.backend.modules.memberships.repositories.SubscriptionPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public List<SubscriptionPlanResponse> getAll() {
        return subscriptionPlanRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SubscriptionPlanResponse getById(Integer id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan de suscripción no encontrado"));
        return mapToResponse(plan);
    }

    public SubscriptionPlanResponse create(CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .planName(request.getPlanName())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .build();

        subscriptionPlanRepository.save(plan);

        return mapToResponse(plan);
    }

    public SubscriptionPlanResponse update(Integer id, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan de suscripción no encontrado"));

        if (request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getDurationDays() != null) {
            plan.setDurationDays(request.getDurationDays());
        }

        subscriptionPlanRepository.save(plan);

        return mapToResponse(plan);
    }

    public void delete(Integer id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan de suscripción no encontrado"));
        subscriptionPlanRepository.delete(plan);
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .planName(plan.getPlanName())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .build();
    }
}
