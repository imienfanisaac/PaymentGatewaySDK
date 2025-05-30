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
public class TenantDto {
    private UUID id;
    private String tenantCode;
    private String name;
    private String description;
    private boolean isActive;
    private LocalDateTime dateCreated;
    private String createdBy;
    private LocalDateTime dateModified;
    private String modifiedBy;
}
