package com.gymjf.backend.modules.billing.services;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.billing.domain.PaymentMethod;
import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;
import com.gymjf.backend.modules.billing.dtos.BillingSummaryResponse;
import com.gymjf.backend.modules.billing.dtos.CashClosingResponse;
import com.gymjf.backend.modules.billing.dtos.MethodTotal;
import com.gymjf.backend.modules.billing.repositories.AccountReceivableRepository;
import com.gymjf.backend.modules.billing.repositories.TransactionRepository;
import com.gymjf.backend.shared.domain.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

/** Reportes del administrador: cierre de caja (RF-5.2.2) y resumen para el panel (RF-5.3). Solo lectura. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BillingReportService {

    private final TransactionRepository transactionRepository;
    private final AccountReceivableRepository accountReceivableRepository;
    private final DelinquencyService delinquencyService;
    private final Clock clock;

    /** Lo cobrado en un día (por defecto hoy, hora de Costa Rica), separado por método de pago. */
    public CashClosingResponse cashClosing(LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now(clock);
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.plusDays(1).atStartOfDay();
        List<MethodTotal> byMethod = totals(from, to);
        return new CashClosingResponse(day, byMethod, sum(byMethod),
                byMethod.stream().mapToLong(MethodTotal::count).sum(),
                count(TransactionValidationStatus.PENDING, from, to),
                count(TransactionValidationStatus.REJECTED, from, to));
    }

    /** Resumen de un período (por defecto el mes en curso) más la situación actual de deudas y pagos por aprobar. */
    public BillingSummaryResponse summary(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(clock);
        LocalDate start = from != null ? from : today.withDayOfMonth(1);
        LocalDate end = to != null ? to : today;
        if (end.isBefore(start)) {
            throw new BadRequestException("La fecha final no puede ser anterior a la inicial");
        }
        List<MethodTotal> byMethod = totals(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        return new BillingSummaryResponse(start, end, sum(byMethod), byMethod,
                transactionRepository.countByValidationStatus(TransactionValidationStatus.PENDING),
                accountReceivableRepository.totalOpenDebt(),
                accountReceivableRepository.countByPendingBalanceGreaterThan(BigDecimal.ZERO),
                delinquencyService.countDelinquent());
    }

    private List<MethodTotal> totals(LocalDateTime from, LocalDateTime to) {
        return transactionRepository.totalsByMethod(TransactionValidationStatus.APPROVED, from, to).stream()
                .map(row -> new MethodTotal((PaymentMethod) row[0], (Long) row[1], (BigDecimal) row[2]))
                .sorted(Comparator.comparing(MethodTotal::method))
                .toList();
    }

    private BigDecimal sum(List<MethodTotal> totals) {
        return totals.stream().map(MethodTotal::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long count(TransactionValidationStatus status, LocalDateTime from, LocalDateTime to) {
        return transactionRepository
                .countByValidationStatusAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(status, from, to);
    }
}
