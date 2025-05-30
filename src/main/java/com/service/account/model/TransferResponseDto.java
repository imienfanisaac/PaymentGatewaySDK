package com.service.account.model;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransferResponseDto {
    private String message;
    private boolean success;
    private LocalDateTime timestamp;
    private TransactionDetails details;

    @Data
    @Builder
    public static class TransactionDetails {
        private Long fromAccountNo;
        private Long toAccountNo;
        private BigDecimal amount;
        private BigDecimal newBalanceFrom;
        private BigDecimal newBalanceTo;
    }
}

