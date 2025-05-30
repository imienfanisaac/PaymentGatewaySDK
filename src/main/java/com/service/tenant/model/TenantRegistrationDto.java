package com.service.tenant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationDto {
    private String name;
    private String description;
    private String planType; // BASIC, PRO, ENTERPRISE


}
