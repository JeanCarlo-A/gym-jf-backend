package com.gymjf.backend.modules.billing.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.billing.domain.AccountReceivable;
import com.gymjf.backend.modules.billing.domain.DelinquencyStatus;
import com.gymjf.backend.modules.billing.dtos.AccountReceivableResponse;
import com.gymjf.backend.modules.billing.dtos.CreateAccountReceivableRequest;
import com.gymjf.backend.modules.billing.dtos.UpdateAccountReceivableRequest;
import com.gymjf.backend.modules.billing.repositories.AccountReceivableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountReceivableService {

    private final AccountReceivableRepository accountReceivableRepository;
    private final UserRepository userRepository;

    public List<AccountReceivableResponse> getAll() {
        return accountReceivableRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AccountReceivableResponse> getByUserId(Integer userId) {
        return accountReceivableRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AccountReceivableResponse getById(Integer id) {
        AccountReceivable accountReceivable = accountReceivableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta por cobrar no encontrada"));
        return mapToResponse(accountReceivable);
    }

    public AccountReceivableResponse create(CreateAccountReceivableRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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

    public AccountReceivableResponse update(Integer id, UpdateAccountReceivableRequest request) {
        AccountReceivable accountReceivable = accountReceivableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta por cobrar no encontrada"));

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

    public void delete(Integer id) {
        AccountReceivable accountReceivable = accountReceivableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta por cobrar no encontrada"));
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
