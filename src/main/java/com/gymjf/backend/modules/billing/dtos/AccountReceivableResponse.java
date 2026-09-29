package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gymjf.backend.modules.billing.domain.DelinquencyStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountReceivableResponse {

    private Integer id;

    private Integer userId;

    private BigDecimal totalDebt;

    private BigDecimal pendingBalance;

    private LocalDate issueDate;

    private DelinquencyStatus delinquencyStatus;

}
