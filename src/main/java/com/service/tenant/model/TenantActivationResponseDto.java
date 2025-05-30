package com.service.tenant.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantActivationResponseDto {
    private UUID tenantId;
    private String tenantCode; // API key
    private String name;
    private String status;
    private LocalDateTime expiresAt;

}