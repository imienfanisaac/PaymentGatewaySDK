package com.service.client.model;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDto {
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[A-Za-z\\s-]+$", message = "Client name can only contain letters, numbers, symbols, spaces and hyphens")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;
}