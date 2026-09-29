package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.billing.domain.DelinquencyStatus;

import lombok.Data;

@Data
public class UpdateAccountReceivableRequest {

    private BigDecimal totalDebt;

    private BigDecimal pendingBalance;

    private DelinquencyStatus delinquencyStatus;

}
