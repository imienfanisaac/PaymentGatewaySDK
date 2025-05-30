package com.service.bank.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankRequestDto {

    @NotBlank(message = "Sortcode is required")
    @Pattern(regexp = "\\d+", message = "Sortcode must contain only digits")
    @Size(min = 4,max = 6, message = "Sortcode must be 4-6 digits long")
    private String sortCode;

    @NotBlank(message = "Country is required")
    @Pattern(regexp = "^[A-Za-z\\s-]+$", message = " country name can only contain letters, spaces and hyphens")
    private String country;

    @NotBlank(message = "Bank name is required")
    @Pattern(regexp = "^[A-Za-z\\s-]+$", message = "Bank name can only contain letters, spaces and hyphens")
    private String name;
}
