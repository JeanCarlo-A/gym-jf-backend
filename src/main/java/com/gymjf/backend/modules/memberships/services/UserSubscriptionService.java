package com.gymjf.backend.modules.memberships.services;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.billing.services.AccountReceivableService;
import com.gymjf.backend.modules.memberships.domain.SubscriptionPlan;
import com.gymjf.backend.modules.memberships.domain.SubscriptionStatus;
import com.gymjf.backend.modules.memberships.domain.UserSubscription;
import com.gymjf.backend.modules.memberships.dtos.CreateUserSubscriptionRequest;
import com.gymjf.backend.modules.memberships.dtos.UpdateUserSubscriptionRequest;
import com.gymjf.backend.modules.memberships.dtos.UserSubscriptionResponse;
import com.gymjf.backend.modules.memberships.repositories.SubscriptionPlanRepository;
import com.gymjf.backend.modules.memberships.repositories.UserSubscriptionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.gymjf.backend.modules.memberships.dtos.RenewSubscriptionRequest;
import com.gymjf.backend.shared.domain.exceptions.BadRequestException;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final CurrentUser currentUser;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final AccountReceivableService accountReceivableService;
    @PersistenceContext
    private EntityManager entityManager;

    public List<UserSubscriptionResponse> getAll() {
        return (currentUser.isAdmin() ? userSubscriptionRepository.findAll() : userSubscriptionRepository.findByUserId(currentUser.id())).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<UserSubscriptionResponse> getByUserId(Integer userId) {
        currentUser.requireOwnerOrAdmin(userId);
        return userSubscriptionRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserSubscriptionResponse getById(Integer id) {
        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));
        currentUser.requireOwnerOrAdmin(subscription.getUser().getId());
        return mapToResponse(subscription);
    }

    @Transactional
    public UserSubscriptionResponse create(CreateUserSubscriptionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan de suscripción no encontrado"));

        LocalDate start = request.getStartDate() != null ? request.getStartDate() : LocalDate.now(clock);
        SubscriptionStatus status = request.getStatus() != null ? request.getStatus() : SubscriptionStatus.PENDING;
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .plan(plan)
                .startDate(start)
                .endDate(calculateEndDate(start, plan))
                .status(status)
                // Pendiente de pago = genera su deuda; si el administrador la registra ACTIVE se asume ya pagada
                .accountReceivable(status == SubscriptionStatus.PENDING
                        ? accountReceivableService.issue(user, plan.getPrice()) : null)
                .build();

        userSubscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    @Transactional
    public UserSubscriptionResponse update(Integer id, UpdateUserSubscriptionRequest request) {
        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));

        if (request.getStartDate() != null) {
            subscription.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            subscription.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            subscription.setStatus(request.getStatus());
        }

        if (subscription.getEndDate().isBefore(subscription.getStartDate())) {
            throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la de inicio");
        }

        userSubscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    /**
     * Renueva o compra un plan con el procedimiento almacenado sp_renovar_suscripcion: crea la suscripción
     * PENDING (pendiente de pago) y su cuenta por cobrar en una sola transacción. Un cliente solo puede
     * renovar la suya; el administrador cualquiera.
     */
    @Transactional
    public UserSubscriptionResponse renew(RenewSubscriptionRequest request) {
        currentUser.requireOwnerOrAdmin(request.getUserId());
        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        if (!subscriptionPlanRepository.existsById(request.getPlanId())) {
            throw new ResourceNotFoundException("Plan de suscripción no encontrado");
        }
        // "Hoy" lo manda Java (hora de Costa Rica): CURRENT_DATE de MySQL usaría la hora UTC del servidor
        entityManager.createNativeQuery("CALL sp_renovar_suscripcion(:userId, :planId, :today)")
                .setParameter("userId", request.getUserId())
                .setParameter("planId", request.getPlanId())
                .setParameter("today", LocalDate.now(clock))
                .executeUpdate();
        UserSubscription created = userSubscriptionRepository.findTopByUserIdOrderByIdDesc(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));
        return mapToResponse(created);
    }

    /** Marca como EXPIRED las suscripciones ACTIVE que ya cumplieron su último día. Devuelve cuántas. */
    @Transactional
    public int expireDue() {
        return userSubscriptionRepository.expireDue(LocalDate.now(clock));
    }

    /** Se ejecuta cada día a las 00:00 (hora de Costa Rica). */
    @Scheduled(cron = "0 0 0 * * *", zone = "${application.timezone:America/Costa_Rica}")
    public void expireDueDaily() {
        int expired = expireDue();
        if (expired > 0) {
            log.info("Suscripciones vencidas hoy: {}", expired);
        }
    }

    /** La duración del plan incluye el día de inicio: 30 días desde el 1 vencen el 30; el pase diario, el mismo día. */
    private LocalDate calculateEndDate(LocalDate start, SubscriptionPlan plan) {
        return start.plusDays(plan.getDurationDays() - 1L);
    }

    @Transactional
    public void delete(Integer id) {
        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));
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
                .accountReceivableId(subscription.getAccountReceivable() != null
                        ? subscription.getAccountReceivable().getId() : null)
                .build();
    }
}
