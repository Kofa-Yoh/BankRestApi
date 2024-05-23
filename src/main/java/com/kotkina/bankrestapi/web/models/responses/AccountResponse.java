package com.kotkina.bankrestapi.web.models.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountResponse {
    private Long id;
    private BigDecimal initialDeposit;
    private BigDecimal balance;
}
