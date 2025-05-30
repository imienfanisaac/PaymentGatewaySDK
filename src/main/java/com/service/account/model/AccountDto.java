package com.service.account.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private UUID id;
    private String accountName;
    private Long accountNo;
    private UUID customerId;
    private String currency;
    private BigDecimal balance;
    private UUID clientId;
}
