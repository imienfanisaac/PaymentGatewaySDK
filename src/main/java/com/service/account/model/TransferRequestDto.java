package com.service.account.model;


import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
    @NotNull(message = "Source account number is required")
    @Min(value = 1000000000L, message = "Source account number must be at least 10 digits")
    @Max(value = 9999999999L, message = "Source account number must not exceed 10 digits")
    private Long fromAccountNo;

    @NotNull(message = "Destination account number is required")
    @Min(value = 1000000000L, message = "Recipient account number must be at least 10 digits")
    @Max(value = 9999999999L, message = "Recipient account number must not exceed 10 digits")
    private Long toAccountNo;

    @NotNull(message = "Transfer amount is required")
    @Positive(message = "Transfer amount must be positive")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;


}

