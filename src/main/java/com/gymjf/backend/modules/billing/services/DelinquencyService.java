package com.gymjf.backend.modules.billing.services;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.billing.domain.AccountReceivable;
import com.gymjf.backend.modules.billing.domain.DelinquencyStatus;
import com.gymjf.backend.modules.billing.dtos.DelinquencyResponse;
import com.gymjf.backend.modules.billing.repositories.AccountReceivableRepository;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Morosidad (RF-5.4): un usuario es moroso si la suma de sus deudas abiertas supera el monto límite
 * (₡15 000 por defecto) o si tiene una deuda abierta de más de 30 días. Se calcula al momento con los saldos
 * reales, así que el bloqueo de acceso siempre está al día; la tarea diaria solo marca el estado de las
 * deudas (OVERDUE = pendiente, IN_COLLECTION = en cobro por morosidad) para los reportes.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DelinquencyService {

    private final AccountReceivableRepository accountReceivableRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final Clock clock;

    @Value("${application.billing.delinquency-amount:15000}")
    private BigDecimal maxAmount;

    @Value("${application.billing.delinquency-days:30}")
    private int maxDays;

    /** Días que una deuda nueva tarda en contar como vencida para el monto límite. */
    @Value("${application.billing.grace-days:3}")
    private int graceDays;

    /** Estado de morosidad de un usuario (el dueño o un administrador). */
    public DelinquencyResponse status(Integer userId) {
        currentUser.requireOwnerOrAdmin(userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return evaluate(userId, accountReceivableRepository
                .findByUserIdAndPendingBalanceGreaterThan(userId, BigDecimal.ZERO));
    }

    /** Uso interno (el llamador ya validó permisos): ¿tiene el usuario deuda vencida? */
    public DelinquencyResponse evaluateUser(Integer userId) {
        return evaluate(userId, accountReceivableRepository
                .findByUserIdAndPendingBalanceGreaterThan(userId, BigDecimal.ZERO));
    }

    /** Todos los usuarios morosos, de mayor a menor deuda. */
    public List<DelinquencyResponse> listDelinquent() {
        return openDebtsByUser().entrySet().stream()
                .map(e -> evaluate(e.getKey(), e.getValue()))
                .filter(DelinquencyResponse::isDelinquent)
                .sorted(Comparator.comparing(DelinquencyResponse::getTotalPending).reversed())
                .toList();
    }

    public long countDelinquent() {
        return listDelinquent().size();
    }

    /**
     * Actualiza el estado de las deudas abiertas: IN_COLLECTION para las de usuarios morosos y OVERDUE
     * para las de quienes ya no lo son. Devuelve cuántas deudas cambiaron.
     */
    @Transactional
    public int markDelinquent() {
        int changed = 0;
        for (Map.Entry<Integer, List<AccountReceivable>> entry : openDebtsByUser().entrySet()) {
            DelinquencyStatus target = evaluate(entry.getKey(), entry.getValue()).isDelinquent()
                    ? DelinquencyStatus.IN_COLLECTION : DelinquencyStatus.OVERDUE;
            for (AccountReceivable account : entry.getValue()) {
                if (account.getDelinquencyStatus() != target) {
                    account.setDelinquencyStatus(target);
                    accountReceivableRepository.save(account);
                    changed++;
                }
            }
        }
        return changed;
    }

    /** Se ejecuta cada día a las 00:05 (hora de Costa Rica), justo después del vencimiento de suscripciones. */
    @Scheduled(cron = "0 5 0 * * *", zone = "${application.timezone:America/Costa_Rica}")
    public void markDelinquentDaily() {
        int changed = markDelinquent();
        if (changed > 0) {
            log.info("Deudas con estado de morosidad actualizado: {}", changed);
        }
    }

    private Map<Integer, List<AccountReceivable>> openDebtsByUser() {
        return accountReceivableRepository.findByPendingBalanceGreaterThan(BigDecimal.ZERO).stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));
    }

    private DelinquencyResponse evaluate(Integer userId, List<AccountReceivable> open) {
        LocalDate today = LocalDate.now(clock);
        BigDecimal total = open.stream().map(AccountReceivable::getPendingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Una deuda recién emitida (p. ej. renovar el plan por adelantado) todavía no está vencida:
        // solo cuenta para el monto límite cuando ya pasaron los días de gracia.
        LocalDate graceLimit = today.minusDays(graceDays);
        BigDecimal overdue = open.stream().filter(a -> !a.getIssueDate().isAfter(graceLimit))
                .map(AccountReceivable::getPendingBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate oldest = open.stream().map(AccountReceivable::getIssueDate)
                .min(Comparator.naturalOrder()).orElse(null);
        long days = oldest == null ? 0 : ChronoUnit.DAYS.between(oldest, today);

        boolean overAmount = overdue.compareTo(maxAmount) > 0;
        boolean overDays = days > maxDays;
        String reason;
        if (overAmount && overDays) {
            reason = "Debe ₡%s vencidos (más del límite de ₡%s) y tiene una deuda de hace %d días (más de %d)"
                    .formatted(overdue.toPlainString(), maxAmount.toPlainString(), days, maxDays);
        } else if (overAmount) {
            reason = "Debe ₡%s vencidos, más del límite de ₡%s".formatted(overdue.toPlainString(), maxAmount.toPlainString());
        } else if (overDays) {
            reason = "Tiene una deuda abierta de hace %d días (el límite es %d)".formatted(days, maxDays);
        } else if (open.isEmpty()) {
            reason = "Sin deudas pendientes";
        } else {
            reason = "Tiene deudas pendientes por ₡%s pero dentro de los límites".formatted(total.toPlainString());
        }

        return DelinquencyResponse.builder()
                .userId(userId)
                .delinquent(overAmount || overDays)
                .totalPending(total)
                .totalOverdue(overdue)
                .graceDays(graceDays)
                .openAccounts(open.size())
                .oldestIssueDate(oldest)
                .oldestDebtDays(days)
                .maxAmount(maxAmount)
                .maxDays(maxDays)
                .reason(reason)
                .build();
    }
}
