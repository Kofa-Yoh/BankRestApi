package com.kotkina.bankrestapi.web.models.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private Boolean success;
    private Instant created;
    private Instant updated;
}
