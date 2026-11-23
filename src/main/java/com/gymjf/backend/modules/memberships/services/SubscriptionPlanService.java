package com.gymjf.backend.modules.memberships.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.memberships.domain.PlanType;
import com.gymjf.backend.modules.memberships.domain.SubscriptionPlan;
import com.gymjf.backend.modules.memberships.dtos.CreateSubscriptionPlanRequest;
import com.gymjf.backend.modules.memberships.dtos.SubscriptionPlanResponse;
import com.gymjf.backend.modules.memberships.dtos.UpdateSubscriptionPlanRequest;
import com.gymjf.backend.modules.memberships.repositories.SubscriptionPlanRepository;

import lombok.RequiredArgsConstructor;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
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
                .orElseThrow(() -> new ResourceNotFoundException("Plan de suscripción no encontrado"));
        return mapToResponse(plan);
    }

    @Transactional
    public SubscriptionPlanResponse create(CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .planName(request.getPlanName())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .type(request.getType() != null ? request.getType() : PlanType.MEMBERSHIP)
                .build();
        applyDayPassRule(plan);

        subscriptionPlanRepository.save(plan);

        return mapToResponse(plan);
    }

    @Transactional
    public SubscriptionPlanResponse update(Integer id, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de suscripción no encontrado"));

        if (request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getDurationDays() != null) {
            plan.setDurationDays(request.getDurationDays());
        }
        if (request.getType() != null) {
            plan.setType(request.getType());
        }
        applyDayPassRule(plan);

        subscriptionPlanRepository.save(plan);

        return mapToResponse(plan);
    }

    @Transactional
    public void delete(Integer id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de suscripción no encontrado"));
        subscriptionPlanRepository.delete(plan);
    }

    /** El pase diario siempre dura 1 día (vence a las 00:00 del día siguiente). */
    private void applyDayPassRule(SubscriptionPlan plan) {
        if (plan.getType() == PlanType.DAY_PASS) {
            plan.setDurationDays(1);
        }
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .type(plan.getType())
                .id(plan.getId())
                .planName(plan.getPlanName())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .build();
    }
}
