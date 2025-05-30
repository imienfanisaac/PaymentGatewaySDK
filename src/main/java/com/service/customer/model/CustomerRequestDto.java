package com.service.customer.model;


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
public class CustomerRequestDto {
    @NotBlank(message = "First name is required")
    @Pattern(regexp = "^[A-Za-z-]*$", message = "First name must only contain letters and hyphens.")
    @Size(min=2,max = 25, message = "First name cannot be shorter than 2 characters & no longer than 25 characters.")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Pattern(regexp = "^[A-Za-z-]*$", message = "Last name must only contain letters and hyphens.")
    @Size(min=2,max = 25, message = "Last name cannot be shorter than 2 characters & no longer than 25 characters.")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+?[0-9]{7,15})$",message = "Phone number must contain only digits and may start with a '+'")
    @Size(min = 9,max = 15, message = "Phone number must be 9-15 digits long")
    private String phoneNumber;

    @NotNull(message = "Bank ID is required")
    private UUID bankId;

    private UUID clientId;

    private String Bvn;
}


