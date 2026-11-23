package com.gymjf.backend.modules.memberships.services;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.memberships.domain.AccessMode;
import com.gymjf.backend.modules.memberships.domain.SubscriptionStatus;
import com.gymjf.backend.modules.memberships.domain.UserSubscription;
import com.gymjf.backend.modules.memberships.dtos.AccessCheckResponse;
import com.gymjf.backend.modules.memberships.repositories.UserSubscriptionRepository;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;

import lombok.RequiredArgsConstructor;

/** Decide si una persona puede entrar al gimnasio según el modo de acceso y su suscripción. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccessService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final GymSettingsService gymSettingsService;
    private final CurrentUser currentUser;
    private final Clock clock;

    public AccessCheckResponse check(Integer userId) {
        currentUser.requireOwnerOrAdmin(userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        AccessMode mode = gymSettingsService.currentMode();
        LocalDate today = LocalDate.now(clock);

        UserSubscription valid = userSubscriptionRepository
                .findFirstByUserIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateDesc(
                        userId, SubscriptionStatus.ACTIVE, today, today)
                .orElse(null);

        AccessCheckResponse.AccessCheckResponseBuilder response = AccessCheckResponse.builder()
                .userId(userId).accessMode(mode);
        if (valid != null) {
            return response.allowed(true)
                    .reason("Suscripción vigente hasta el " + valid.getEndDate() + " (vence a las 00:00 del día siguiente)")
                    .subscriptionId(valid.getId()).validUntil(valid.getEndDate()).build();
        }
        if (mode == AccessMode.FREE) {
            return response.allowed(true)
                    .reason("Acceso libre: el gimnasio no exige plan para entrar en este momento").build();
        }
        return response.allowed(false)
                .reason("Acceso restringido: no tiene una suscripción vigente. Debe renovar su plan").build();
    }
}
