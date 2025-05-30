package com.service.card.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CardRequestDto {
    @NotBlank(message = "Account number is required")
    @Min(value = 1000000000L, message = "Account number must be at least 10 digits")
    @Max(value = 9999999999L, message = "Account number must not exceed 10 digits")
    private String accountNo;

    @NotBlank(message = "Card holder's name is required")
    @Pattern(regexp = "^[A-Za-z\\s-]+$", message = "Card holder's name can only contain letters, spaces and hyphens")
    @Size(min = 2, max = 100, message = "Client name must be between 2 and 100 characters")
    private String holdersName;

    @NotBlank(message = "Card type is required")
    @Pattern(regexp = "^(VISA Debit|MasterCard Debit|VISA|MasterCard|American Express|AmEx|VC|VC Debit|MC|MC Debit|)$",
            message = "Invalid card type.The card types we support are VISA, VISA Debit, MasterCard, MasterCard Debit, American Express.")
    private String cardType;
}