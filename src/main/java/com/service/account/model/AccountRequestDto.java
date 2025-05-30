package com.service.account.model;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDto {

    @NotBlank(message = "Account name is required")
    @Pattern(regexp = "^[A-Za-z\\s-]+$", message = "Account name can only contain letters, spaces and hyphens")
    @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
    private String accountName;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter ISO code")
    private String currency;

    @NotNull(message = "Balance is required")
    @Positive(message = "Balance must be positive")
    private BigDecimal balance;

}