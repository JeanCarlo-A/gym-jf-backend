package com.gymjf.backend.modules.billing.services;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.billing.domain.AccountReceivable;
import com.gymjf.backend.modules.billing.domain.DelinquencyStatus;
import com.gymjf.backend.modules.billing.dtos.AccountReceivableResponse;
import com.gymjf.backend.modules.billing.dtos.CreateAccountReceivableRequest;
import com.gymjf.backend.modules.billing.dtos.UpdateAccountReceivableRequest;
import com.gymjf.backend.modules.billing.repositories.AccountReceivableRepository;

import lombok.RequiredArgsConstructor;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountReceivableService {

    private final AccountReceivableRepository accountReceivableRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final Clock clock;

    public List<AccountReceivableResponse> getAll() {
        return (currentUser.isAdmin() ? accountReceivableRepository.findAll() : accountReceivableRepository.findByUserId(currentUser.id())).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AccountReceivableResponse> getByUserId(Integer userId) {
        currentUser.requireOwnerOrAdmin(userId);
        return accountReceivableRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AccountReceivableResponse getById(Integer id) {
        AccountReceivable accountReceivable = accountReceivableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));
        currentUser.requireOwnerOrAdmin(accountReceivable.getUser().getId());
        return mapToResponse(accountReceivable);
    }

    /**
     * Emite una deuda nueva a nombre del usuario (por una suscripción o una compra). Nace OVERDUE, que en este
     * sistema significa "pendiente de pago"; la fecha de emisión es hoy en hora de Costa Rica.
     */
    @Transactional
    public AccountReceivable issue(User user, BigDecimal amount) {
        return accountReceivableRepository.save(AccountReceivable.builder()
                .user(user)
                .totalDebt(amount)
                .pendingBalance(amount)
                .issueDate(LocalDate.now(clock))
                .delinquencyStatus(DelinquencyStatus.OVERDUE)
                .build());
    }

    @Transactional
    public AccountReceivableResponse create(CreateAccountReceivableRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        AccountReceivable accountReceivable = AccountReceivable.builder()
                .user(user)
                .totalDebt(request.getTotalDebt())
                .pendingBalance(request.getPendingBalance())
                .issueDate(request.getIssueDate())
                .delinquencyStatus(request.getDelinquencyStatus() != null
                        ? request.getDelinquencyStatus()
                        : DelinquencyStatus.UP_TO_DATE)
                .build();

        accountReceivableRepository.save(accountReceivable);

        return mapToResponse(accountReceivable);
    }

    @Transactional
    public AccountReceivableResponse update(Integer id, UpdateAccountReceivableRequest request) {
        AccountReceivable accountReceivable = accountReceivableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));

        if (request.getTotalDebt() != null) {
            accountReceivable.setTotalDebt(request.getTotalDebt());
        }

        if (request.getPendingBalance() != null) {
            accountReceivable.setPendingBalance(request.getPendingBalance());
        }

        if (request.getDelinquencyStatus() != null) {
            accountReceivable.setDelinquencyStatus(request.getDelinquencyStatus());
        }

        accountReceivableRepository.save(accountReceivable);

        return mapToResponse(accountReceivable);
    }

    @Transactional
    public void delete(Integer id) {
        AccountReceivable accountReceivable = accountReceivableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));
        accountReceivableRepository.delete(accountReceivable);
    }

    private AccountReceivableResponse mapToResponse(AccountReceivable accountReceivable) {
        return AccountReceivableResponse.builder()
                .id(accountReceivable.getId())
                .userId(accountReceivable.getUser().getId())
                .totalDebt(accountReceivable.getTotalDebt())
                .pendingBalance(accountReceivable.getPendingBalance())
                .issueDate(accountReceivable.getIssueDate())
                .delinquencyStatus(accountReceivable.getDelinquencyStatus())
                .build();
    }
}
